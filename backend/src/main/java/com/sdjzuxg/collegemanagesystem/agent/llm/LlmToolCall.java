package com.sdjzuxg.collegemanagesystem.agent.llm;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LlmToolCall {
    private String id;
    private String name;
    private String arguments;
}
