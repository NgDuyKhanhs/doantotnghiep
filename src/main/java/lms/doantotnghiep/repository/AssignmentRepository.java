package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.dto.AssignmentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    @Query(value = "select a.* from assignmenttbl a join enrollmenttbl e on a.enrollid = e.enrollid where e.enrollid = ?1", nativeQuery = true)
    List<Assignment> findByEnrollment_Id(int enrollId);

    Assignment findById(int id);
}
