package lms.doantotnghiep.service;

import lms.doantotnghiep.dto.EnrollmentDTO;

import java.util.List;

public interface EnrollmentService {
    List<EnrollmentDTO> getAllEnrollments(Integer classId);

    void registerEnrollment(List<Integer> enrollIds);

    List<EnrollmentDTO> getEnrollmentFilter(String type, Integer userId);
}
