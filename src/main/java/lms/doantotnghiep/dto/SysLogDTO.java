package lms.doantotnghiep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLogDTO {
    private Integer syslogId;
    private Integer userId;
    private String action;
    private String description;
    private Timestamp startTime;
    private String ipAddress;
    private Integer status;
    private String nameDevice;

    public SysLogDTO(Integer syslogId, String action, String ipAddress, Timestamp startTime, Integer status, String description, String nameDevice) {
        this.syslogId = syslogId;
        this.action = action;
        this.ipAddress = ipAddress;
        this.startTime = startTime;
        this.status = status;
        this.description = description;
        this.nameDevice = nameDevice;

    }

}
