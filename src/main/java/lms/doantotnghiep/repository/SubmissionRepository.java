package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    @Query(value =  "SELECT * FROM submissiontbl s WHERE s.user_id = ?1 AND s.assignment_id = ?2 AND s.status = 'IN_PROGRESS'", nativeQuery = true)
    Optional<Submission> findActiveSubmission(int userId, int assignmentId);

    @Query(value = "SELECT s.* " +
            "FROM submissiontbl s " +
            "            WHERE s.user_id = ?1 " +
            "            AND s.assignment_id = ?2 " +
            "            AND s.status = 'IN_PROGRESS' " +
            "            ORDER BY s.start_time DESC", nativeQuery = true)
    Optional<Submission> findActiveByUserIdAndAssignmentId(int userId, int assignmentId);


    @Query(value = "SELECT s.* FROM submissiontbl s " +
            "            WHERE s.user_id = ?1 " +
            "            AND s.assignment_id = ?2 " +
            "            ORDER BY s.start_time DESC", nativeQuery = true)
    Optional<Submission> findLatestByUserIdAndAssignmentId(int userId, int assignmentId);

    @Query(value = "SELECT s.* FROM submissiontbl s " +
            "            WHERE s.assignment_id = ?1 " +
            "            ORDER BY s.start_time DESC", nativeQuery = true)
    List<Submission> findAllByAssignmentId(int assignmentId);

    // Lấy submission theo status (không phân trang)
    @Query(value = "SELECT s.* FROM submissiontbl s  \n" +
            "            WHERE s.assignment_id = ?1  \n" +
            "            AND (:status IS NULL OR s.status = ?2)  \n" +
            "            ORDER BY s.start_time DESC", nativeQuery = true)
    List<Submission> findByAssignmentIdAndStatus( int assignmentId,String status);

}
