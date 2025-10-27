package lms.doantotnghiep.dto.response;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class PdfResponse {
    private Integer idEnroll;
    private String pdfFile;
    private String nameFile;
    private Timestamp createdAt;

    public PdfResponse(Integer idEnroll, String pdfFile, String nameFile, Timestamp createdAt) {
        this.idEnroll = idEnroll;
        this.pdfFile = pdfFile;
        this.nameFile = nameFile;
        this.createdAt = createdAt;
    }

    public PdfResponse() {
    }
}