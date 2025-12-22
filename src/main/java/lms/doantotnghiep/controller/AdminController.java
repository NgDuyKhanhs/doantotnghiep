package lms.doantotnghiep.controller;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.dto.CourseDTO;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.UserDTO;
import lms.doantotnghiep.dto.request.UploadEnrollmentReq;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.repository.SubmissionRepository;
import lms.doantotnghiep.service.CurriculumService;
import lms.doantotnghiep.service.EnrollmentService;
import lms.doantotnghiep.service.UserService;
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
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CurriculumService curriculumService;
    @Autowired
    private SubmissionRepository submissionRepository;

    @GetMapping("/get-list-teacher")
    public ResponseEntity<?> getListTeachers(Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<UserDTO> userDTOS = new ArrayList<>();
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            userDTOS = userService.getAllTeachers();
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
    }

    @GetMapping("/get-user-by-id")
    public ResponseEntity<?> getUserByID(Authentication authentication, @RequestParam Integer id) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        UserDTO userDTO = new UserDTO();
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            userDTO = userService.getMoreDetailsUser(id);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @GetMapping("/get-all-enrollments")
    public ResponseEntity<?> getEligibleEnrollments(Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<EnrollmentDTO> enrollmentDTOS = new ArrayList<>();
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            enrollmentDTOS = enrollmentService.getAllEnrollments(userDetailsImple.getClassId());
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(enrollmentDTOS, HttpStatus.OK);
    }

    @PostMapping("/upload-enrollment")
    public ResponseEntity<?> uploadEnrollment(@RequestBody EnrollmentDTO enrollmentDTO, Authentication authentication, HttpServletRequest request) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            try {
                userService.uploadEnrollment(userDetailsImple.getId(),enrollmentDTO,request);
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Tạo khóa học thành công"
                ));
            } catch (AppException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "Tạo khóa học thất bại: " + e.getMessage()
                ));
            }
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/get-list-users")
    public ResponseEntity<?> getListUsers(@RequestParam String className, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<UserDTO> userDTOS = new ArrayList<>();
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            userDTOS = userService.getAllStudents(className);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
    }

    @PostMapping("/upload-course")
    public ResponseEntity<?> uploadCourse(@RequestBody CourseDTO courseDTO, Authentication authentication,HttpServletRequest request) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            try {
                curriculumService.createCurriculum(userDetailsImple.getId(),request,courseDTO);
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Tạo khóa học thành công"
                ));
            } catch (AppException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "Tạo khóa học thất bại: " + e.getMessage()
                ));
            }
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }


    @PostMapping("/swap-course-for-user")
    public ResponseEntity<?> swapCourseForUser(@RequestParam Integer courseID, @RequestParam Integer id, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            try {
                userService.swapCourseForUser(courseID,id);
                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Thay giảng viên dạy thành công"
                ));
            } catch (AppException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                ));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", "Thay giảng viên dạy thát bại: " + e.getMessage()
                ));
            }
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/decrease-score")
    ResponseEntity<?> decreaseScore(@RequestParam Integer id, Authentication authentication, @RequestParam Double score, @RequestParam String email) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            userService.decreaseScore(id, score, email);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Hạ điểm thành công"));
        }
        return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "Lỗi"));
    }
}
