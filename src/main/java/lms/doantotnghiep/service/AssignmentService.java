package lms.doantotnghiep.service;

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
    AssignmentDTO createAssignment(CreateAssignmentDTO assignmentDTO);
    List<AssignmentDTO> getAssignmentById(int id, int userId, String type);
    AssignmentDTO getDetailAssignmentByID(int id);
    ExamSession startExam(int userId, int assignmentId, List<AnswersJson> answersJsons, ActionType actionType);

    SubmitResult submitExam(int userId, int assignmentId, List<AnswersJson> answersFromClient);

    ViolationReport createViolation(int userId, int assignmentId, String typeViolation, String description, String evidence);

    Integer countSubmitted(int userId,int assignmentId);


}
