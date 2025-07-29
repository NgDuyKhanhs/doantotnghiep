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
@Table(name = "violationtbl")
public class ViolationReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vioId")
    private int id;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String typeViolation;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;
    @Column(columnDefinition = "NVARCHAR(255)")
    private String evidence;
    private LocalDateTime createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
}
