package lms.doantotnghiep.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubmissionResponse {
    private Integer submissionId;
    private Integer userId;
    private String studentName;
    private String studentEmail;
    private String status;
    private Double score;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private Long spentSeconds;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer violationCount;

}