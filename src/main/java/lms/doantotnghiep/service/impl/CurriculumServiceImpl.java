package lms.doantotnghiep.service.impl;

import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.dto.CourseDTO;
import lms.doantotnghiep.repository.CourseRepository;
import lms.doantotnghiep.repository.UserRepository;
import lms.doantotnghiep.service.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class CurriculumServiceImpl implements CurriculumService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<CourseDTO> getAllCurriculum() {
        List<CourseDTO> courseDTOList = new ArrayList<>();
        courseDTOList = courseRepository.getAllCourse();
        courseDTOList.forEach(course -> {
            course.setUser(userRepository.getTeacherByID(course.getUserId()));
        });
        return courseDTOList;
    }

    @Override
    public Course getCurriculumById(Integer id) {
        if (courseRepository.existsById(id)) {
            return courseRepository.findById(id).get();
        }
        return null;
    }

    @Override
    public void deleteCurriculumById(Integer id) {

    }

    @Override
    public void updateCurriculumById(Integer id, Course course) {

    }
}
