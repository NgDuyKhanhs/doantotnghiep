package lms.doantotnghiep.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.Class;
import lms.doantotnghiep.domain.Course;
import lms.doantotnghiep.domain.SysLog;
import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.dto.CourseDTO;
import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import lms.doantotnghiep.repository.ClassRepository;
import lms.doantotnghiep.repository.CourseRepository;
import lms.doantotnghiep.repository.SysLogRepository;
import lms.doantotnghiep.repository.UserRepository;
import lms.doantotnghiep.service.CurriculumService;
import lms.doantotnghiep.service.upload.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CurriculumServiceImpl implements CurriculumService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private SysLogRepository sysLogRepository;

    @Override
    public List<CourseDTO> getAllCurriculum() {
        List<CourseDTO> courseDTOList = new ArrayList<>();
        courseDTOList = courseRepository.getAllCourse();
        courseDTOList.forEach(course -> {
            course.setUser(userRepository.getUserByID(course.getUserId()));
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

    @Override
    public void createCurriculum(int id, HttpServletRequest request, CourseDTO courseDTO) {
        if (courseDTO == null) {
            throw new AppException(ErrorConstant.INVALID_ENROLLMENT_DTO);
        }
        String currentIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String deviceName = detectDeviceName(userAgent);
        Course course = new Course();
        Optional<User> userOptional = userRepository.findById(courseDTO.getUserId());
        Optional<Class> classOptional = classRepository.findById(courseDTO.getClassId());
        userOptional.ifPresent(course::setTeacher);
        classOptional.ifPresent(aClass -> course.setClassId(aClass.getId()));
        String imageUrl = imageService.getUrlImage(courseDTO.getBanner());
        course.setBanner(imageUrl);
        course.setName(courseDTO.getName());
        course.setCredits(courseDTO.getCredits());
        courseRepository.save(course);
        SysLog sysLog = new SysLog();
        User user = userRepository.findById(id).get();
        sysLog.setUser(user);
        sysLog.setAction("Tạo khóa học");
        sysLog.setNameDevice(deviceName);
        sysLog.setStartTime(LocalDateTime.now());
        sysLog.setIpAddress(currentIp);
        sysLog.setStatus(0);
        sysLogRepository.save(sysLog);

    }
    public String detectDeviceName(String userAgent) {
        if (userAgent.contains("iPad")) {
            return "iPad - Safari Mobile (iOS)";
        } else if (userAgent.contains("iPhone")) {
            return "iPhone - Safari Mobile (iOS)";
        } else if (userAgent.contains("Android")) {
            return "Android - Chrome";
        } else if (userAgent.contains("Windows")) {
            return "Windows - Chrome/Edge";
        } else if (userAgent.contains("Mac OS X")) {
            return "MacOS - Safari/Chrome";
        }
        return "Unknown Device";
    }
}
