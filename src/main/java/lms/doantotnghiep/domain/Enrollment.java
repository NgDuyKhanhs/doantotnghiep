package lms.doantotnghiep.domain;

import jakarta.persistence.*;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.PdfDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "enrollmenttbl")

@SqlResultSetMappings(
        {
                @SqlResultSetMapping(
                        name = "EnrollmentDTO",
                        classes = {
                                @ConstructorResult(
                                        targetClass = EnrollmentDTO.class,
                                        columns = {
                                                @ColumnResult(name = "enrollId", type = int.class),
                                                @ColumnResult(name = "available", type = Integer.class),
                                                @ColumnResult(name = "registered", type = Integer.class),
                                                @ColumnResult(name = "startTime", type = Timestamp.class),
                                                @ColumnResult(name = "endTime", type = Timestamp.class),
                                                @ColumnResult(name = "courseId", type = Integer.class),
                                                @ColumnResult(name = "locked", type = boolean.class),
                                                @ColumnResult(name = "userId", type = Integer.class),
                                                @ColumnResult(name = "banner", type = String.class),
                                        }
                                ),
                        }
                ),
        }
)
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollid")
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer available;
    private Integer registered;
    private boolean locked;
    private boolean lockWhenFull;
    @ElementCollection
    @CollectionTable(
            name = "enrollment_pdf_files",
            joinColumns = @JoinColumn(name = "enrollid")
    )
    private List<PdfDTO> pdfFiles = new ArrayList<>();
    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments;
}
