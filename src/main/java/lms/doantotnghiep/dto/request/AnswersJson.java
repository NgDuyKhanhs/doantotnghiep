package lms.doantotnghiep.dto.request;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnswersJson {
    private int questionId;
    private int selectedChoiceId;
}
