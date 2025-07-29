package lms.doantotnghiep.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Builder
@Data
public class ResponseLoginDTO {
    private Integer userId;
    private String accessToken;
    private ResponseCookie refreshCookie;
}
