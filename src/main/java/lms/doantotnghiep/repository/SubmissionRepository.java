package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    @Query(value =  "SELECT * FROM submissiontbl s WHERE s.user_id = ?1 AND s.assignment_id = ?2 AND s.status = 'IN_PROGRESS'", nativeQuery = true)
    Optional<Submission> findActiveSubmission(int userId, int assignmentId);
}
