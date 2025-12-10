package lms.doantotnghiep.dto.response;

import lombok.Data;

@Data
public class SubmitResult {
    private Integer submissionId;
    private Integer totalQuestions;
    private Integer correctCount;
    private Double score;
    private String submittedAt;

    public SubmitResult() {
    }

    public SubmitResult(Integer submissionId,
                        Integer totalQuestions,
                        Integer correctCount,
                        Double score,
                        String submittedAt) {
        this.submissionId = submissionId;
        this.totalQuestions = totalQuestions;
        this.correctCount = correctCount;
        this.score = score;
        this.submittedAt = submittedAt;
    }

}