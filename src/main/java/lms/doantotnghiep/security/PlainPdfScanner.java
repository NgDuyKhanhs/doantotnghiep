package lms.doantotnghiep.security;

import lms.doantotnghiep.dto.response.ScanResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class PlainPdfScanner {

    private static final String[] IMMEDIATE_DANGEROUS_TOKENS = new String[] {
            "/JS", "/JavaScript", "/OpenAction",
            "/EmbeddedFiles", "/Names/EmbeddedFiles",
            "/RichMedia", "/Launch", "/XFA",
            "/Encrypt"
    };

    private static final String[] AA_CONTEXT_TOKENS = new String[] {
            "/javascript", "/js", "/launch", "/openaction", "/uri", "/s /javascript"
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

        if (bytes == null) {
            result.addReason("Input is null");
            return result;
        }
        if (bytes.length == 0) {
            result.addReason("Empty file");
            return result;
        }

        int maxWindow = Math.min(bytes.length, 5 * 1024 * 1024);
        String textLower;
        try {
            String text = new String(bytes, 0, maxWindow, StandardCharsets.ISO_8859_1);
            int trimEnd = text.length();
            while (trimEnd > 0 && (text.charAt(trimEnd - 1) == '\0' || Character.isWhitespace(text.charAt(trimEnd - 1)))) {
                trimEnd--;
            }
            if (trimEnd != text.length()) {
                text = text.substring(0, trimEnd);
            }
            textLower = text.toLowerCase(Locale.ROOT);
        } catch (Exception e) {
            result.addReason("Failed to decode bytes: " + e.getMessage());
            return result;
        }

        // 1) Các token gây nguy hiểm xử lý như trước
        for (String token : IMMEDIATE_DANGEROUS_TOKENS) {
            if (textLower.contains(token.toLowerCase(Locale.ROOT))) {
                result.addReason("Found token: " + token);
                result.addDetail(token, "present");
            }
        }

        // 2) Xử lý /AA theo ngữ cảnh — không block nếu không phát hiện hành động thực thi
        String aaToken = "/aa";
        int idx = textLower.indexOf(aaToken);
        boolean aaFound = false;
        boolean aaContextDanger = false;
        while (idx >= 0) {
            aaFound = true;
            // lấy vùng ngữ cảnh quanh /AA (ví dụ 2 KB trước và sau)
            int ctxRadius = 2048;
            int start = Math.max(0, idx - ctxRadius);
            int end = Math.min(textLower.length(), idx + aaToken.length() + ctxRadius);
            String ctx = textLower.substring(start, end);

            // tìm token nguy hiểm trong vùng ngữ cảnh
            for (String ctxTok : AA_CONTEXT_TOKENS) {
                if (ctx.contains(ctxTok)) {
                    aaContextDanger = true;
                    result.addReason("Found /AA with dangerous action in context: " + ctxTok);
                    result.addDetail("/AA", "context:" + ctxTok);
                    break;
                }
            }
            if (!aaContextDanger) {
                // không thấy action nguy hiểm trong vùng quanh lần xuất hiện này
                // chỉ ghi detail (không addReason), để giảm false-positive
                result.addDetail("/AA", "present_no_action_detected");
            } else {
                // nếu đã phát hiện context nguy hiểm, ta có thể dừng (hoặc continue để tìm thêm)
                // break; // comment nếu muốn tìm nhiều instance
            }

            idx = textLower.indexOf(aaToken, idx + aaToken.length());
        }

        if (!aaFound) {
            result.addDetail("/AA", "absent");
        }

        // 3) Phát hiện object/stream
        int objCount = countRegexOccurrences(textLower, "(?m)\\b\\d+\\s+\\d+\\s+obj\\b");
        int streamCount = countRegexOccurrences(textLower, "(?m)\\bstream\\b");
        result.addDetail("objects", String.valueOf(objCount));
        result.addDetail("streams", String.valueOf(streamCount));
        if (objCount == 0) {
            objCount = countRegexOccurrences(textLower, "\\bobj\\b");
            result.addDetail("objects_fallback", String.valueOf(objCount));
        }

        // 4) AcroForm/XFA detection
        if (textLower.contains("/acroform")) {
            result.addDetail("AcroForm", "present");
            result.addReason("Contains AcroForm (potential interactive form)");
        }

        // 5) Heuristic obfuscation check
        int pdfKeywordScore = keywordScore(textLower);
        result.addDetail("keyword_score", String.valueOf(pdfKeywordScore));
        if (pdfKeywordScore < 2) {
            result.addReason("Low PDF keyword score (possible obfuscation)");
            result.addDetail("obfuscation_hint", "low-score");
        }

        return result;
    }

    private int countRegexOccurrences(String text, String regex) {
        try {
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher m = p.matcher(text);
            int count = 0;
            while (m.find()) count++;
            return count;
        } catch (Exception e) {
            return 0;
        }
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