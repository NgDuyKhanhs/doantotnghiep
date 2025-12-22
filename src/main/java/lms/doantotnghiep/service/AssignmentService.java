package lms.doantotnghiep.service;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.domain.ViolationReport;
import lms.doantotnghiep.dto.AssignmentDTO;
import lms.doantotnghiep.dto.ExamSession;
import lms.doantotnghiep.dto.request.AnswersJson;
import lms.doantotnghiep.dto.request.CreateAssignmentDTO;
import lms.doantotnghiep.dto.response.SubmitResult;
import lms.doantotnghiep.enums.ActionType;

import java.util.List;

public interface AssignmentService {
    AssignmentDTO createAssignment(int id,CreateAssignmentDTO assignmentDTO,HttpServletRequest request);
    List<AssignmentDTO> getAssignmentById(int id, int userId, String type);
    AssignmentDTO getDetailAssignmentByID(int id);
    ExamSession startExam(int userId, int assignmentId, List<AnswersJson> answersJsons, ActionType actionType, HttpServletRequest request);

    SubmitResult submitExam(int userId, int assignmentId, List<AnswersJson> answersFromClient,HttpServletRequest request);

    ViolationReport createViolation(int userId, int assignmentId, String typeViolation, String description, String evidence);

    Integer countSubmitted(int userId,int assignmentId);

    List<AssignmentDTO> findUnsubmitted(int userId);

}
