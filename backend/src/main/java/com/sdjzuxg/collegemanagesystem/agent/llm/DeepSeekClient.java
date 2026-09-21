package com.sdjzuxg.collegemanagesystem.agent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdjzuxg.collegemanagesystem.agent.tool.AgentToolDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "deepseek")
public class DeepSeekClient implements LlmClient {
    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);
    private final RestClient restClient;
    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;

    public DeepSeekClient(DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        if (properties.isThinkingEnabled()) {
            throw new IllegalStateException("当前 Agent 版本不支持 DeepSeek Thinking Tool Calling，请将 ai.deepseek.thinking-enabled 设置为 false");
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(Math.max(1, properties.getTimeoutSeconds())));
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).requestFactory(requestFactory).build();
    }

    @Override
    public LlmResponse chat(List<LlmMessage> messages, List<AgentToolDefinition> tools) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("AI 服务暂时不可用");
        }
        try {
            Map<String,Object> body = new LinkedHashMap<>();
            body.put("model", properties.getModel());
            body.put("messages", messages.stream().map(this::toPayload).toList());
            body.put("tools", tools.stream().map(AgentToolDefinition::toFunctionTool).toList());
            body.put("tool_choice", "auto");
            body.put("max_tokens", Math.max(256, properties.getMaxOutputTokens()));
            if (!properties.isThinkingEnabled()) {
                body.put("thinking", Map.of("type", "disabled"));
            }
            String raw = restClient.post().uri("/chat/completions")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve().body(String.class);
            return parse(raw);
        } catch (RestClientResponseException e) {
            log.error("DeepSeek API returned HTTP {}", e.getStatusCode().value());
            throw new IllegalStateException("AI 服务暂时不可用", e);
        } catch (RuntimeException e) {
            log.error("DeepSeek API request failed exception={}", e.getClass().getSimpleName(), e);
            throw new IllegalStateException("AI 服务暂时不可用", e);
        }
    }

    private Map<String,Object> toPayload(LlmMessage message) {
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("role", message.getRole());
        payload.put("content", message.getContent());
        if (message.getToolCallId() != null) payload.put("tool_call_id", message.getToolCallId());
        if (message.getName() != null) payload.put("name", message.getName());
        if (message.hasToolCalls()) {
            payload.put("tool_calls", message.getToolCalls().stream().map(call -> {
                Map<String,Object> function = new LinkedHashMap<>(); function.put("name", call.getName()); function.put("arguments", call.getArguments());
                Map<String,Object> toolCall = new LinkedHashMap<>(); toolCall.put("id", call.getId()); toolCall.put("type", "function"); toolCall.put("function", function); return toolCall;
            }).toList());
        }
        return payload;
    }

    private LlmResponse parse(String raw) {
        try {
            JsonNode message = objectMapper.readTree(raw).path("choices").path(0).path("message");
            List<LlmToolCall> calls = new ArrayList<>();
            for (JsonNode node : message.path("tool_calls")) {
                JsonNode fn = node.path("function");
                calls.add(new LlmToolCall(node.path("id").asText(), fn.path("name").asText(), fn.path("arguments").asText("{}")));
            }
            String content = message.path("content").isNull() ? null : message.path("content").asText("");
            return new LlmResponse(content, calls);
        } catch (Exception e) {
            throw new IllegalStateException("AI 服务暂时不可用", e);
        }
    }
}

