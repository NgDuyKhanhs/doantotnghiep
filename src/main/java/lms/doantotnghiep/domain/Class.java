package lms.doantotnghiep.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "classtbl")
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classId")
    private Integer id;
    private String code;
    @Column(name = "name", columnDefinition = "NVARCHAR(255)")
    private String name;

    public Class(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @OneToMany(
            mappedBy = "classId",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private Set<User> users = new HashSet<>();
}
