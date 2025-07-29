package lms.doantotnghiep.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coursetbl")
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "courseId")
    private Integer id;
    @Column(name = "name", columnDefinition = "NVARCHAR(255)")
    private String name;
    private String banner;
    private double credits;
    private Integer classId; // Môn của lớp nào


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User teacher;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Media> medias;

    public Course(String name, double credits) {
        this.name = name;
        this.credits = credits;
    }
}
