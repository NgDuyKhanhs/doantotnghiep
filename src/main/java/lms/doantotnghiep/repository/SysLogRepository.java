package lms.doantotnghiep.repository;

import lms.doantotnghiep.domain.SysLog;
import lms.doantotnghiep.dto.SysLogDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;


public interface SysLogRepository extends JpaRepository<SysLog, Integer> {
    @Query(value = "select * from syslogtbl order by start_time desc", nativeQuery = true)
    List<SysLog> getAllSysLogs();

    @Query(value = "select syslog_id as syslogId, action, ip_address as ipAddress, start_time as startTime, status, description, name_device as nameDevice from syslogtbl where user_id = ?1 order by start_time desc", nativeQuery = true)
    List<SysLogDTO> getSysLogByUserID(Integer id);

    @Query(value = "SELECT * FROM syslogtbl s WHERE s.user_id = ?1 ORDER BY s.start_time DESC", nativeQuery = true)
    List<SysLog> findRecentByUserId(Integer userId);

    @Query(value = "SELECT COUNT(*) FROM syslogtbl s WHERE s.user_id = ?1 AND s.start_time >= ?2", nativeQuery = true)
    long countRecentLogins(Integer userId, LocalDateTime startTime);

    boolean existsByUserIdAndNameDevice(Integer userId, String nameDevice);
}
