package lms.doantotnghiep.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.domain.SysLog;
import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.dto.*;
import lms.doantotnghiep.dto.response.PdfResponse;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.service.CurriculumService;
import lms.doantotnghiep.service.EnrollmentService;
import lms.doantotnghiep.service.SysLogService;
import lms.doantotnghiep.service.UserService;
import lms.doantotnghiep.service.impl.UserDetailsImple;
import lms.doantotnghiep.service.upload.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private CurriculumService curriculumService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private SysLogService sysLogService;
    @Autowired
    private PdfService pdfService;
    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        return new ResponseEntity<>(userDetailsImple, HttpStatus.OK);
    }

    @GetMapping("/get-curriculum")
    public ResponseEntity<?> viewCurriculum(Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<CourseDTO> courses = new ArrayList<>();
        if (userDetailsImple != null) {
            courses = curriculumService.getAllCurriculum();
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/get-all-classes")
    public ResponseEntity<?> viewAllClasses() {
        return new ResponseEntity<>(userService.getAllClasses(), HttpStatus.OK);
    }


    @PostMapping("/register-enrollment")
    public ResponseEntity<?> registerEnrollment(@Valid @RequestBody List<Integer> enrollIds) {
        try {
            enrollmentService.registerEnrollment(enrollIds);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Đăng ký thành công"
            ));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Đăng ký thất bại: " + e.getMessage()
            ));
        }
    }


    @GetMapping("/get-filter-enrollments")
    public ResponseEntity<?> getEnrollmentsFilter(@RequestParam String type, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<EnrollmentDTO> enrollmentDTOS = new ArrayList<>();
        if (userDetailsImple != null) {
            enrollmentDTOS = enrollmentService.getEnrollmentFilter(type, userDetailsImple.getId(), "ROLE_USER");
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(enrollmentDTOS, HttpStatus.OK);
    }

    @GetMapping("/get-list-registered")
    public ResponseEntity<?> getRegisteredCourses(Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        String currentRole = "";
        if(userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))){
            currentRole = "ROLE_USER";
        }
        else if(userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TEACHER"))){
            currentRole = "ROLE_TEACHER";
        }
        List<EnrollmentDTO> enrollmentDTOS = new ArrayList<>();
        enrollmentDTOS = enrollmentService.getEnrollmentFilter("Đã đăng ký", userDetailsImple.getId(), currentRole);
        return new ResponseEntity<>(enrollmentDTOS, HttpStatus.OK);
    }

    @GetMapping("/get-info-teacher-by-id")
    public ResponseEntity<?> getInfoEnroll(Authentication authentication, @RequestParam Integer id) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        UserDTO userDTO = new UserDTO();
        if (userDetailsImple != null) {
            userDTO = userService.getTeacherByID(id);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
    @GetMapping("/get-list-student-from-enrollment")
    public ResponseEntity<?> getListUserFromEnrollment(Authentication authentication, @RequestParam Integer id) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<UserDTO> userDTOS = new ArrayList<>();
        if (userDetailsImple != null) {
            userDTOS = userService.getListUserFromEnrollment(id);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
    }
    @GetMapping("/get-pdfs-by-enrollId")
    public ResponseEntity<?> getPdfFilesByEnrollId(Authentication authentication, @RequestParam Integer id) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<PdfResponse> PpfFiles = new ArrayList<>();
        if (userDetailsImple != null) {
            PpfFiles = enrollmentService.getPDFFilesByEnrollmentId(id);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(PpfFiles, HttpStatus.OK);
    }

    @GetMapping("/syslogs-user")
    private ResponseEntity<?> getSysLog(Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<SysLogDTO> sysLogs = new ArrayList<>();
        if (userDetailsImple != null) {
            sysLogs = sysLogService.getSysLogByUser(userDetailsImple.getId());
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(sysLogs, HttpStatus.OK);
    }

    @PostMapping("/save-sys-log")
    public ResponseEntity<?> saveSysLog(@RequestBody SysLogDTO sysLogDTO, HttpServletRequest request, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        SysLog sysLog = new SysLog();
        if (userDetailsImple != null) {
            sysLogDTO.setUserId(userDetailsImple.getId());
            sysLog = sysLogService.saveSysLogFromUser(sysLogDTO,request);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(sysLog, HttpStatus.OK);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewPdf(@PathVariable Integer id, @RequestParam String url, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        String signedUrl = "";
        if (userDetailsImple != null) {
            boolean hasAccess = enrollmentService.hasEnrolledCourse(userDetailsImple.getId(), url);
            if (!hasAccess) {
                return new ResponseEntity<>("Bạn chưa đăng ký học, không thể xem PDF này", HttpStatus.FORBIDDEN);
            }
            signedUrl = pdfService.generateSignedUrl(url);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(signedUrl, HttpStatus.OK);
    }
}
