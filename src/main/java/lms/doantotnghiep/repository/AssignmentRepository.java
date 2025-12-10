package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.dto.AssignmentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    @Query(value = "SELECT a.* " +
            "FROM assignmenttbl a " +
            "JOIN enrollmenttbl e ON a.enrollid = e.enrollid " +
            "WHERE e.enrollid = ?1 " +
            "  AND NOT EXISTS ( " +
            "    SELECT 1 " +
            "    FROM submissiontbl s " +
            "    WHERE s.assignment_id = a.assignment_id " +
            "      AND s.user_id = ?2 " +
            "  )", nativeQuery = true)
    List<Assignment> findUnsubmittedByEnrollID(int enrollId, int userId);


    @Query(value = "SELECT DISTINCT a.* " +
            "FROM assignmenttbl a " +
            "JOIN enrollmenttbl e ON a.enrollid = e.enrollid " +
            "JOIN submissiontbl s ON s.assignment_id = a.assignment_id " +
            "    AND s.user_id = ?1 " +
            "WHERE e.enrollid = ?2", nativeQuery = true)
    List<Assignment> findSubmittedByEnrollID(int userId, int enrollId);
    Assignment findById(int id);
}
