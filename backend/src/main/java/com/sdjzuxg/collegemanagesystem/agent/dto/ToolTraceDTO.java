package com.sdjzuxg.collegemanagesystem.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ToolTraceDTO {
    private String tool;
    private String displayName;
    private String status;
}
