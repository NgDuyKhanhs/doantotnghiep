package lms.doantotnghiep.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Time;
import java.sql.Timestamp;
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
    private String roleName;
    private Timestamp created;
    private String avatar;
    private Integer id;

    public UserDTO(Integer id,String email, String fullname, String avatar){
        this.id = id;
        this.email = email;
        this.fullname = fullname;
        this.avatar = avatar;
    }
    public UserDTO(Integer id, String email, String avatar, Timestamp created, String fullname, String roleName){
        this.id = id;
        this.email = email;
        this.avatar = avatar;
        this.created = created;
        this.fullname = fullname;
        this.roleName = roleName;
    }
}
