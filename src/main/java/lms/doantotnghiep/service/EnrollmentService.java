package lms.doantotnghiep.service;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.dto.CourseDTO;
import lms.doantotnghiep.dto.EnrollmentDTO;
import lms.doantotnghiep.dto.PdfDTO;
import lms.doantotnghiep.dto.response.PdfResponse;

import java.io.IOException;
import java.util.List;

public interface EnrollmentService {
    List<EnrollmentDTO> getAllEnrollments(Integer classId);

    void registerEnrollment(List<Integer> enrollIds, HttpServletRequest request);

    List<EnrollmentDTO> getEnrollmentFilter(String type, Integer userId, String currentRole);

    void addPDFInEnrollment(EnrollmentDTO enrollmentDTO, Integer userId) throws IOException;

    List<PdfResponse> getPDFFilesByEnrollmentId(Integer enrollmentId);

    boolean hasEnrolledCourse(Integer userId, String url);
}
