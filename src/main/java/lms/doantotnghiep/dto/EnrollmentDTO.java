package lms.doantotnghiep.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.domain.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EnrollmentDTO {

    private int enrollId;
    private Integer available;
    private Integer registered;
    private boolean locked;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp  startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp  endTime;
    private Integer courseId;
    // Người dạy
    private Integer userId;
    private CourseDTO course;
    private boolean selected;
    private String className;
    public EnrollmentDTO(int enrollId, Integer available,Integer registered, Timestamp  startTime, Timestamp  endTime, Integer courseId, boolean locked, Integer userId, String className) {
        this.enrollId = enrollId;
        this.available = available;
        this.registered = registered;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseId = courseId;
        this.locked = locked;
        this.userId = userId;
        this.className = className;
    }


}
