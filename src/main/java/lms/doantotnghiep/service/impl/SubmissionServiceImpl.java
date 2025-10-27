package lms.doantotnghiep.service.impl;

import lms.doantotnghiep.domain.Assignment;
import lms.doantotnghiep.domain.Submission;
import lms.doantotnghiep.repository.AssignmentRepository;
import lms.doantotnghiep.repository.SubmissionRepository;
import lms.doantotnghiep.repository.UserRepository;
import lms.doantotnghiep.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Override
    public int create(int userId, int assignmentId) {
        // Kiểm tra xem đã có submission đang mở chưa
        var existing = submissionRepository.findActiveSubmission(userId, assignmentId);
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Assignment assignment = assignmentRepository.findById(assignmentId);

        Submission submission = new Submission();
        submission.setUser(user);
        submission.setAssignment(assignment);
        submission.setStartTime(java.time.LocalDateTime.now());
        submission.setStatus("IN_PROGRESS");

        submissionRepository.save(submission);
        return submission.getId();
    }
}
