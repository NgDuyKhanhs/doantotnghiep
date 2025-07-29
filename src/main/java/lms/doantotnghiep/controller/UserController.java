package lms.doantotnghiep.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.dto.CourseDTO;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.service.CurriculumService;
import lms.doantotnghiep.service.EnrollmentService;
import lms.doantotnghiep.service.UserService;
import lms.doantotnghiep.service.impl.UserDetailsImple;
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
    public ResponseEntity<?> getEnrollmentsFilter(@RequestParam  String type,Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<EnrollmentDTO> enrollmentDTOS = new ArrayList<>();
        if (userDetailsImple != null) {
            enrollmentDTOS = enrollmentService.getEnrollmentFilter(type,userDetailsImple.getId());
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(enrollmentDTOS, HttpStatus.OK);
    }
}
