package com.sdjzuxg.collegemanagesystem.agent.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiActionLog {
    private Long logId;
    private Long conversationId;
    private Integer userId;
    private String userType;
    private String toolName;
    private String riskLevel;
    private String argumentsJson;
    private String resultJson;
    private String status;
    private Long durationMs;
    private LocalDateTime createdAt;
}
