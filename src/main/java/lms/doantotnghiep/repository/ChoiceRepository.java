package lms.doantotnghiep.repository;
import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.domain.Choice;
import lms.doantotnghiep.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface ChoiceRepository extends CrudRepository<Choice, Integer> {
    List<Choice> findByQuestion(Question question);

    // Lấy đáp án đúng cho nhiều câu hỏi (giả định field isCorrect trên Choice)
    @Query(value = "SELECT c.* FROM choices c WHERE c.question_id IN :questionIds AND c.is_correct = true", nativeQuery = true)
    List<Choice> findCorrectChoicesByQuestionIds(Set<Integer> questionIds);

    // Load các lựa chọn theo id (để set vào Answer)
    List<Choice> findByIdIn(Collection<Integer> ids);
}