package lms.doantotnghiep.service.impl;

import com.cloudinary.api.exceptions.ApiException;
import com.nimbusds.jose.JOSEException;
import lms.doantotnghiep.domain.*;
import lms.doantotnghiep.domain.Class;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.ResponseLoginDTO;
import lms.doantotnghiep.dto.TokenResponse;
import lms.doantotnghiep.dto.UserDTO;
import lms.doantotnghiep.dto.request.UploadEnrollmentReq;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import lms.doantotnghiep.enums.RoleType;
import lms.doantotnghiep.enums.TokenType;
import lms.doantotnghiep.jwt.JwtTokenProvider;
import lms.doantotnghiep.repository.*;
import lms.doantotnghiep.service.MailService;
import lms.doantotnghiep.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Value("${urlDefaultAvt}")
    private String defaultAvt;
    @Value("${urlClient}")
    private String urlClient;

    @Value("${jwt.refreshable-duration}")
    long refreshableDuration;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private MailService mailService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Override
    public void register(UserDTO userDTO) {
        // validate
        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            throw new AppException(ErrorConstant.PASSWORDS_NOT_MATCH);
        }
        List<User> userList = userRepository.findAllByEmail(userDTO.getEmail());
        userList.forEach(user -> {
            if (user.isActive()) {
                throw new AppException(ErrorConstant.EMAIL_IS_USED);
            }
            if (isExpireTime(user.getCreatedAt())) {
                userRepository.delete(user);
            } else {
                throw new AppException(ErrorConstant.EMAIL_IS_IN_PROCESS);
            }
        });

        User user = new User();
        Set<Role> roles = new HashSet<>();
        BeanUtils.copyProperties(userDTO, user);
        user.setActive(false);
        user.setAvatar(defaultAvt);
        if (classRepository.findById(userDTO.getClassId()).isPresent()) {
            user.setClassId(classRepository.findById(userDTO.getClassId()).get());
        }

        user.setPassword(encoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByRoleName(RoleType.ROLE_USER)
                .orElseThrow(() -> new RuntimeException(ErrorConstant.ROLE_NOT_EXIST.getMessage()));
        roles.add(userRole);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        UserDetailsImple userDetailsImple = UserDetailsImple.build(savedUser);
        String activeToken = jwtTokenProvider.generateJwtToken(userDetailsImple, TokenType.ACCESS_TOKEN);
        String activateLink = urlClient + "/active/" + activeToken;
        mailService.sendActivationEmail(user.getEmail(), user.getFullname(), "Kích hoạt tài khoản", activateLink);
    }

    private boolean isExpireTime(LocalDateTime createdAt) {
        LocalDateTime createdAtPlus3Minutes = createdAt.plusMinutes(3);
        LocalDateTime now = LocalDateTime.now();
        return createdAtPlus3Minutes.isBefore(now);
    }

    @Override
    public boolean activeAccount(String token) {
        String email = jwtTokenProvider.extractEmail(token, TokenType.ACCESS_TOKEN);
        User user = userRepository.findByEmailSigned(email)
                .orElseThrow(() -> new AppException(ErrorConstant.USER_NOT_EXISTED));
        if (user.isActive()) {
            throw new AppException(ErrorConstant.USER_ACTIVATED);
        } else {
            user.setActive(true);
            userRepository.save(user);
            return true;
        }
    }

    @Override
    public ResponseLoginDTO login(UserDTO userDTO) {
        var user = userRepository
                .findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new AppException(ErrorConstant.INVALID_USERNAME_PASSWORD));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userDTO.getEmail(),
                        userDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImple userDetailsImple = (UserDetailsImple) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateJwtToken(userDetailsImple, TokenType.ACCESS_TOKEN);
        String refreshToken = jwtTokenProvider.generateJwtToken(userDetailsImple, TokenType.REFRESH_TOKEN);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // chỉ https
                .path("/")
                .maxAge(refreshableDuration)
                .build();

        return ResponseLoginDTO.builder()
                .userId(user.getId())
                .accessToken(jwt)
                .refreshCookie(refreshCookie)
                .build();
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) throws ParseException, JOSEException {
        var signedRefreshJWT = jwtTokenProvider.verifyToken(refreshToken, TokenType.REFRESH_TOKEN);
//        if logged out the refresh token
        if (!jwtTokenProvider.validateJwtToken(refreshToken, TokenType.REFRESH_TOKEN)) {
            throw new AppException(ErrorConstant.UNAUTHENTICATED);
        }
        var email = signedRefreshJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorConstant.UNAUTHENTICATED));
        UserDetailsImple userDetailsImple = UserDetailsImple.build(user);
        var accessToken = jwtTokenProvider.generateJwtToken(userDetailsImple , TokenType.ACCESS_TOKEN);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .build();
    }


    @Override
    public UserDetailsImple getPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImple) {
            return (UserDetailsImple) principal;
        }
        return null;
    }

    @Override
    public List<Class> getAllClasses() {
        return classRepository.findAll();
    }

    @Override
    public List<UserDTO> getAllTeachers() {
        return userRepository.getListTeachers();
    }

    @Override
    public void uploadEnrollment(UploadEnrollmentReq uploadEnrollmentReq) {
        EnrollmentDTO dto = uploadEnrollmentReq.getEnrollmentDTO();

        if (dto == null) {
            throw new AppException(ErrorConstant.INVALID_ENROLLMENT_DTO);
        }

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new AppException(ErrorConstant.COURSE_NOT_FOUND));

        if (dto.getStartTime() == null || dto.getEndTime() == null) {
            throw new AppException(ErrorConstant.INVALID_TIME_RANGE);
        }

        if (dto.getStartTime().after(dto.getEndTime())) {
            throw new AppException(ErrorConstant.INVALID_TIME_RANGE);
        }

        if (dto.getAvailable() == null || dto.getAvailable() <= 0) {
            throw new AppException(ErrorConstant.INVALID_AVAILABLE);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setLocked(dto.isLocked());
        enrollment.setAvailable(dto.getAvailable());
        enrollment.setStartTime(dto.getStartTime().toLocalDateTime());
        enrollment.setEndTime(dto.getEndTime().toLocalDateTime());
        enrollment.setCourse(course);

        enrollmentRepository.save(enrollment);
    }


}
