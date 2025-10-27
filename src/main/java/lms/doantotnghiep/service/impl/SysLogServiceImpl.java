package lms.doantotnghiep.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.SysLog;
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
    public SysLog saveSysLogFromUser(SysLogDTO sysLog, HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {

            ipAddress = ipAddress.split(",")[0];
        } else {
            ipAddress = request.getHeader("X-Real-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        SysLog sysLog1 = new SysLog();
        sysLog1.setUser(userRepository.findById(sysLog.getUserId()).get());
        sysLog1.setStartTime(LocalDateTime.now());
        sysLog1.setAction(sysLog.getAction());
        sysLog1.setStatus(sysLog.getStatus());
        sysLog1.setDescription(sysLog.getDescription());
        sysLog1.setIpAddress(ipAddress);
        return sysLogRepository.save(sysLog1);
    }

    @Override
    public List<SysLogDTO> getSysLogByUser(Integer id) {
        return sysLogRepository.getSysLogByUserID(id);
    }
}
