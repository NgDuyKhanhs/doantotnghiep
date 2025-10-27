package lms.doantotnghiep.repository;
import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.domain.Choice;
import lms.doantotnghiep.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoiceRepository extends JpaRepository<Choice, Integer> {
    List<Choice> findByQuestion(Question question);
}