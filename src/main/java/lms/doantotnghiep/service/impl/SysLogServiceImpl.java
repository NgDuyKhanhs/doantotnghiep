package lms.doantotnghiep.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.SysLog;
import lms.doantotnghiep.domain.User;
import lms.doantotnghiep.dto.SysLogDTO;
import lms.doantotnghiep.repository.SysLogRepository;
import lms.doantotnghiep.repository.UserRepository;
import lms.doantotnghiep.service.SysLogService;
import lms.doantotnghiep.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysLogServiceImpl implements SysLogService {

    @Autowired
    private SysLogRepository sysLogRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public SysLog saveSysLogFromUser(
            SysLogDTO sysLog,
            HttpServletRequest request) {

        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = ipAddress.split(",")[0].trim();
        } else {
            // Thử lấy từ X-Real-IP
            ipAddress = request.getHeader("X-Real-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        SysLog sysLogEntry = new SysLog();
        User userReference = userRepository.getReferenceById(sysLogEntry.getUser().getId());
        sysLogEntry.setUser(userReference);
        sysLogEntry.setStartTime(LocalDateTime.now());
        sysLogEntry.setAction(sysLog.getAction());
        sysLogEntry.setStatus(sysLog.getStatus());
        sysLogEntry.setDescription(sysLog.getDescription());
        sysLogEntry.setIpAddress(ipAddress);
        try {
            return sysLogRepository.save(sysLogEntry);
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu SysLog: " + e.getMessage());
            return null;
        }
    }
    @Override
    public List<SysLogDTO> getSysLogByUser(Integer id) {
        return sysLogRepository.getSysLogByUserID(id);
    }
}
