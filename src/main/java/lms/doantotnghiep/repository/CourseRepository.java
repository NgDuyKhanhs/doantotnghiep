package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.dto.CourseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface CourseRepository extends CrudRepository<Course, Integer> {
    @Query(value = "select course_id as courseId, name, credits from coursetbl where course_id = ?1", nativeQuery = true)
    CourseDTO findCourseByID(Integer courseId);

    @Query(value = "select c.course_id as courseId, c.name, c.credits, cl.name as className, c.banner, c.user_id as userId " +
            "from coursetbl c " +
            "         left join classtbl cl on cl.class_id = c.class_id " +
            "         left join usertbl u on u.user_id = c.user_id", nativeQuery = true)
    List<CourseDTO> getAllCourse();


}
