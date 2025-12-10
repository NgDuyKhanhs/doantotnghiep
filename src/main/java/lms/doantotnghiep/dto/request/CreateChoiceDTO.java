package lms.doantotnghiep.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChoiceDTO {
    private String text;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
}