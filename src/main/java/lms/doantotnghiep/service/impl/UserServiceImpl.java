package lms.doantotnghiep.service.impl;

import com.cloudinary.api.exceptions.ApiException;
import com.nimbusds.jose.JOSEException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.*;

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

    @Autowired
    private SysLogRepository sysLogRepository;
    @Autowired
    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

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
    public ResponseLoginDTO login(UserDTO userDTO, HttpServletRequest request) {
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
        checkAndLogLogin(user, request, "Đăng nhập");
        return ResponseLoginDTO.builder()
                .userId(user.getId())
                .accessToken(jwt)
                .refreshCookie(refreshCookie)
                .build();
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
    private void checkAndLogLogin(User user, HttpServletRequest request, String action) {
        String currentIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String deviceName = detectDeviceName(userAgent);

        List<SysLog> recentLogs = sysLogRepository.findRecentByUserId(user.getId());
        SysLog lastLog = recentLogs.isEmpty() ? null : recentLogs.get(0);

        boolean suspicious = false;
        StringBuilder desc = new StringBuilder();

        if (lastLog != null) {
            if (!currentIp.equals(lastLog.getIpAddress())) {
                suspicious = true;
                desc.append("IP thay đổi");
            }

            boolean existedDevice = sysLogRepository.existsByUserIdAndNameDevice(user.getId(), deviceName);
            if (!existedDevice) {
                suspicious = true;
                desc.append("Thiết bị mới");
            }

            long countIn5Min = sysLogRepository.countRecentLogins(user.getId(), LocalDateTime.now().minusMinutes(5));
            if (countIn5Min > 3) {
                suspicious = true;
                desc.append("Quá nhiều yêu cầu trong 5 phút. ");
            }
        }

        // Ghi log
        SysLog log = new SysLog();
        log.setUser(user);
        log.setStartTime(LocalDateTime.now());
        log.setNameDevice(deviceName);
        log.setAction(action);
        log.setStatus(suspicious ? 4 : 0); // 0 = thành công, 4 = cảnh báo
        log.setIpAddress(currentIp);
        log.setDescription(desc.toString());
        sysLogRepository.save(log);

        // Gửi cảnh báo nếu bất thường
        if (suspicious) {
            mailService.sendSuspiciousLoginEmail(
                    user.getEmail(),
                    user.getFullname(),
                    currentIp,
                    deviceName,
                    LocalDateTime.now().toString()
            );
        }
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
        var accessToken = jwtTokenProvider.generateJwtToken(userDetailsImple, TokenType.ACCESS_TOKEN);
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
    public void uploadEnrollment(EnrollmentDTO enrollmentDTO) {
        if (enrollmentDTO == null) {
            throw new AppException(ErrorConstant.INVALID_ENROLLMENT_DTO);
        }

        Course course = courseRepository.findById(enrollmentDTO.getCourseId())
                .orElseThrow(() -> new AppException(ErrorConstant.COURSE_NOT_FOUND));

        if (enrollmentDTO.getStartTime() == null || enrollmentDTO.getEndTime() == null) {
            throw new AppException(ErrorConstant.INVALID_TIME_RANGE);
        }

        if (enrollmentDTO.getStartTime().after(enrollmentDTO.getEndTime())) {
            throw new AppException(ErrorConstant.INVALID_TIME_RANGE);
        }

        if (enrollmentDTO.getAvailable() == null || enrollmentDTO.getAvailable() <= 0) {
            throw new AppException(ErrorConstant.INVALID_AVAILABLE);
        }

        LocalDateTime start = enrollmentDTO.getStartTime().toLocalDateTime();
        LocalDateTime end = enrollmentDTO.getEndTime().toLocalDateTime();

        boolean isDuplicate = enrollmentRepository.existsByCourseIdAndTimeOverlap(
                enrollmentDTO.getCourseId(), start, end);

        if (isDuplicate) {
            throw new AppException(ErrorConstant.DUPLICATE_ENROLLMENT);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setAvailable(enrollmentDTO.getAvailable());
        enrollment.setStartTime(start);
        enrollment.setEndTime(end);
        enrollment.setCourse(course);
        enrollment.setRegistered(0);
        enrollment.setLockWhenFull(enrollmentDTO.isLockWhenFull());
        enrollmentRepository.save(enrollment);
    }

    @Override
    public List<UserDTO> getAllStudents(String className) {
        List<UserDTO> userDTOS = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        String sql = switch (className) {
            case "Công nghệ thông tin" -> "select u.user_id    as id, " +
                    "       u.email, " +
                    "       u.avatar, " +
                    "       u.created_at as created, " +
                    "       u.fullname, " +
                    "       CASE " +
                    "           WHEN ru.roleid = 1 " +
                    "               THEN 'Sinh viên' " +
                    "           ELSE 'Chưa có quyền' " +
                    "           END      as roleName " +
                    "from usertbl u " +
                    "         join roleusertbl ru on u.user_id = ru.userid " +
                    "where ru.roleid = 1 and u.class_id = 1";
            case "An toàn thông tin" -> "select u.user_id    as id, " +
                    "       u.email, " +
                    "       u.avatar, " +
                    "       u.created_at as created, " +
                    "       u.fullname, " +
                    "       CASE " +
                    "           WHEN ru.roleid = 1 " +
                    "               THEN 'Sinh viên' " +
                    "           ELSE 'Chưa có quyền' " +
                    "           END      as roleName " +
                    "from usertbl u " +
                    "         join roleusertbl ru on u.user_id = ru.userid " +
                    "where ru.roleid = 1 and u.class_id = 2";
            case "Điện tử viễn thông" -> "select u.user_id    as id, " +
                    "       u.email, " +
                    "       u.avatar, " +
                    "       u.created_at as created, " +
                    "       u.fullname, " +
                    "       CASE " +
                    "           WHEN ru.roleid = 1 " +
                    "               THEN 'Sinh viên' " +
                    "           ELSE 'Chưa có quyền' " +
                    "           END      as roleName " +
                    "from usertbl u " +
                    "         join roleusertbl ru on u.user_id = ru.userid " +
                    "where ru.roleid = 1 and u.class_id = 3";
            default -> "";
        };
        Query query = entityManager.createNativeQuery(sql, "UserDTO");
        setParams(query, params);
        userDTOS = query.getResultList();
        return userDTOS;
    }

    @Override
    public UserDTO getTeacherByID(Integer id) {
        return userRepository.getTeacherByID(id);
    }

    @Override
    public List<UserDTO> getListUserFromEnrollment(Integer enrollId) {
        return userRepository.getListUserFromEnrollment(enrollId);
    }

    public static void setParams(Query query, Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            Set<Map.Entry<String, Object>> set = params.entrySet();
            for (Map.Entry<String, Object> obj : set) {
                if (obj.getValue() == null) query.setParameter(obj.getKey(), "");
                else query.setParameter(obj.getKey(), obj.getValue());
            }
        }
    }
}
