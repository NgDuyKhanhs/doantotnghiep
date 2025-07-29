package lms.doantotnghiep.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "media")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mediaId")
    private Integer id;

    private String url;

    private String type; // "IMAGE" or "VIDEO"

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

}
