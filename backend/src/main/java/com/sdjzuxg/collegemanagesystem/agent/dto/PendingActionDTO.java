package com.sdjzuxg.collegemanagesystem.agent.dto;

import com.sdjzuxg.collegemanagesystem.agent.entity.AiPendingAction;
import lombok.Data;

@Data
public class PendingActionDTO {
    private Long actionId;
    private String toolName;
    private String title;
    private String summary;
    private String status;
    private String expiresAt;

    public static PendingActionDTO from(AiPendingAction action, String title) {
        PendingActionDTO dto = new PendingActionDTO();
        dto.actionId = action.getActionId();
        dto.toolName = action.getToolName();
        dto.title = title;
        dto.summary = action.getActionSummary();
        dto.status = action.getStatus();
        dto.expiresAt = action.getExpiresAt() == null ? null : action.getExpiresAt().toString();
        return dto;
    }
}
