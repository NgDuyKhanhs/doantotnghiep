package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Enrollment;
import lms.doantotnghiep.dto.EnrollmentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface EnrollmentRepository extends CrudRepository<Enrollment, Integer> {
    @Query(value = "select e.enrollid as enrollId, " +
            "                   e.available, " +
            "                   e.registered, " +
            "                   e.start_time as startTime, " +
            "                   e.end_time as endTime, " +
            "                   e.course_id as courseId, " +
            "                   e.locked, " +
            "                   c.user_id as userId, " +
            "                   cl.name as className" +
            "            from enrollmenttbl e " +
            "                     join dbo.coursetbl c on e.course_id = c.course_id " +
            "                     join classtbl cl on cl.class_id = c.class_id", nativeQuery = true)
    List<EnrollmentDTO> getAllEnrollments(Integer classId, Integer userId);
}
