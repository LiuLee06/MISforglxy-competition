package com.sdjzuxg.collegemanagesystem.agent.llm;

import com.sdjzuxg.collegemanagesystem.agent.tool.AgentToolDefinition;
import java.util.List;

public interface LlmClient {
    LlmResponse chat(List<LlmMessage> messages, List<AgentToolDefinition> tools);
}
