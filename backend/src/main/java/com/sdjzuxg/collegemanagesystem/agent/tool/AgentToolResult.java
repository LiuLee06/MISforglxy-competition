package com.sdjzuxg.collegemanagesystem.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentToolResult {
    private boolean success;
    private Object data;
    private String error;

    public static AgentToolResult ok(Object data) { return new AgentToolResult(true, data, null); }
    public static AgentToolResult fail(String error) { return new AgentToolResult(false, null, error); }
}
