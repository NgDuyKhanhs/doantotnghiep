package lms.doantotnghiep.service;


import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.dto.CourseDTO;

import java.util.List;

public interface CurriculumService {
    List<CourseDTO> getAllCurriculum();
    Course getCurriculumById(Integer id);
    void deleteCurriculumById(Integer id);
    void updateCurriculumById(Integer id, Course course);
    void createCurriculum(int id, HttpServletRequest request, CourseDTO courseDTO);
}
