package com.sdjzuxg.collegemanagesystem.agent.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiMessage {
    private Long messageId;
    private Long conversationId;
    private String role;
    private String content;
    private String toolName;
    private String toolCallId;
    private String toolArguments;
    private String toolResult;
    private LocalDateTime createdAt;
}
