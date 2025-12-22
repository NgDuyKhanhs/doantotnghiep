package lms.doantotnghiep.controller;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.dto.*;
import lms.doantotnghiep.dto.request.AnswersJson;
import lms.doantotnghiep.dto.request.CreateAssignmentDTO;
import lms.doantotnghiep.dto.request.UploadEnrollmentReq;
import lms.doantotnghiep.dto.response.StudentSubmissionResponse;
import lms.doantotnghiep.dto.response.SubmissionHistoryResponse;
import lms.doantotnghiep.enums.ActionType;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.service.*;
import lms.doantotnghiep.service.impl.UserDetailsImple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class TeacherController {

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CurriculumService curriculumService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private SubmissionService submissionService;

    @PostMapping("/upload-pdf")
    public ResponseEntity<?> uploadPDF(@RequestBody EnrollmentDTO enrollmentDTO, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"))) {
            try {
                enrollmentService.addPDFInEnrollment(enrollmentDTO, userDetailsImple.getId());
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Thêm PDF thành công"
                ));
            } catch (AppException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "Thêm PDF thất bại: " + e.getMessage()
                ));
            }
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/upload-assignment")
    public ResponseEntity<?> uploadAssignments(@RequestBody CreateAssignmentDTO dto, Authentication authentication,HttpServletRequest request) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"))) {
            try {
                AssignmentDTO result = assignmentService.createAssignment(userDetailsImple.getId(),dto,request);
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Lỗi tạo bài tập: " + e.getMessage());
            }
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/get-assignment-by-{id}")
    public ResponseEntity<?> getAssignment(@PathVariable int id, Authentication authentication, @RequestParam String type) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null) {
            try {
                List<AssignmentDTO> result = assignmentService.getAssignmentById(id, userDetailsImple.getId(), type);
                if (result != null) {
                    return ResponseEntity.ok(result);
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy bài tập với ID: " + id);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Lỗi: " + e.getMessage());
            }
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

    }

    @GetMapping("/get-detail-assignment-by-{id}")
    public ResponseEntity<?> getDetailAssignmentByID(@PathVariable int id, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null) {
            try {
                AssignmentDTO result = assignmentService.getDetailAssignmentByID(id);
                if (result != null) {
                    return ResponseEntity.ok(result);
                }
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Không tìm thấy bài tập với ID: " + id);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Lỗi: " + e.getMessage());
            }
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

    }

    @PostMapping("/start")
    public ResponseEntity<?> startExam(@RequestParam int assignmentId, @RequestParam ActionType actionType, @RequestBody(required = false) List<AnswersJson> answersJsons, Authentication authentication,HttpServletRequest request) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null) {
            if (answersJsons == null) answersJsons = new ArrayList<>();

            ExamSession session = assignmentService.startExam(userDetailsImple.getId(), assignmentId, answersJsons, actionType, request);
            return ResponseEntity.ok(session);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

    }

    @GetMapping("/count-submitted")
    public ResponseEntity<?> countSubmitted(@RequestParam int assignmentId, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null) {
            Integer countSubmitted = assignmentService.countSubmitted(userDetailsImple.getId(), assignmentId);
            return ResponseEntity.ok(countSubmitted);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

    }

    @GetMapping("/submission-history")
    public ResponseEntity<?> getSubmissionHistory(@RequestParam int assignmentId, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null) {
            SubmissionHistoryResponse submissionHistoryResponse = submissionService.getSubmissionHistory(userDetailsImple.getId(), assignmentId);
            return ResponseEntity.ok(submissionHistoryResponse);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

    }

    @GetMapping("/submission-list")
    public ResponseEntity<?> getListHistorySubmission(
            @RequestParam int assignmentId,
            @RequestParam String status,
            Authentication authentication) {

        try {
            UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);

            if (userDetailsImple == null) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token không hợp lệ"));
            }

            // Lấy role
            boolean isTeacher = userDetailsImple.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_TEACHER"));

            if (!isTeacher) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Bạn không có quyền xem nội dung"));
            }

            // Nếu là TEACHER thì lấy danh sách bài nộp
            List<StudentSubmissionResponse> submissions =
                    submissionService.getStudentSubmissions(
                            userDetailsImple.getId(),
                            assignmentId,
                            status);

            return ResponseEntity.ok(submissions);

        } catch (Exception ex) {
            // Lỗi hệ thống
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống", "error", ex.getMessage()));
        }
    }


}
