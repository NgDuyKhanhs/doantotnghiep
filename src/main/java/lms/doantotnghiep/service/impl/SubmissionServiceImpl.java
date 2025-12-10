package lms.doantotnghiep.service.impl;

import jakarta.transaction.Transactional;
import lms.doantotnghiep.domain.*;
import lms.doantotnghiep.dto.response.StudentSubmissionResponse;
import lms.doantotnghiep.dto.response.SubmissionHistoryResponse;
import lms.doantotnghiep.repository.*;
import lms.doantotnghiep.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ViolationRepository violationRepository;

    @Override
    public int create(int userId, int assignmentId) {
        // Kiểm tra xem đã có submission đang mở chưa
        var existing = submissionRepository.findActiveSubmission(userId, assignmentId);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId);

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setAssignment(assignment);
        submission.setStartTime(java.time.LocalDateTime.now());
        submission.setStatus("IN_PROGRESS");

        submissionRepository.save(submission);
        return submission.getId();
    }

    @Override
    @Transactional
    public SubmissionHistoryResponse getSubmissionHistory(int userId, int assignmentId) {
        // 1) Lấy submission mới nhất của user cho assignment
        Submission submission = submissionRepository
                .findLatestByUserIdAndAssignmentId(userId, assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch sử làm bài."));

        Assignment assignment = assignmentRepository.findById(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment không tồn tại.");
        }

        List<Question> questions = questionRepository.findByAssignmentId(assignmentId);

        List<Answer> answers = answerRepository.findBySubmissionIdWithDetails(submission.getId());
        int correctCount = 0;
        for (Answer ans : answers) {
            if (Boolean.TRUE.equals(ans.getIsCorrect())) {
                correctCount++;
            }
        }

        // 5) Tính spentSeconds từ endTime - startTime
        Long spentSeconds = null;
        if (submission.getStartTime() != null && submission.getEndTime() != null) {
            spentSeconds = java.time.Duration.between(
                    submission.getStartTime(),
                    submission.getEndTime()
            ).getSeconds();
        }
        List<ViolationReport> violations = violationRepository.findByUserIdAndAssignmentIdd(userId, assignmentId);

        List<ViolationReport> violationDetails = new ArrayList<>();
        for (ViolationReport v : violations) {
            ViolationReport vd = new ViolationReport();
            vd.setId(v.getId());
            vd.setTypeViolation(v.getTypeViolation());
            vd.setDescription(v.getDescription());
            vd.setEvidence(v.getEvidence());
            vd.setCreatedAt(v.getCreatedAt());
            violationDetails.add(vd);
        }

        SubmissionHistoryResponse response = new SubmissionHistoryResponse();
        response.setSubmissionId(submission.getId());
        response.setStatus(submission.getStatus());
        response.setScore(submission.getScore());
        response.setStartTime(submission.getStartTime());
        response.setEndTime(submission.getEndTime());
        response.setSpentSeconds(spentSeconds);
        response.setTotalQuestions(questions.size());
        response.setCorrectCount(correctCount);
        response.setViolations(violationDetails);

        return response;
    }

    @Override
    public List<StudentSubmissionResponse> getStudentSubmissions(int teacherId, int assignmentId, String status) {
        Assignment assignment = assignmentRepository.findById(assignmentId);

        List<Submission> submissions = (status != null && !status.isBlank())
                ? submissionRepository.findByAssignmentIdAndStatus(assignmentId, status)
                : submissionRepository.findAllByAssignmentId(assignmentId);

        // 4) Số câu hỏi của assignment
        int totalQuestions = (assignment.getQuestions() != null)
                ? assignment.getQuestions().size()
                : questionRepository.findByAssignmentId(assignmentId).size();

        // 5) Map sang DTO
        return submissions.stream().map(sub -> {
            StudentSubmissionResponse dto = new StudentSubmissionResponse();
            dto.setSubmissionId(sub.getId());
            dto.setStatus(sub.getStatus());
            dto.setScore(sub.getScore() != null ? sub.getScore() : 0.0);
            dto.setStartTime(sub.getStartTime());
            dto.setEndTime(sub.getEndTime());
            dto.setTotalQuestions(totalQuestions);

            // Tính spentSeconds từ startTime - endTime
            if (sub.getStartTime() != null && sub.getEndTime() != null) {
                dto.setSpentSeconds(Duration.between(sub.getStartTime(), sub.getEndTime()).getSeconds());
            } else {
                dto.setSpentSeconds(0L);
            }

            // Thông tin user (đã fetch sẵn)
            User user = sub.getUser();
            if (user != null) {
                dto.setUserId(user.getId());
                dto.setStudentName(user.getFullname());
                dto.setStudentEmail(user.getEmail());
            }

            // Số câu đúng (đếm từ Answer)
            int correctCount = answerRepository.countCorrectBySubmissionId(sub.getId());
            dto.setCorrectCount(correctCount);

            // Số vi phạm (lấy qua assignmentId)
            int violationCount = 0;
            if (user != null) {
                violationCount = violationRepository
                        .findByUserIdAndAssignmentId(user.getId(), assignmentId)
                        .size();
            }
            dto.setViolationCount(violationCount);

            return dto;
        }).collect(Collectors.toList());

    }

}
