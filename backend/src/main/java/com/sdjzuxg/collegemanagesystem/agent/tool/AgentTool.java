package com.sdjzuxg.collegemanagesystem.agent.tool;

import java.util.Map;

public interface AgentTool {
    String getName();
    String getDescription();
    ToolRiskLevel getRiskLevel();
    Map<String,Object> getParametersSchema();
    default String getDisplayName() { return getName(); }
    default String getRequiredMenu() { return null; }
    default String getRequiredPermission() { return null; }
    default boolean isAdminOnly() { return false; }
    AgentToolResult execute(Map<String,Object> arguments);
    default AgentToolDefinition definition() {
        return new AgentToolDefinition(getName(), getDescription(), getRiskLevel(),
                getParametersSchema(), getRequiredMenu(), getRequiredPermission(), isAdminOnly());
    }
}

