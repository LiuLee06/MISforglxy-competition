package com.sdjzuxg.collegemanagesystem.agent.tool;

import com.sdjzuxg.collegemanagesystem.agent.security.AgentCapabilityService;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.function.Function;

@Component
public class AgentToolRegistry {
    private final Map<String, AgentTool> tools = new LinkedHashMap<>();
    private final AgentCapabilityService capabilityService;

    public AgentToolRegistry(List<AgentTool> toolBeans, AgentCapabilityService capabilityService) {
        this.capabilityService = capabilityService;
        for (AgentTool tool : toolBeans) tools.put(tool.getName(), tool);
    }

    public AgentTool get(String name) { return tools.get(name); }

    public List<AgentTool> getAvailable(LoginUser user) {
        return tools.values().stream().filter(t -> capabilityService.canUse(user, t)).toList();
    }

    public List<AgentToolDefinition> getAvailableDefinitions(LoginUser user) {
        return getAvailable(user).stream().map(AgentTool::definition).toList();
    }

    public boolean canUse(LoginUser user, AgentTool tool) {
        return capabilityService.canUse(user, tool);
    }

    public static Map<String,Object> schema(Map<String,Object> properties, List<String> required) {
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("type", "object");
        result.put("properties", properties);
        if (required != null && !required.isEmpty()) result.put("required", required);
        return result;
    }

    public static Map<String,Object> property(String type, String description) {
        return Map.of("type", type, "description", description);
    }
}
