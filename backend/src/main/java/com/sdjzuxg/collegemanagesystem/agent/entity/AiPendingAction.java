package com.sdjzuxg.collegemanagesystem.agent.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiPendingAction {
    private Long actionId;
    private Long conversationId;
    private Integer userId;
    private String userType;
    private String toolName;
    private String argumentsJson;
    private String actionSummary;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime executedAt;
}
