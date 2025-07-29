package lms.doantotnghiep.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lms.doantotnghiep.domain.Enrollment;
import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import lms.doantotnghiep.repository.CourseRepository;
import lms.doantotnghiep.repository.EnrollmentRepository;
import lms.doantotnghiep.repository.UserRepository;
import lms.doantotnghiep.service.EnrollmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    @PersistenceContext(unitName = "entityManagerFactory")
    private EntityManager entityManager;

    @Override
    public List<EnrollmentDTO> getAllEnrollments(Integer classId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImple userDetails = (UserDetailsImple) authentication.getPrincipal();
        List<EnrollmentDTO> enrollmentDTOList = new ArrayList<>();
        enrollmentDTOList = enrollmentRepository.getAllEnrollments(classId, userDetails.getId());
        enrollmentDTOList.forEach(enrollmentDTO -> {
            enrollmentDTO.setCourse(courseRepository.findCourseByID(enrollmentDTO.getCourseId()));
        });
        return enrollmentDTOList;
    }

    @Override
    public void registerEnrollment(List<Integer> enrollIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImple userDetails = (UserDetailsImple) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findByEmail(userDetails.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            for (Integer id : enrollIds) {
                Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
                if (enrollment.isPresent()) {
                    Enrollment enrollment1 = enrollment.get();
                    if (!enrollment1.isLocked()) {
                        enrollment1.setRegistered(enrollment1.getRegistered() + 1);
                        if (Objects.equals(enrollment1.getRegistered(), enrollment1.getAvailable())) {
                            enrollment1.setLocked(true);
                        }
                        enrollmentRepository.save(enrollment1);
                        user.getEnrollments().add(enrollment1);
                    } else {
                        throw new AppException(ErrorConstant.REGISTER_INVALID);
                    }
                }
            }
            userRepository.save(user);
        } else {
            throw new AppException(ErrorConstant.UNAUTHENTICATED);
        }

    }

    @Override
    public List<EnrollmentDTO> getEnrollmentFilter(String type, Integer userId) {
        List<EnrollmentDTO> enrollmentDTOS;
        Map<String, Object> params = new HashMap<>();
        String sql = "";
        if (type.equals("Chưa đăng ký")) {
            sql = "select e.enrollid   as enrollId, " +
                    "       e.available, " +
                    "       e.registered, " +
                    "       e.start_time as startTime, " +
                    "       e.end_time   as endTime, " +
                    "       e.course_id  as courseId, " +
                    "       e.locked, " +
                    "       c.user_id as userId " +
                    "from enrollmenttbl e " +
                    "         join dbo.coursetbl c on e.course_id = c.course_id " +
                    "         join usertbl u on u.class_id = c.class_id " +
                    "         join classtbl cl on cl.class_id = c.class_id " +
                    "         left join enrollmentusertbl eu ON eu.enrollid = e.enrollid AND eu.userid = u.user_id " +
                    "where c.class_id = u.class_id and u.user_id = :id AND eu.enrollid IS NULL;";

        } else if (type.equals("Đã đăng ký")) {
            sql = "select e.enrollid as enrollId, " +
                    "       e.available, " +
                    "       e.registered, " +
                    "       e.start_time as startTime, " +
                    "       e.end_time   as endTime, " +
                    "       e.course_id  as courseId, " +
                    "       e.locked, " +
                    "       c.user_id as userId " +
                    "from enrollmenttbl e " +
                    "         join dbo.coursetbl c on e.course_id = c.course_id " +
                    "         join usertbl u on u.class_id = c.class_id " +
                    "         join classtbl cl on cl.class_id = c.class_id " +
                    "         join enrollmentusertbl eu ON eu.enrollid = e.enrollid AND eu.userid = u.user_id " +
                    "where c.class_id = u.class_id and u.user_id = :id";
        }
        params.put("id", userId);
        Query query = entityManager.createNativeQuery(sql, "EnrollmentDTO");
        setParams(query, params);
        enrollmentDTOS = query.getResultList();
        enrollmentDTOS.forEach(enrollmentDTO -> {
            enrollmentDTO.setCourse(courseRepository.findCourseByID(enrollmentDTO.getCourseId()));
        });
        return enrollmentDTOS;
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