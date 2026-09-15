package com.sdjzuxg.collegemanagesystem.agent;

import com.sdjzuxg.collegemanagesystem.agent.llm.*;
import com.sdjzuxg.collegemanagesystem.agent.tool.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MockLlmClientTest {
    @Test
    void workloadQuestionProducesToolCall() {
        AgentToolDefinition definition = new AgentToolDefinition("query_my_workload","",ToolRiskLevel.READ,Map.of(),"", "",false);
        LlmResponse response = new MockLlmClient().chat(List.of(LlmMessage.user("帮我查一下我的工作量")), List.of(definition));
        assertTrue(response.hasToolCalls());
        assertEquals("query_my_workload", response.getToolCalls().get(0).getName());
    }

    @Test
    void unreadNoticeQuestionDoesNotNeedNoticeId() {
        AgentToolDefinition definition = new AgentToolDefinition("query_notice_read_stats","",ToolRiskLevel.READ,Map.of(),"", "",false);
        LlmResponse response = new MockLlmClient().chat(List.of(LlmMessage.user("帮我查查我发布的通知还有哪些人没读")), List.of(definition));
        assertTrue(response.hasToolCalls());
        assertEquals("query_notice_read_stats", response.getToolCalls().get(0).getName());
        assertEquals("{}", response.getToolCalls().get(0).getArguments());
    }
}
