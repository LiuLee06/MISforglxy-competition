package com.sdjzuxg.collegemanagesystem.agent.service;

import com.sdjzuxg.collegemanagesystem.agent.dto.*;
import com.sdjzuxg.collegemanagesystem.agent.entity.*;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import java.util.List;

public interface AgentService {
    AgentChatResponse chat(AgentChatRequest request);
    List<AiConversation> conversations(LoginUser user);
    List<AiMessage> messages(Long conversationId, LoginUser user);
    AgentActionService.ActionExecutionResult confirm(Long actionId);
    boolean cancel(Long actionId);
}

