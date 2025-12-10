package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends CrudRepository<Question, Integer> {
    List<Question> findByAssignment(Assignment assignment);

    @Query(value = "SELECT q.* FROM questions q WHERE q.assignment_id = ?1 ORDER BY q.id ASC", nativeQuery = true)
    List<Question> findByAssignmentId(int assignmentId);
}