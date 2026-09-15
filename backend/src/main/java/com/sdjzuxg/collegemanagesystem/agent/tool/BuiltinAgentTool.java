package com.sdjzuxg.collegemanagesystem.agent.tool;

import java.util.Map;
import java.util.function.Function;

public class BuiltinAgentTool implements AgentTool {
    private final String name;
    private final String description;
    private final ToolRiskLevel riskLevel;
    private final Map<String,Object> schema;
    private final String displayName;
    private final String requiredMenu;
    private final String requiredPermission;
    private final boolean adminOnly;
    private final Function<Map<String,Object>, AgentToolResult> executor;

    public BuiltinAgentTool(String name, String description, ToolRiskLevel riskLevel,
                            Map<String,Object> schema, String displayName, boolean adminOnly,
                            Function<Map<String,Object>, AgentToolResult> executor) {
        this(name, description, riskLevel, schema, displayName, null, null, adminOnly, executor);
    }

    public BuiltinAgentTool(String name, String description, ToolRiskLevel riskLevel,
                            Map<String,Object> schema, String displayName, String requiredMenu,
                            String requiredPermission, boolean adminOnly,
                            Function<Map<String,Object>, AgentToolResult> executor) {
        this.name = name; this.description = description; this.riskLevel = riskLevel;
        this.schema = schema; this.displayName = displayName; this.requiredMenu = requiredMenu;
        this.requiredPermission = requiredPermission; this.adminOnly = adminOnly; this.executor = executor;
    }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ToolRiskLevel getRiskLevel() { return riskLevel; }
    public Map<String,Object> getParametersSchema() { return schema; }
    public String getDisplayName() { return displayName; }
    public String getRequiredMenu() { return requiredMenu; }
    public String getRequiredPermission() { return requiredPermission; }
    public boolean isAdminOnly() { return adminOnly; }
    public AgentToolResult execute(Map<String,Object> arguments) { return executor.apply(arguments == null ? Map.of() : arguments); }
}
