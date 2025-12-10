package lms.doantotnghiep.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private int id;
    private String text;
    @JsonIgnore
    private int correctChoiceId; // Index của lựa chọn đúng
    private int assignmentId;
    private List<ChoiceDTO> choices;
}