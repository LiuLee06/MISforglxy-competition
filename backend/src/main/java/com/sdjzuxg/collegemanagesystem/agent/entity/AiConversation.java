package com.sdjzuxg.collegemanagesystem.agent.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiConversation {
    private Long conversationId;
    private Integer userId;
    private String userType;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
