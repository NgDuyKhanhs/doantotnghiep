package lms.doantotnghiep.dto.request;

import lms.doantotnghiep.dto.CourseDTO;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadEnrollmentReq {
    private CourseDTO courseDTO;
    private UserDTO userDTO;
    private EnrollmentDTO enrollmentDTO;
}
