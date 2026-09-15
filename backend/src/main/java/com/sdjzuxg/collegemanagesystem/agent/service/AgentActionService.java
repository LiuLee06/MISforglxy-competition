package com.sdjzuxg.collegemanagesystem.agent.service;

import com.sdjzuxg.collegemanagesystem.agent.dto.PendingActionDTO;
import com.sdjzuxg.collegemanagesystem.agent.tool.*;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import java.util.Map;

public interface AgentActionService {
    PendingActionDTO create(Long conversationId, LoginUser user, AgentTool tool, Map<String,Object> args);
    ActionExecutionResult confirm(Long actionId, LoginUser user, AgentToolRegistry registry);
    boolean cancel(Long actionId, LoginUser user);
    record ActionExecutionResult(boolean success, String message, String status) {}
}
