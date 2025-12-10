package lms.doantotnghiep.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.domain.Choice;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String text;

    @Column(name = "correct_choice_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int correctChoiceId;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Choice> choices;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignmentId")
    private Assignment assignment;

    @JsonIgnore
    public int getCorrectChoiceId() { return correctChoiceId; }
}
