package com.sdjzuxg.collegemanagesystem.agent.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdjzuxg.collegemanagesystem.agent.tool.AgentToolDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmClient implements LlmClient {
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LlmResponse chat(List<LlmMessage> messages, List<AgentToolDefinition> tools) {
        LlmMessage last = messages.get(messages.size() - 1);
        String input = last.getContent() == null ? "" : last.getContent();
        if (input.contains("[[KNOWLEDGE_NORMALIZE]]")) {
            String draft = input.contains("DRAFT:") ? input.substring(input.indexOf("DRAFT:") + 6).trim() : "";
            try {
                return new LlmResponse(objectMapper.writeValueAsString(Map.of("markdown", draft, "warnings", List.of())), List.of());
            } catch (Exception e) {
                return new LlmResponse("{\"markdown\":\"\",\"warnings\":[\"mock failed\"]}", List.of());
            }
        }
        if (input.contains("[[KNOWLEDGE_SUMMARY]]")) {
            String fileName = "学院文件";
            Matcher fileMatcher = Pattern.compile("文件名：([^\\n]+)").matcher(input);
            if (fileMatcher.find()) fileName = fileMatcher.group(1).trim();
            String content = input.contains("CONTENT:") ? input.substring(input.indexOf("CONTENT:") + 8).trim() : "";
            String plain = content.replaceAll("[#|*`>-]", " ").replaceAll("\\s+", " ").trim();
            String summary = plain.length() <= 120 ? plain : plain.substring(0, 120) + "…";
            try {
                return new LlmResponse(objectMapper.writeValueAsString(Map.of("title", fileName.replaceFirst("(?i)\\.(docx?|)$", ""),
                        "summary", summary, "keywords", List.of(fileName), "documentType", "其他")), List.of());
            } catch (Exception e) {
                return new LlmResponse("{}", List.of());
            }
        }
        if ("tool".equals(last.getRole())) {
            String result = last.getContent() == null ? "" : last.getContent();
            String marker = String.valueOf((char)34) + "success" + String.valueOf((char)34) + ":false";
            if (result.contains(marker)) return new LlmResponse("查询失败：" + result, List.of());
            if ("search_knowledge_catalog".equals(last.getName())) {
                try {
                    JsonNode envelope = objectMapper.readTree(result);
                    JsonNode documents = envelope.path("data").path("documents");
                    if (documents.isArray() && documents.size() > 0) {
                        int documentId = documents.get(0).path("documentId").asInt();
                        Map<String, Object> args = new LinkedHashMap<>();
                        args.put("documentId", documentId);
                        args.put("query", "");
                        return new LlmResponse(null, List.of(new LlmToolCall("mock-knowledge-read-" + System.nanoTime(),
                                "read_knowledge_document", objectMapper.writeValueAsString(args))));
                    }
                } catch (Exception ignored) {
                    // mock 检索失败时直接返回普通文本，保持测试服务可用。
                }
            }
            if ("read_knowledge_document".equals(last.getName())) {
                return new LlmResponse("已根据学院文件正文整理答案。\n\n来源：学院知识库文件及相关章节。\n\n" + result, List.of());
            }
            return new LlmResponse("已根据学院业务系统查询完成。结果如下：" + System.lineSeparator() + result, List.of());
        }
        String selectedTool = null;
        if (input.contains("制度") || input.contains("规定") || input.contains("办法") || input.contains("文件") || input.contains("知识库")) selectedTool = "search_knowledge_catalog";
        else if (input.contains("我是谁") || input.contains("当前用户")) selectedTool = "get_current_user";
        else if (input.contains("工作量") && (input.contains("未确认") || input.contains("完成"))) selectedTool = "query_workload_completion";
        else if (input.contains("工作量")) selectedTool = "query_my_workload";
        else if (input.contains("监考") || input.contains("考试")) selectedTool = "query_my_exam_assignments";
        else if (input.contains("教师") || input.contains("老师")) selectedTool = "search_teachers";
        else if (input.contains("通知") && (input.contains("未读") || input.contains("没读") || input.contains("没有读") || input.contains("阅读"))) selectedTool = "query_notice_read_stats";
        else if ((input.contains("预约") || input.contains("预定")) && !input.contains("可用")) selectedTool = "create_room_application";
        else if (input.contains("会议室")) selectedTool = "find_available_meeting_rooms";

        String requestedTool = selectedTool;
        if (requestedTool != null && tools.stream().anyMatch(t -> t.getName().equals(requestedTool))) {
            Map<String,Object> args = new LinkedHashMap<>();
            if ("find_available_meeting_rooms".equals(selectedTool)) {
                args.put("date", LocalDate.now(ZONE_ID).plusDays(1).toString()); args.put("startTime","14:00"); args.put("endTime","16:00"); args.put("minCapacity",0);
            } else if ("create_room_application".equals(selectedTool)) {
                Matcher room = Pattern.compile("(\\d+)").matcher(input);
                args.put("roomId", room.find() ? Integer.valueOf(room.group(1)) : 1);
                args.put("date", LocalDate.now(ZONE_ID).plusDays(1).toString()); args.put("startTime","14:00"); args.put("endTime","16:00"); args.put("purpose","学院事务");
            } else if ("search_teachers".equals(selectedTool)) {
                args.put("keyword",""); args.put("department","");
            } else if ("search_knowledge_catalog".equals(selectedTool)) {
                args.put("query", input); args.put("limit", 5);
            }
            try { return new LlmResponse(null, List.of(new LlmToolCall("mock-" + System.nanoTime(), selectedTool, objectMapper.writeValueAsString(args)))); }
            catch (Exception e) { return new LlmResponse("Mock 参数生成失败", List.of()); }
        }
        return new LlmResponse("你好，我是学院行政 AI 助手，可以帮你查询工作量、会议室、考试和通知信息，也可以在确认后提交预约或通知操作。", List.of());
    }
}


