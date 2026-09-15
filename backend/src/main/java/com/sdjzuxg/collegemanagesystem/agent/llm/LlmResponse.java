package com.sdjzuxg.collegemanagesystem.agent.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class LlmResponse {
    private String content;
    private List<LlmToolCall> toolCalls;

    public boolean hasToolCalls() { return toolCalls != null && !toolCalls.isEmpty(); }
}
