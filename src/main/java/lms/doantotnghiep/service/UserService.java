package lms.doantotnghiep.service;

import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.Class;

import lms.doantotnghiep.dto.*;
import lms.doantotnghiep.dto.request.UploadEnrollmentReq;
import lms.doantotnghiep.service.impl.UserDetailsImple;
import org.springframework.security.core.Authentication;


import java.text.ParseException;
import java.util.List;

public interface UserService {
    void register(UserDTO userDTO);
    boolean activeAccount(String token);
    ResponseLoginDTO login(UserDTO userDTO,  HttpServletRequest request);
    TokenResponse refreshToken(String refreshToken) throws ParseException, JOSEException;
    UserDetailsImple getPrincipal(Authentication authentication);


    List<Class> getAllClasses();
    //Admin
    List<UserDTO> getAllTeachers();
    void uploadEnrollment(EnrollmentDTO enrollmentDTO);
    List<UserDTO> getAllStudents(String className);
    UserDTO getUserByID(Integer id);
    UserDTO getMoreDetailsUser(Integer id);
    List<UserDTO> getListUserFromEnrollment(Integer enrollId);

    void swapCourseForUser(Integer courseID, Integer id);
}
