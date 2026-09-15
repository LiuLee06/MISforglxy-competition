package com.sdjzuxg.collegemanagesystem.agent.tool;

import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AgentToolDefinition {
    private String name;
    private String description;
    private ToolRiskLevel riskLevel;
    private Map<String,Object> parameters;
    private String requiredMenu;
    private String requiredPermission;
    private boolean adminOnly;

    public AgentToolDefinition(String name, String description, ToolRiskLevel riskLevel,
                               Map<String,Object> parameters, String requiredMenu,
                               String requiredPermission, boolean adminOnly) {
        this.name = name;
        this.description = description;
        this.riskLevel = riskLevel;
        this.parameters = parameters == null ? Map.of("type","object","properties",Map.of()) : parameters;
        this.requiredMenu = requiredMenu;
        this.requiredPermission = requiredPermission;
        this.adminOnly = adminOnly;
    }

    public Map<String,Object> toFunctionTool() {
        Map<String,Object> function = new LinkedHashMap<>();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", parameters);
        return Map.of("type", "function", "function", function);
    }
}
