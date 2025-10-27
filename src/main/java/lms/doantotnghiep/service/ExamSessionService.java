package lms.doantotnghiep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lms.doantotnghiep.dto.ExamSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
public class ExamSessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String PREFIX = "EXAM_SESSION:";

    public void saveExamSession(ExamSession session, long ttlMinutes) {
        String key = PREFIX + session.getUserId() + ":" + session.getAssignmentId();
        redisTemplate.opsForValue().set(key, session, Duration.ofMinutes(ttlMinutes));
    }

    public ExamSession getExamSession(int userId, int assignmentId) {
        String key = PREFIX + userId + ":" + assignmentId;
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null) return null;

        ExamSession session = objectMapper.convertValue(data, ExamSession.class);

        LocalDateTime start = LocalDateTime.parse(session.getStartTime(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        long elapsed = Duration.between(start, LocalDateTime.now()).getSeconds();
        long remaining = Math.max(session.getRemainingSeconds() - elapsed, 0);

        session.setRemainingSeconds(remaining);
        return session;
    }



    public void deleteExamSession(int userId, int assignmentId) {
        String key = PREFIX + userId + ":" + assignmentId;
        redisTemplate.delete(key);
    }

    public ExamSession findActiveSessionByUser(int userId) {
        String pattern = PREFIX + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return null;
        }
        for (String key : keys) {
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                ExamSession session = objectMapper.convertValue(data, ExamSession.class);
                // Nếu còn thời gian thì xem như đang làm
                if (session.getRemainingSeconds() > 0) {
                    return session;
                }
            }
        }
        return null;
    }
}