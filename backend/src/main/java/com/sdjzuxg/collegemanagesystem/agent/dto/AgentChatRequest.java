package com.sdjzuxg.collegemanagesystem.agent.dto;

import lombok.Data;

@Data
public class AgentChatRequest {
    private Long conversationId;
    private String message;
}
