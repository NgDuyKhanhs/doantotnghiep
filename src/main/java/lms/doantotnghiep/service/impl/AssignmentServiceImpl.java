package lms.doantotnghiep.service.impl;

import jakarta.transaction.Transactional;
import lms.doantotnghiep.domain.*;
import lms.doantotnghiep.dto.AssignmentDTO;
import lms.doantotnghiep.dto.ChoiceDTO;
import lms.doantotnghiep.dto.ExamSession;
import lms.doantotnghiep.dto.QuestionDTO;
import lms.doantotnghiep.dto.request.CreateAssignmentDTO;
import lms.doantotnghiep.dto.request.CreateChoiceDTO;
import lms.doantotnghiep.dto.request.CreateQuestionDTO;
import lms.doantotnghiep.repository.*;
import lms.doantotnghiep.service.AssignmentService;
import lms.doantotnghiep.service.ExamSessionService;
import lms.doantotnghiep.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class AssignmentServiceImpl implements AssignmentService {
    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ChoiceRepository choiceRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private ExamSessionService examSessionService;
    @Autowired
    private SubmissionService submissionService;
    @Override
    @Transactional
    public AssignmentDTO createAssignment(CreateAssignmentDTO assignmentDTO) {
        Assignment assignment = new Assignment();
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setStartTime(assignmentDTO.getStartTime());
        assignment.setEndTime(assignmentDTO.getEndTime());
        assignment.setDuration(assignmentDTO.getDuration());
        enrollmentRepository.findById(assignmentDTO.getEnrollId())
                .ifPresent(assignment::setEnrollment);

        List<Question> questions = assignmentDTO.getQuestions().stream().map(qdto -> {
            Question q = new Question();
            q.setText(qdto.getText());
            q.setAssignment(assignment);

            List<Choice> choices = qdto.getChoices().stream().map(cdto -> {
                Choice c = new Choice();
                c.setText(cdto.getText());
                c.setQuestion(q);
                return c;
            }).collect(Collectors.toList());

            if (qdto.getCorrectChoiceId() >= 0 && qdto.getCorrectChoiceId() < choices.size()) {
                q.setCorrectChoiceId(qdto.getCorrectChoiceId());
            } else {
                q.setCorrectChoiceId(-1);
            }

            q.setChoices(choices);
            return q;
        }).collect(Collectors.toList());

        assignment.setQuestions(questions);
        Assignment saved = assignmentRepository.save(assignment);
        return toDto(saved);
    }

    private AssignmentDTO toDto(Assignment assignment) {
        List<QuestionDTO> questionDTOS = assignment.getQuestions().stream().map(q -> {
            List<ChoiceDTO> choiceDTOS = q.getChoices().stream().map(c ->
                    new ChoiceDTO(c.getId(), c.getText(), c.getQuestion().getId())
            ).toList();

            return new QuestionDTO(q.getId(), q.getText(), q.getCorrectChoiceId(), assignment.getId(), choiceDTOS);
        }).toList();

        return new AssignmentDTO(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getStartTime(),
                assignment.getEndTime(),
                assignment.getDuration(),
                assignment.getEnrollment().getId(),
                questionDTOS
        );
    }
    @Override
    public List<AssignmentDTO> getAssignmentById(int enrollId) {
        List<Assignment> assignments = assignmentRepository.findByEnrollment_Id(enrollId);

        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }

        return assignments.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public AssignmentDTO getDetailAssignmentByID(int id) {
       Assignment assignment = assignmentRepository.findById(id);
       return toDto(assignment);
    }

    @Override
    public ExamSession startExam(int userId, int assignmentId) {
        ExamSession active = examSessionService.findActiveSessionByUser(userId);
        if (active != null && active.getAssignmentId() != assignmentId) {
            throw new IllegalStateException("Hãy hoàn thành bài tập trước đó!");
        }

        ExamSession existing = examSessionService.getExamSession(userId, assignmentId);
        if (existing != null) return existing;

        int submissionId = submissionService.create(userId, assignmentId);
        Assignment assignment = assignmentRepository.findById(assignmentId);

        ExamSession session = new ExamSession();
        session.setUserId(userId);
        session.setAssignmentId(assignmentId);
        session.setSubmissionId(submissionId);
        session.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        session.setRemainingSeconds(Long.parseLong(assignment.getDuration()) * 60);

        examSessionService.saveExamSession(session, Long.parseLong(assignment.getDuration()));

        return session;
    }


}
