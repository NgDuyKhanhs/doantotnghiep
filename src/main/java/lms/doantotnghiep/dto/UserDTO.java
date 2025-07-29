package lms.doantotnghiep.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotBlank
    private String email;
    private String fullname;
    @NotBlank
    private String password;
    @NotBlank
    private Integer classId;
    private String confirmPassword;

    public UserDTO(String email, String fullname){
        this.email = email;
        this.fullname = fullname;
    }
}
