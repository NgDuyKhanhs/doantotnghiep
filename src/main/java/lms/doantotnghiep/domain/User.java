package lms.doantotnghiep.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "usertbl")
@SqlResultSetMappings(
        {
                @SqlResultSetMapping(
                        name = "UserDTO",
                        classes = {
                                @ConstructorResult(
                                        targetClass = UserDTO.class,
                                        columns = {
                                                @ColumnResult(name = "id", type = Integer.class),
                                                @ColumnResult(name = "email", type = String.class),
                                                @ColumnResult(name = "avatar", type = String.class),
                                                @ColumnResult(name = "created", type = Timestamp.class),
                                                @ColumnResult(name = "fullname", type = String.class),
                                                @ColumnResult(name = "roleName", type = String.class)
                                        }
                                ),
                        }
                ),
        }
)
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer id;

    @Column(name = "email", columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String fullname;

    @Column(name = "active")
    private boolean active;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String avatar;

    private LocalDate birth;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "roleusertbl",
            joinColumns = @JoinColumn(name = "userID", referencedColumnName = "userId"),
            inverseJoinColumns = @JoinColumn(name = "roleID", referencedColumnName = "roleId"))
    private Set<Role> roles = new HashSet<>();


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "enrollmentusertbl",
            joinColumns = @JoinColumn(name = "userID", referencedColumnName = "userId"),
            inverseJoinColumns = @JoinColumn(name = "enrollID", referencedColumnName = "enrollid"))
    private Set<Enrollment> enrollments  = new HashSet<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private Class classId;


    @OneToMany(mappedBy = "teacher")
    private List<Course> coursesTaught;
}