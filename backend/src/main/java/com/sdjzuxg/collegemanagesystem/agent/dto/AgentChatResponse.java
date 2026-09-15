package com.sdjzuxg.collegemanagesystem.agent.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AgentChatResponse {
    private Long conversationId;
    private String status;
    private String message;
    private PendingActionDTO pendingAction;
    private List<ToolTraceDTO> toolTrace = new ArrayList<>();

    public static AgentChatResponse completed(Long id, String message, List<ToolTraceDTO> trace) {
        AgentChatResponse r = new AgentChatResponse();
        r.conversationId = id;
        r.status = "completed";
        r.message = message;
        if (trace != null) r.toolTrace = trace;
        return r;
    }

    public static AgentChatResponse confirmation(Long id, String message, PendingActionDTO action, List<ToolTraceDTO> trace) {
        AgentChatResponse r = completed(id, message, trace);
        r.status = "need_confirmation";
        r.pendingAction = action;
        return r;
    }

    public static AgentChatResponse error(Long id, String message) {
        AgentChatResponse r = completed(id, message, List.of());
        r.status = "error";
        return r;
    }
}
