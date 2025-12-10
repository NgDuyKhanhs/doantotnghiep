package lms.doantotnghiep.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lms.doantotnghiep.domain.Answer;
import lms.doantotnghiep.dto.request.AnswersJson;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ExamSession implements Serializable {
    private int userId;
    private int assignmentId;
    private int submissionId;
    private String startTime;
    private long remainingSeconds;
    private List<Answer> answers;

    // Lưu tạm
    private List<AnswersJson> answersJson = new ArrayList<>();
    // getters, setters, constructor, toString...
}