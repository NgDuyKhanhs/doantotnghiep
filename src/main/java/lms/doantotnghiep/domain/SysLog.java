package lms.doantotnghiep.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "syslogtbl")
public class  SysLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "syslogId")
    private Integer id;
    private LocalDateTime startTime;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String action;
    private Integer status;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String ipAddress;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "description",columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(name = "name_device",columnDefinition = "NVARCHAR(255)")
    private String nameDevice;

}
