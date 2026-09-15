package com.sdjzuxg.collegemanagesystem.agent.service;

import com.sdjzuxg.collegemanagesystem.agent.entity.*;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import java.util.List;

public interface AgentConversationService {
    AiConversation getOrCreate(Long id, LoginUser user, String firstMessage);
    void saveMessage(AiMessage message);
    List<AiMessage> recentMessages(Long conversationId, int limit);
    List<AiConversation> list(LoginUser user);
    List<AiMessage> listMessages(Long conversationId, LoginUser user);
    boolean delete(Long conversationId, LoginUser user);
}
