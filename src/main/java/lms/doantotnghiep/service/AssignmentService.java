package lms.doantotnghiep.service;

import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.dto.AssignmentDTO;
import lms.doantotnghiep.dto.ExamSession;
import lms.doantotnghiep.dto.request.CreateAssignmentDTO;

import java.util.List;

public interface AssignmentService {
    AssignmentDTO createAssignment(CreateAssignmentDTO assignmentDTO);
    List<AssignmentDTO> getAssignmentById(int id);
    AssignmentDTO getDetailAssignmentByID(int id);
    ExamSession startExam(int userId, int assignmentId);
}
