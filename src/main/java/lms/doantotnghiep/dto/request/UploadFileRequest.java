package lms.doantotnghiep.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileRequest {

    private Integer courseId;


    @NotNull(message = "Ảnh là bắt buộc")
    private List<MultipartFile> images;

    private List<MultipartFile> videos;
}
