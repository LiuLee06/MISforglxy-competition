package com.sdjzuxg.collegemanagesystem.agent;

import com.sdjzuxg.collegemanagesystem.agent.entity.AiConversation;
import com.sdjzuxg.collegemanagesystem.agent.mapper.*;
import com.sdjzuxg.collegemanagesystem.agent.service.impl.AgentConversationServiceImpl;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ConversationRetentionTest {
    @Test
    void deletingConversationDoesNotDeleteAuditLogs() {
        AiConversationMapper conversations = mock(AiConversationMapper.class);
        AiMessageMapper messages = mock(AiMessageMapper.class);
        AiPendingActionMapper pending = mock(AiPendingActionMapper.class);
        AiActionLogMapper audit = mock(AiActionLogMapper.class);
        AiConversation conversation = new AiConversation(); conversation.setConversationId(9L);
        when(conversations.selectOwned(9L, 7, "teacher")).thenReturn(conversation);
        when(conversations.deleteOwned(9L, 7, "teacher")).thenReturn(1);
        AgentConversationServiceImpl service = new AgentConversationServiceImpl(conversations, messages, pending, audit);
        org.junit.jupiter.api.Assertions.assertTrue(service.delete(9L, new LoginUser(7, "teacher", List.of())));
        verify(messages).deleteByConversationId(9L);
        verify(pending).deleteUnfinishedByConversationId(9L);
        verifyNoInteractions(audit);
    }
}
