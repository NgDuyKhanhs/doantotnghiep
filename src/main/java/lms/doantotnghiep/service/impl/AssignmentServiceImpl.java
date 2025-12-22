package lms.doantotnghiep.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lms.doantotnghiep.domain.*;
import lms.doantotnghiep.dto.AssignmentDTO;
import lms.doantotnghiep.dto.ChoiceDTO;
import lms.doantotnghiep.dto.ExamSession;
import lms.doantotnghiep.dto.QuestionDTO;
import lms.doantotnghiep.dto.request.AnswersJson;
import lms.doantotnghiep.dto.request.CreateAssignmentDTO;
import lms.doantotnghiep.dto.request.CreateChoiceDTO;
import lms.doantotnghiep.dto.request.CreateQuestionDTO;
import lms.doantotnghiep.dto.response.SubmitResult;
import lms.doantotnghiep.enums.ActionType;
import lms.doantotnghiep.repository.*;
import lms.doantotnghiep.service.AssignmentService;
import lms.doantotnghiep.service.ExamSessionService;
import lms.doantotnghiep.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ViolationRepository violationRepository;
    @Autowired
    private SysLogRepository sysLogRepository;
    @Override
    @Transactional
    public AssignmentDTO createAssignment(int id,CreateAssignmentDTO assignmentDTO, HttpServletRequest request) {
        Assignment assignment = new Assignment();
        assignment.setDescription(assignmentDTO.getDescription());
        assignment.setTitle(assignmentDTO.getTitle());
        assignment.setStartTime(assignmentDTO.getStartTime());
        assignment.setEndTime(assignmentDTO.getEndTime());
        assignment.setDuration(assignmentDTO.getDuration());
        enrollmentRepository.findById(assignmentDTO.getEnrollId())
                .ifPresent(assignment::setEnrollment);
        String currentIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String deviceName = detectDeviceName(userAgent);
        List<Question> questions = assignmentDTO.getQuestions().stream().map(qdto -> {
            Question q = new Question();
            q.setText(qdto.getText());
            q.setAssignment(assignment);

            List<Choice> choices = qdto.getChoices().stream().map(cdto -> {
                Choice c = new Choice();
                c.setText(cdto.getText());
                c.setQuestion(q);
                c.setIsCorrect(cdto.isCorrect());
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
        SysLog sysLog = new SysLog();
        User user = userRepository.findById(id).get();
        sysLog.setUser(user);
        sysLog.setAction("Tạo bài tập");
        sysLog.setNameDevice(deviceName);
        sysLog.setStartTime(LocalDateTime.now());
        sysLog.setIpAddress(currentIp);
        sysLog.setStatus(0);
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
    public List<AssignmentDTO> getAssignmentById(int enrollId, int userId, String type) {
        List<Assignment> assignments = new ArrayList<>();

        if (type.equalsIgnoreCase("chưa nộp")) {
            assignments = assignmentRepository.findUnsubmittedByEnrollID(enrollId, userId);
        } else if (type.equalsIgnoreCase("đã nộp")) {
            assignments = assignmentRepository.findSubmittedByEnrollID(userId, enrollId);
        } else {
            throw new IllegalArgumentException("Unknown type: " + type + ". Allowed: submitted, unsubmitted");
        }

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
    public String detectDeviceName(String userAgent) {
        if (userAgent.contains("iPad")) {
            return "iPad - Safari Mobile (iOS)";
        } else if (userAgent.contains("iPhone")) {
            return "iPhone - Safari Mobile (iOS)";
        } else if (userAgent.contains("Android")) {
            return "Android - Chrome";
        } else if (userAgent.contains("Windows")) {
            return "Windows - Chrome/Edge";
        } else if (userAgent.contains("Mac OS X")) {
            return "MacOS - Safari/Chrome";
        }
        return "Unknown Device";
    }
    @Override
    public ExamSession startExam(int userId, int assignmentId, List<AnswersJson> answersJsons, ActionType actionType, HttpServletRequest request) {
        // Chặn khi đang có phiên active của bài khác
        ExamSession active = examSessionService.findActiveSessionByUser(userId);
        if (active != null && active.getAssignmentId() != assignmentId) {
            throw new IllegalStateException("Hãy hoàn thành bài tập trước đó!");
        }

        ExamSession existing = examSessionService.getExamSession(userId, assignmentId);
        if (existing != null) {
            switch (actionType) {
                case UPDATE:
                    if (answersJsons != null) {
                        existing.setAnswersJson(answersJsons);
                        examSessionService.saveExamSession(existing, existing.getRemainingSeconds(), false);
                    }
                    return existing;

                case START:
                    return existing;

                case REFRESH:
                case OUT:
                    return existing;

                default:
                    return existing;
            }
        }

        if (actionType != ActionType.START) {
            throw new IllegalStateException("Chưa có phiên làm bài. Vui lòng bắt đầu (START) trước khi thực hiện thao tác này.");
        }

        // Tạo phiên mới cho hành động START
        int submissionId = submissionService.create(userId, assignmentId);
        Assignment assignment = assignmentRepository.findById(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Bài tập không tồn tại.");
        }

        long durationSeconds = Long.parseLong(assignment.getDuration()) * 60L;
        String currentIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String deviceName = detectDeviceName(userAgent);
        ExamSession session = new ExamSession();
        session.setUserId(userId);
        session.setAssignmentId(assignmentId);
        session.setSubmissionId(submissionId);
        session.setStartTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        session.setRemainingSeconds(durationSeconds);
        SysLog sysLog = new SysLog();
        User user = userRepository.findById(userId).orElse(null);
        sysLog.setUser(user);
        sysLog.setAction("Bắt đầu làm bài");
        sysLog.setNameDevice(deviceName);
        sysLog.setStartTime(LocalDateTime.now());
        sysLog.setIpAddress(currentIp);
        sysLogRepository.save(sysLog);
        // START: không set answerJsons từ request, khởi tạo rỗng
        session.setAnswersJson(new ArrayList<>());

        // Lưu với TTL theo đúng số giây còn lại
        examSessionService.saveExamSession(session, durationSeconds, true);
        return session;
    }

    @Override
    @Transactional

    public SubmitResult submitExam(int userId, int assignmentId, List<AnswersJson> answersFromClient,HttpServletRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Bài tập không tồn tại.");
        }
        String currentIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String deviceName = detectDeviceName(userAgent);
        // 1) Lấy session từ Redis
        ExamSession session = examSessionService.getExamSession(userId, assignmentId);

        // 1.1) Xác định submission
        Integer submissionId = (session != null) ? session.getSubmissionId() : null;
        Submission submission;
        if (submissionId != null) {
            submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new IllegalStateException("Submission không tồn tại."));
        } else {
            submission = submissionRepository
                    .findActiveByUserIdAndAssignmentId(userId, assignmentId)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy submission đang thực hiện."));
            submissionId = submission.getId();
        }

        if (Objects.equals(submission.getStatus(), "SUBMITTED")) {
            throw new IllegalStateException("Bài đã được nộp trước đó.");
        }

        // 2) Hợp nhất answers (client ghi đè)
        List<AnswersJson> merged = mergeAnswers(
                (session != null) ? session.getAnswersJson() : Collections.emptyList(),
                (answersFromClient != null) ? answersFromClient : Collections.emptyList()
        );

        // 3) Tải câu hỏi để tạo bản ghi Answer cho từng câu
        List<Question> questions = questionRepository.findByAssignmentId(assignmentId);
        if (questions.isEmpty()) {
            throw new IllegalStateException("Bài tập chưa có câu hỏi.");
        }

        // Build map: questionId -> selectedChoiceId (int; 0 = chưa chọn)
        Map<Integer, Integer> selectedMap = new LinkedHashMap<>();
        if (merged != null) {
            for (AnswersJson a : merged) {
                if (a == null) continue;

                // questionId an toàn cho cả int/Integer
                int qid;
                try {
                    qid = a.getQuestionId(); // int
                } catch (Throwable t) {
                    Integer q = a.getQuestionId(); // Integer
                    qid = (q != null) ? q : 0;
                }
                if (qid <= 0) continue;

                // selectedChoiceId an toàn cho cả int/Integer
                int sel;
                try {
                    sel = a.getSelectedChoiceId(); // int
                } catch (Throwable t) {
                    Integer v = a.getSelectedChoiceId(); // Integer
                    sel = (v != null) ? v : 0;
                }

                selectedMap.put(qid, Math.max(sel, 0));
            }
        }

        // Nạp entity Choice được chọn (id > 0) để dùng cờ isCorrect
        Set<Integer> selectedChoiceIds = new HashSet<>();
        for (Integer sel : selectedMap.values()) {
            if (sel != null && sel > 0) selectedChoiceIds.add(sel);
        }

        Map<Integer, Choice> selectedChoiceEntities = new HashMap<>();
        if (!selectedChoiceIds.isEmpty()) {
            Iterable<Choice> choicesIt = choiceRepository.findAllById(selectedChoiceIds);
            for (Choice c : choicesIt) {
                selectedChoiceEntities.put(c.getId(), c);
            }
        }

        // 4) Xóa answer cũ và ghi answer mới
        answerRepository.deleteBySubmissionId(submissionId);

        int totalQuestions = questions.size();
        int correctCount = 0;
        List<Answer> toSave = new ArrayList<>(totalQuestions);

        LocalDateTime now = LocalDateTime.now();

        for (Question q : questions) {
            int qid = q.getId();
            int selectedChoiceId = selectedMap.getOrDefault(qid, 0);

            Choice selectedChoice = (selectedChoiceId > 0) ? selectedChoiceEntities.get(selectedChoiceId) : null;

            // Chấm điểm dựa trên Choice.isCorrect; đồng thời chắc chắn choice thuộc đúng question
            boolean isCorrect = false;
            if (selectedChoice != null) {
                Integer choiceQid = (selectedChoice.getQuestion() != null) ? selectedChoice.getQuestion().getId() : 0;
                if (choiceQid == qid) {
                    Boolean flag = selectedChoice.getIsCorrect(); // có thể là Boolean
                    isCorrect = (flag != null && flag);
                } else {
                    // lựa chọn không thuộc câu hỏi hiện tại -> sai
                    isCorrect = false;
                }
            }

            Answer ans = new Answer();
            ans.setSubmission(submission);
            ans.setQuestion(q);
            ans.setChoice(selectedChoice);
            ans.setIsCorrect(isCorrect);
            ans.setCreatedAt(now);
            ans.setUpdatedAt(now);
            toSave.add(ans);

            if (isCorrect) correctCount++;
        }

        answerRepository.saveAll(toSave);

        // 5) Tính điểm
        double rawScore = (totalQuestions == 0) ? 0.0 : ((double) correctCount / (double) totalQuestions) * 10.0;
        double score = Math.round(rawScore * 100.0) / 100.0;

        // 6) Cập nhật submission
        LocalDateTime submittedAt = now;
        submission.setStatus("SUBMITTED");
        submission.setEndTime(submittedAt);
        submission.setScore(score);
        submissionRepository.save(submission);
        SysLog sysLog = new SysLog();
        User user = userRepository.findById(userId).orElse(null);
        sysLog.setUser(user);
        sysLog.setAction("Nộp bài");
        sysLog.setNameDevice(deviceName);
        sysLog.setStartTime(LocalDateTime.now());
        sysLog.setIpAddress(currentIp);
        sysLog.setStatus(0);
        sysLogRepository.save(sysLog);
        // 7) Xóa session Redis
        try {
            examSessionService.deleteExamSession(userId, assignmentId);
        } catch (Exception ignored) {}

        return new SubmitResult(
                submissionId,
                totalQuestions,
                correctCount,
                score,
                submittedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    @Override
    public ViolationReport createViolation(int userId, int assignmentId, String typeViolation, String description, String evidence) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        Assignment assignment = assignmentRepository.findById(assignmentId);

        ViolationReport vr = new ViolationReport();
        vr.setTypeViolation(typeViolation);
        vr.setDescription(description);
        vr.setEvidence(evidence);
        vr.setCreatedAt(LocalDateTime.now());
        vr.setUser(user);
        vr.setAssignment(assignment);

        return violationRepository.save(vr);
    }

    @Override
    public Integer countSubmitted(int userId, int assignmentId) {
        return null;
    }

    @Override
    public List<AssignmentDTO> findUnsubmitted(int userId) {
        List<Assignment> assignments = new ArrayList<>();
        assignments = assignmentRepository.findUnsubmitted(userId);
        return assignments.stream()
                .map(this::toDto)
                .toList();
    }

    private List<AnswersJson> mergeAnswers(List<AnswersJson> fromSession, List<AnswersJson> fromClient) {
        // Ưu tiên dữ liệu client nếu có, và khử trùng lặp theo questionId
        Map<Integer, AnswersJson> map = new LinkedHashMap<>();
        if (fromSession != null) {
            for (AnswersJson a : fromSession) {
                if (a == null) continue;
                map.put(a.getQuestionId(), a);
            }
        }
        if (fromClient != null) {
            for (AnswersJson a : fromClient) {
                if (a == null) continue;
                map.put(a.getQuestionId(), a); // overwrite
            }
        }
        return new ArrayList<>(map.values());
    }



}
