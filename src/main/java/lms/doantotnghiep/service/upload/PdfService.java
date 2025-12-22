package lms.doantotnghiep.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.utils.ObjectUtils;
import lms.doantotnghiep.dto.PdfDTO;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import lms.doantotnghiep.repository.EnrollmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfService {

    final Cloudinary cloudinary;
    final EnrollmentRepository enrollmentRepository;

    /**
     * Upload PDF file lên Cloudinary
     */
    public String uploadPdf(MultipartFile file, String publicId) {
        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",   // giữ dạng raw cho PDF
                            "public_id", publicId,
                            "format", "pdf"           // ép lưu với đuôi .pdf
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            log.error("Upload PDF failed", e);
            throw new AppException(ErrorConstant.FAIL_UPLOAD_CLOUD);
        }
    }

    /**
     * Upload PDF từ base64
     */
    public String getUrlPdf(String base64Pdf) {
        String uuid = UUID.randomUUID().toString();
        MultipartFile multipartFile = Base64ToMultipartFileConverter.convert(base64Pdf);
        return uploadPdf(multipartFile, "Pdf_" + uuid);
    }


    /**
     * Lấy public_id từ URL Cloudinary
     */
    private String getPublicId(String url) {
        String[] parts = url.split("/");
        String fileNameWithExt = parts[parts.length - 1]; // Ví dụ: Pdf_123abc.pdf
        return fileNameWithExt.split("\\.")[0];           // Trả về "Pdf_123abc"
    }

    /**
     * Xóa PDF trên Cloudinary
     */
    public void deletePdf(String pdfUrl) {
        try {
            String publicId = getPublicId(pdfUrl);
            Map<String, Object> params = ObjectUtils.asMap("resource_type", "raw");
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, params);
            log.info("Delete PDF result: {}", result);
        } catch (Exception e) {
            log.error("Failed to delete PDF", e);
        }
    }

    String normalizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9-_\\.]", "_")
                .replace(" ", "_");
        return normalized;
    }

    public String uploadPdfBytes(byte[] pdfBytes, String fileName) {
        try {
            String safeFileName = normalizeFileName(fileName) + "_" + System.currentTimeMillis() + ".pdf";

            Map uploadResult = cloudinary.uploader().upload(pdfBytes,
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "type", "private",
                            "public_id", safeFileName,
                            "access_control", List.of(
                                    ObjectUtils.asMap(
                                            "access_type", "token"
                                    )
                            )
                    ));

            return (String) uploadResult.get("public_id");

        } catch (IOException e) {
            throw new RuntimeException("Upload PDF thất bại", e);
        }
    }

    public String generateSignedUrl(String publicId) {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            // 10 phút
            long expirationTimestamp = vietnamTime.toEpochSecond() + 60; //
            return cloudinary.privateDownload(
                    publicId,
                    "pdf",
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "expires_at", expirationTimestamp
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Generate URL thất bại: " + e.getMessage(), e);
        }
    }

}
