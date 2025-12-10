package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AnswerRepository extends CrudRepository<Answer, Integer> {
    List<Answer> findBySubmissionId(int submissionId);

    @Modifying
    @Query(value = "DELETE FROM answer WHERE submission_id = ?1", nativeQuery = true)
    void deleteBySubmissionId(int submissionId);


    @Query(value = "SELECT a.* FROM answer a WHERE a.submission_id = ?1", nativeQuery = true)
    List<Answer> findBySubmissionIdWithDetails(int submissionId);

    @Query(value = "SELECT COUNT(*) FROM Answer a WHERE a.submission_id = ?1 AND a.is_correct = 1", nativeQuery = true)
    int countCorrectBySubmissionId(int submissionId);
}