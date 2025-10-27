package lms.doantotnghiep.dto;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Embeddable
public class PdfDTO {
    @Column(name = "pdf_url")
    private String pdfFile;

    @Column(name = "name_file",columnDefinition = "NVARCHAR(255)")
    private String nameFile;

    @Column(name = "created_at")
    private Timestamp createdAt;

    public PdfDTO(String pdfFile, String nameFile, Timestamp createdAt) {
        this.pdfFile = pdfFile;
        this.nameFile = nameFile;
        this.createdAt = createdAt;
    }

    public PdfDTO() {}
}
