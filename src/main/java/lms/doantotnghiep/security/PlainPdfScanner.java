package lms.doantotnghiep.security;

import lms.doantotnghiep.dto.response.ScanResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;


@Component
public class PlainPdfScanner {

    // Các token nguy hiểm phổ biến (case-insensitive)
    private static final String[] DANGEROUS_TOKENS = new String[] {
            "/JS", "/JavaScript", "/OpenAction", "/AA",
            "/EmbeddedFiles", "/Names/EmbeddedFiles",
            "/RichMedia", "/Launch", "/XFA",
            "/Encrypt" // PDF có mã hóa: có thể chứa nội dung khó kiểm soát
    };

    private static final Pattern PDF_HEADER = Pattern.compile("^%PDF-", Pattern.CASE_INSENSITIVE);

    public boolean hasPdfHeader(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return false;
        int len = Math.min(bytes.length, 8);
        String head = new String(bytes, 0, len, StandardCharsets.US_ASCII);
        return PDF_HEADER.matcher(head).find();
    }

    public ScanResult scan(byte[] bytes) {
        ScanResult result = new ScanResult();

        // 1) Chuẩn hóa: cắt đầu cuối \0 để tránh rác; không cần decode toàn bộ nếu file rất lớn
        // Đơn giản: tạo một chuỗi lowercase từ tối đa N MB đầu (ví dụ 5MB) để tìm token
        int window = Math.min(bytes.length, 5 * 1024 * 1024);
        String textLower = new String(bytes, 0, window, StandardCharsets.ISO_8859_1).toLowerCase(Locale.ROOT);

        // 2) Tìm token nguy hiểm
        for (String token : DANGEROUS_TOKENS) {
            if (textLower.contains(token.toLowerCase(Locale.ROOT))) {
                result.addReason("Found token: " + token);
                result.addDetail(token, "present");
            }
        }

        // 3) Phát hiện object streams (có thể chứa JS/Actions) - chỉ đánh dấu thông tin
        int objCount = countOccurrences(textLower, "obj");
        int streamCount = countOccurrences(textLower, "stream");
        result.addDetail("objects", String.valueOf(objCount));
        result.addDetail("streams", String.valueOf(streamCount));

        // 4) Kiểm tra dấu hiệu form XFA/AcroForm
        if (textLower.contains("/acroform")) {
            result.addDetail("AcroForm", "present");
        }

        // 5) Kiểm tra nếu có hành động mở tài liệu (OpenAction) hoặc Additional Actions (AA) đã đánh dấu ở trên
        // 6) Nếu phát hiện /Encrypt: coi là nguy hiểm (tùy policy)
        if (textLower.contains("/encrypt")) {
            result.addReason("Encrypted PDF");
            result.addDetail("Encrypt", "present");
        }

        // 7) Kiểm tra sự hiện diện của các ký hiệu JavaScript theo pattern đơn giản
        // Tìm (JS) hoặc (JavaScript) trong phần dictionaries/objects
        // Dùng heuristics: nếu có /JavaScript hoặc /JS -> đánh dấu nguy hiểm (done ở 2)

        // 8) Kịch bản né tránh: nếu dữ liệu chủ yếu binary và hầu như không có từ khóa PDF, thêm cảnh báo
        int pdfKeywordScore = keywordScore(textLower);
        result.addDetail("keyword_score", String.valueOf(pdfKeywordScore));
        if (pdfKeywordScore < 2) {
            result.addReason("Low PDF keyword score (possible obfuscation)");
            result.addDetail("obfuscation_hint", "low-score");
        }

        return result;
    }

    private int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    private int keywordScore(String textLower) {
        int score = 0;
        if (textLower.contains("/catalog")) score++;
        if (textLower.contains("/pages")) score++;
        if (textLower.contains("/page")) score++;
        if (textLower.contains("/header")) score++; // hiếm, nhưng thêm cho đa dạng
        if (textLower.contains("/type")) score++;
        if (textLower.contains("/root")) score++;
        return score;
    }
}