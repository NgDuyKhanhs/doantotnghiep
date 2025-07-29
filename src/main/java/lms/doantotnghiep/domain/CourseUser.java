package lms.doantotnghiep.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "courseusertbl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "courseID", nullable = false)
    private Course course;

    @Column(name = "status")
    private Integer status;

    @Column(name = "progress")
    private double progress;

    @Column(name = "score")
    private double score;

    @OneToMany(mappedBy = "courseUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions;

}
