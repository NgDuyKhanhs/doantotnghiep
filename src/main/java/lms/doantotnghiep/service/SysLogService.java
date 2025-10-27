package lms.doantotnghiep.service;

import jakarta.servlet.http.HttpServletRequest;
import lms.doantotnghiep.domain.SysLog;
import lms.doantotnghiep.dto.SysLogDTO;

import java.util.List;

public interface SysLogService {
    SysLog saveSysLogFromUser(SysLogDTO sysLog, HttpServletRequest request);

    List<SysLogDTO> getSysLogByUser(Integer id);
}
