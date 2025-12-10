package lms.doantotnghiep.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.domain.ViolationReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionHistoryResponse {
    private Integer submissionId;
    private String status;
    private Double score;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    private Long spentSeconds;
    private Integer totalQuestions;
    private Integer correctCount;
    private List<ViolationReport> violations;
}