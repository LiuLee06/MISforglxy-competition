package com.sdjzuxg.collegemanagesystem.agent.tool;

import java.util.Map;

public interface AgentTool {
    String getName();
    String getDescription();
    ToolRiskLevel getRiskLevel();
    Map<String,Object> getParametersSchema();
    default String getDisplayName() { return getName(); }
    default String getRequiredMenu() { return switch (getName()) { case "search_teachers" -> "/teacher-info"; case "query_my_workload" -> "/teacher-workload"; case "find_available_meeting_rooms", "query_my_room_applications", "create_room_application" -> "/meeting-reserve"; case "query_my_exam_assignments" -> "/exam-supervise"; case "query_notice_read_stats", "remind_notice" -> "/notice-maintain"; default -> null; }; }
    default String getRequiredPermission() { return null; }
    default boolean isAdminOnly() { return false; }
    AgentToolResult execute(Map<String,Object> arguments);
    default AgentToolDefinition definition() {
        return new AgentToolDefinition(getName(), getDescription(), getRiskLevel(),
                getParametersSchema(), getRequiredMenu(), getRequiredPermission(), isAdminOnly());
    }
}

