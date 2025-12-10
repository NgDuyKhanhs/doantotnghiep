package lms.doantotnghiep.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorConstant {
    USERNAME_IS_USED(1, "Tên người dùng đã được sử dụng", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME_PASSWORD(2, "Tên người dùng hoặc mật khẩu không chính xác", HttpStatus.BAD_REQUEST),
    EMAIL_IS_USED(3, "Email đã được sử dụng", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EXISTED(4, "Email không tồn tại", HttpStatus.BAD_REQUEST),
    EMAIL_IS_IN_PROCESS(5, "Email đang được xử lý, vui lòng thử lại", HttpStatus.BAD_REQUEST),
    PHONE_IS_USED(6, "Số điện thoại đã được sử dụng", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN_TYPE(7, "Loại token không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(8, "Tên người dùng không tồn tại", HttpStatus.BAD_REQUEST),
    USER_ACTIVATED(9, "Người dùng đã được kích hoạt", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_EXISTED(10, "Địa chỉ không tồn tại", HttpStatus.BAD_REQUEST),
    PASSWORDS_NOT_MATCH(11, "Mật khẩu và xác nhận mật khẩu không khớp", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_EXIST(14, "Tài khoản không tồn tại", HttpStatus.BAD_REQUEST),
    USER_ALREADY_FOLLOWED(15, "Người dùng đã được theo dõi", HttpStatus.BAD_REQUEST),
    FOLLOW_NOT_EXIST(16, "Theo dõi không tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXIST(17,"Quyền không tồn tại", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(1999, "Lỗi không phân loại", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1007, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1008, "Bạn không có quyền truy cập", HttpStatus.FORBIDDEN),
    CAPTCHA_INVALID(12, "Captcha không hợp lệ", HttpStatus.BAD_REQUEST),
    FAIL_UPLOAD_CLOUD(13, "Lỗi khi tải lên đám mây", HttpStatus.BAD_REQUEST),
    FILE_ERROR(25, "File lỗi", HttpStatus.BAD_REQUEST),
    REGISTER_INVALID(21, "Bạn không có quyền đăng ký môn học này nữa!", HttpStatus.BAD_REQUEST),
    FILE_JS(25, "File chứa JS không thể tải lên", HttpStatus.BAD_REQUEST),
    INVALID_ENROLLMENT_DTO(14, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    COURSE_NOT_FOUND(15, "Không tìm thấy khóa học", HttpStatus.NOT_FOUND),
    INVALID_TIME_RANGE(16, "Thời gian bắt đầu phải trước thời gian kết thúc", HttpStatus.BAD_REQUEST),
    INVALID_AVAILABLE(17, "Số lượng chỗ còn lại phải lớn hơn 0", HttpStatus.BAD_REQUEST),
    DUPLICATE_ENROLLMENT(18, "Khóa học đã tồn tại", HttpStatus.BAD_REQUEST),
    PERMISSION_DENIED(18, "Người dùng không có quyền truy cập", HttpStatus.BAD_REQUEST);
    ErrorConstant(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
