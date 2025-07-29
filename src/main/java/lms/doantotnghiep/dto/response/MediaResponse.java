package lms.doantotnghiep.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class MediaResponse {

    private Integer id;

    private String url;

    private String type; // "IMAGE" or "VIDEO"

    private Integer courseId;

    public MediaResponse(Integer id, String url, String type, Integer courseId) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.courseId = courseId;
    }

}
