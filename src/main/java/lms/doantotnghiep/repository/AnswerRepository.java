package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findBySubmissionId(int submissionId);
}