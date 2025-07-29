package lms.doantotnghiep.controller;

import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.UserDTO;
import lms.doantotnghiep.dto.request.UploadEnrollmentReq;
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

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("/get-list-teacher")
    public ResponseEntity<?> activeAccount(Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        List<UserDTO> userDTOS = new ArrayList<>();
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            userDTOS = userService.getAllTeachers();
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
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
    public ResponseEntity<?> uploadEnrollment(@RequestBody UploadEnrollmentReq uploadEnrollmentReq, Authentication authentication) {
        UserDetailsImple userDetailsImple = userService.getPrincipal(authentication);
        if (userDetailsImple != null && userDetailsImple.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            userService.uploadEnrollment(uploadEnrollmentReq);
        } else return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>("Tạo khóa học thành công", HttpStatus.OK);
    }
}
