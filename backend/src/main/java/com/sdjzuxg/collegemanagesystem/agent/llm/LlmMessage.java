package com.sdjzuxg.collegemanagesystem.agent.llm;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class LlmMessage {
    private String role;
    private String content;
    private String toolCallId;
    private String name;
    private List<LlmToolCall> toolCalls;

    public static LlmMessage system(String content) { return message("system", content); }
    public static LlmMessage user(String content) { return message("user", content); }
    public static LlmMessage assistant(String content) { return message("assistant", content); }
    public static LlmMessage tool(String callId, String name, String content) {
        LlmMessage m = message("tool", content);
        m.toolCallId = callId;
        m.name = name;
        return m;
    }
    public static LlmMessage messageForHistory(String role, String content) { return message(role, content); }
    private static LlmMessage message(String role, String content) {
        LlmMessage m = new LlmMessage();
        m.role = role;
        m.content = content;
        return m;
    }
    public static LlmMessage assistantToolCalls(String content, List<LlmToolCall> calls) {
        LlmMessage m = assistant(content);
        m.toolCalls = calls == null ? new ArrayList<>() : calls;
        return m;
    }
    public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }
}

