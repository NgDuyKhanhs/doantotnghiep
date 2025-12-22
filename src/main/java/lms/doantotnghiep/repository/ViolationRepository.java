package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.ViolationReport;
import lms.doantotnghiep.dto.ViolationReportDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ViolationRepository extends CrudRepository<ViolationReport, Integer> {
    @Query(value = "SELECT v.* FROM violationtbl v " +
            "            WHERE v.user_id = ?1 " +
            "            AND v.assignment_id = ?2 " +
            "            ORDER BY v.created_at DESC", nativeQuery = true)
    List<ViolationReport> findByUserIdAndAssignmentIdd(int userId, int assignmentId);

    @Query(value = "SELECT v.* FROM violationtbl v\n" +
            "            WHERE v.user_id = ?1\n" +
            "            AND v.assignment_id = ?2\n" +
            "            ORDER BY v.created_at DESC", nativeQuery = true)
    List<ViolationReport> findByUserIdAndAssignmentId(int userId, int assignmentId);

    @Query(value = "select distinct a.title, v.created_at, v.type_violation, s.score from violationtbl v join assignmenttbl a on v.assignment_id = a.assignment_id join submissiontbl s on s.assignment_id = a.assignment_id and v.user_id = s.user_id where v.user_id = ?1", nativeQuery = true)
    List<ViolationReportDTO> findByUserId(int userId);
}
