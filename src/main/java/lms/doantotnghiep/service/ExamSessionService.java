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
import java.util.concurrent.TimeUnit;

@Service
public class ExamSessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private static final String PREFIX = "EXAM_SESSION:";

    public void saveExamSession(ExamSession session, long ttlSeconds, boolean isNew) {
        String key = PREFIX + session.getUserId() + ":" + session.getAssignmentId();
        if (ttlSeconds <= 0) {
            ttlSeconds = 3600;
        }
        if (isNew) {
            redisTemplate.opsForValue().set(key, session, Duration. ofSeconds(ttlSeconds));
        } else {
            long existingTtl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (existingTtl > 0) {
                redisTemplate.opsForValue().set(key, session, Duration.ofSeconds(existingTtl));
            } else {
                redisTemplate.opsForValue().set(key, session, Duration.ofSeconds(ttlSeconds));
            }
        }
    }


    public ExamSession getExamSession(int userId, int assignmentId) {
        String key = PREFIX + userId + ":" + assignmentId;
        Object data = redisTemplate.opsForValue().get(key);
        if (data == null) return null;

        ExamSession session = objectMapper.convertValue(data, ExamSession.class);

        Long ttl = redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.SECONDS);
        long remaining = (ttl != null && ttl > 0) ? ttl : 0L;

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