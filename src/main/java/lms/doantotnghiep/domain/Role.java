package lms.doantotnghiep.domain;


import jakarta.persistence.*;
import lms.doantotnghiep.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roletbl")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roleId")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "roleName")
    private RoleType roleName;

    public Role(RoleType roleName) {
        this.roleName = roleName;
    }
}
