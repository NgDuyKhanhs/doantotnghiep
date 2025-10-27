package lms.doantotnghiep.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionDTO {
    private String text;
    private int correctChoiceId;
    private List<CreateChoiceDTO> choices;
}