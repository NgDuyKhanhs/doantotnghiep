package lms.doantotnghiep.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CourseDTO {
    private Integer courseId;
    private String name;
    private double credits;
    private String className;
    private String banner;
    private Integer userId;
    private UserDTO user;
    public CourseDTO(Integer courseId, String name, double credits) {
        this.courseId = courseId;
        this.name = name;
        this.credits = credits;
    }

    public CourseDTO(Integer courseId, String name, double credits, String className, String banner, Integer userId) {
        this.courseId = courseId;
        this.name = name;
        this.credits = credits;
        this.className = className;
        this.banner = banner;
        this.userId = userId;
    }
}
