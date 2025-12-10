package lms.doantotnghiep.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateViolationRequest {
    private Integer assignmentID;       // bắt buộc
    private String typeViolation;   // ví dụ: PRINT_SCREEN, EXIT_FULLSCREEN, VISIBILITY_HIDDEN
    private String description;     // mô tả thêm (kèm IP, userAgent...)
    private String evidence;        // bằng chứng (optional: base64/s3 url/ghi chú)

}