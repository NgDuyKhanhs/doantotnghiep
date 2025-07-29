package lms.doantotnghiep.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResponseConfig {
    private List<String> keywords;
    private String response;
}
