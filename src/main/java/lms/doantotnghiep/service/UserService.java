package lms.doantotnghiep.service;

import com.nimbusds.jose.JOSEException;
import lms.doantotnghiep.domain.Class;

import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.ResponseLoginDTO;
import lms.doantotnghiep.dto.TokenResponse;
import lms.doantotnghiep.dto.UserDTO;
import lms.doantotnghiep.dto.request.UploadEnrollmentReq;
import lms.doantotnghiep.service.impl.UserDetailsImple;
import org.springframework.security.core.Authentication;


import java.text.ParseException;
import java.util.List;

public interface UserService {
    void register(UserDTO userDTO);
    boolean activeAccount(String token);
    ResponseLoginDTO login(UserDTO userDTO);
    TokenResponse refreshToken(String refreshToken) throws ParseException, JOSEException;
    UserDetailsImple getPrincipal(Authentication authentication);


    List<Class> getAllClasses();
    //Admin
    List<UserDTO> getAllTeachers();
    void uploadEnrollment(UploadEnrollmentReq uploadEnrollmentReq);
}
