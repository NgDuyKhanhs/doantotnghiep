package lms.doantotnghiep.service;

import lms.doantotnghiep.dto.response.StudentSubmissionResponse;
import lms.doantotnghiep.dto.response.SubmissionHistoryResponse;

import java.util.List;

public interface SubmissionService {
    int create(int userId, int assignmentId);
    SubmissionHistoryResponse getSubmissionHistory(int userId, int assignmentId);

    List<StudentSubmissionResponse> getStudentSubmissions(int teacherId, int assignmentId, String status);

}
