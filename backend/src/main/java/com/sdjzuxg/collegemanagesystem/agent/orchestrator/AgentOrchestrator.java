package com.sdjzuxg.collegemanagesystem.agent.orchestrator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdjzuxg.collegemanagesystem.agent.dto.*;
import com.sdjzuxg.collegemanagesystem.agent.entity.*;
import com.sdjzuxg.collegemanagesystem.agent.llm.*;
import com.sdjzuxg.collegemanagesystem.agent.mapper.AiActionLogMapper;
import com.sdjzuxg.collegemanagesystem.agent.prompt.AgentPromptBuilder;
import com.sdjzuxg.collegemanagesystem.agent.service.*;
import com.sdjzuxg.collegemanagesystem.agent.tool.*;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.service.TeacherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;
import java.util.*;

@Service
public class AgentOrchestrator implements AgentService {
    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);
    private final AgentConversationService conversations;
    private final AgentActionService actions;
    private final AgentToolRegistry registry;
    private final LlmClient llmClient;
    private final AgentPromptBuilder promptBuilder;
    private final TeacherService teacherService;
    private final AiActionLogMapper actionLogMapper;
    private final ObjectMapper objectMapper;
    private final int maxHistoryMessages;
    private final int maxToolIterations;
    private final int maxToolCalls;

    public AgentOrchestrator(AgentConversationService conversations, AgentActionService actions,
            AgentToolRegistry registry, LlmClient llmClient, AgentPromptBuilder promptBuilder,
            TeacherService teacherService, AiActionLogMapper actionLogMapper, ObjectMapper objectMapper,
            @Value("${ai.agent.max-history-messages:20}") int maxHistoryMessages,
            @Value("${ai.agent.max-tool-iterations:6}") int maxToolIterations,
            @Value("${ai.agent.max-tool-calls:8}") int maxToolCalls) {
        this.conversations=conversations;this.actions=actions;this.registry=registry;this.llmClient=llmClient;this.promptBuilder=promptBuilder;
        this.teacherService=teacherService;this.actionLogMapper=actionLogMapper;this.objectMapper=objectMapper;this.maxHistoryMessages=maxHistoryMessages;
        this.maxToolIterations=maxToolIterations;this.maxToolCalls=maxToolCalls;
    }

    @Override
    public AgentChatResponse chat(AgentChatRequest request) {
        LoginUser user=CurrentUserUtil.get();
        if(user==null)return AgentChatResponse.error(null,"未登录或登录已过期");
        if(request==null||request.getMessage()==null||request.getMessage().isBlank())return AgentChatResponse.error(null,"请输入问题");
        if(request.getMessage().length()>4000)return AgentChatResponse.error(request.getConversationId(),"问题长度不能超过4000字");
        AiConversation conversation=conversations.getOrCreate(request.getConversationId(),user,request.getMessage());
        AiMessage userMessage=new AiMessage();userMessage.setConversationId(conversation.getConversationId());userMessage.setRole("user");userMessage.setContent(request.getMessage());conversations.saveMessage(userMessage);
        List<ToolTraceDTO> trace=new ArrayList<>();
        try {
            Teacher teacher=teacherService.findById(user.getUserId());
            List<LlmMessage> messages=new ArrayList<>();
            messages.add(LlmMessage.system(promptBuilder.build(user,teacher)));
            messages.addAll(buildLegalHistory(conversations.recentMessages(conversation.getConversationId(), Math.max(maxHistoryMessages * 3, 30))));
            List<AgentToolDefinition> definitions=registry.getAvailableDefinitions(user);
            int totalCalls=0;
            for(int iteration=0;iteration<maxToolIterations;iteration++) {
                LlmResponse response=llmClient.chat(messages,definitions);
                if(!response.hasToolCalls()) {
                    String answer=response.getContent()==null||response.getContent().isBlank()?"已完成处理。":response.getContent();
                    saveAssistant(conversation.getConversationId(),answer);
                    return AgentChatResponse.completed(conversation.getConversationId(),answer,trace);
                }
                if(totalCalls+response.getToolCalls().size()>maxToolCalls)return AgentChatResponse.error(conversation.getConversationId(),"本次请求涉及的工具调用过多，请缩小问题范围");
                totalCalls+=response.getToolCalls().size();
                boolean containsWriteCall=response.getToolCalls().stream()
                        .map(call -> registry.get(call.getName()))
                        .anyMatch(tool -> tool!=null && tool.getRiskLevel()==ToolRiskLevel.WRITE_CONFIRM);
                if(!containsWriteCall) saveAssistantToolCalls(conversation.getConversationId(),response);
                messages.add(LlmMessage.assistantToolCalls(response.getContent(),response.getToolCalls()));
                for(LlmToolCall call:response.getToolCalls()) {
                    AgentTool tool=registry.get(call.getName());
                    if(tool==null||!registry.canUse(user,tool)) {
                        String error=tool==null?"未知工具":"当前账号没有使用该工具的权限";
                        addToolError(messages,conversation.getConversationId(),call,error,trace);
                        continue;
                    }
                    Map<String,Object> args;
                    try { args=objectMapper.readValue(call.getArguments()==null?"{}":call.getArguments(),new TypeReference<>(){}); }
                    catch(Exception e){addToolError(messages,conversation.getConversationId(),call,"工具参数不是合法 JSON",trace);continue;}
                    if(tool.getRiskLevel()==ToolRiskLevel.WRITE_CONFIRM) {
                        PendingActionDTO pending=actions.create(conversation.getConversationId(),user,tool,args);
                        Map<String,Object> prepared=new LinkedHashMap<>(); prepared.put("actionId",pending.getActionId()); prepared.put("status","PENDING");
                        saveLog(conversation.getConversationId(),user,tool,args,prepared,"PREPARED",0L);
                        trace.add(new ToolTraceDTO(tool.getName(),tool.getDisplayName(),"pending_confirmation"));
                        String confirmation="我已经准备好以下操作，请确认后执行。";
                        saveAssistant(conversation.getConversationId(),confirmation);
                        return AgentChatResponse.confirmation(conversation.getConversationId(),confirmation,pending,trace);
                    }
                    long started=System.currentTimeMillis();
                    AgentToolResult result;
                    try { result=tool.execute(args); } catch(Exception e){result=AgentToolResult.fail("参数无效或业务校验失败");}
                    long duration=System.currentTimeMillis()-started;
                    trace.add(new ToolTraceDTO(tool.getName(),tool.getDisplayName(),result.isSuccess()?"success":"failed"));
                    Map<String,Object> envelope=new LinkedHashMap<>();envelope.put("success",result.isSuccess());envelope.put("data",result.getData());envelope.put("error",result.getError());
                    String content=objectMapper.writeValueAsString(envelope);
                    messages.add(LlmMessage.tool(call.getId(),call.getName(),content));
                    AiMessage tm=new AiMessage();tm.setConversationId(conversation.getConversationId());tm.setRole("tool");tm.setToolName(call.getName());tm.setToolCallId(call.getId());tm.setToolArguments(call.getArguments());tm.setToolResult(content);conversations.saveMessage(tm);
                    Object logResult=result.isSuccess()?result.getData():failureResult(result.getError());
                    saveLog(conversation.getConversationId(),user,tool,args,logResult,result.isSuccess()?"SUCCESS":"FAILED",duration);
                }
            }
            return AgentChatResponse.error(conversation.getConversationId(),"AI 工具处理达到上限，请稍后重试");
        } catch(Exception e) {
            log.warn("Agent request failed conversationId={} userId={} userType={} exception={}",
                    conversation.getConversationId(), user.getUserId(), user.getUserType(), e.getClass().getSimpleName(), e);
            return AgentChatResponse.error(conversation.getConversationId(),"AI 服务暂时不可用，请稍后重试。");
        }
    }

    private List<LlmMessage> buildLegalHistory(List<AiMessage> stored) {
        List<List<LlmMessage>> groups = new ArrayList<>();
        for (int i = 0; i < stored.size();) {
            AiMessage current = stored.get(i);
            if ("tool".equals(current.getRole())) { i++; continue; }
            if ("assistant".equals(current.getRole()) && current.getToolArguments() != null && !current.getToolArguments().isBlank()) {
                List<LlmMessage> group = new ArrayList<>();
                group.add(toLlmMessage(current));
                List<String> callIds;
                try {
                    List<Map<String,String>> calls = objectMapper.readValue(current.getToolArguments(), new TypeReference<>() {});
                    callIds = calls.stream().map(call -> call.get("id")).filter(Objects::nonNull).toList();
                } catch (Exception ignored) { i++; continue; }
                int next = i + 1;
                while (next < stored.size() && "tool".equals(stored.get(next).getRole())) {
                    AiMessage tool = stored.get(next);
                    if (tool.getToolCallId() != null && callIds.contains(tool.getToolCallId())) group.add(toLlmMessage(tool));
                    next++;
                }
                Set<String> resultIds = group.stream().map(LlmMessage::getToolCallId)
                        .filter(Objects::nonNull).collect(Collectors.toSet());
                if (!callIds.isEmpty() && resultIds.containsAll(callIds)) groups.add(group);
                i = next;
                continue;
            }
            groups.add(List.of(toLlmMessage(current)));
            i++;
        }
        int from = Math.max(0, groups.size() - maxHistoryMessages);
        List<LlmMessage> result = new ArrayList<>();
        for (List<LlmMessage> group : groups.subList(from, groups.size())) result.addAll(group);
        return result;
    }

    private Map<String,Object> failureResult(String error) {
        Map<String,Object> result = new LinkedHashMap<>(); result.put("error", error); return result;
    }

    private void addToolError(List<LlmMessage> messages,Long cid,LlmToolCall call,String error,List<ToolTraceDTO> trace){
        Map<String,Object> body=new LinkedHashMap<>(); body.put("success",false); body.put("error",error);
        String json; try { json=objectMapper.writeValueAsString(body); } catch(Exception e) { json="tool error"; }
        messages.add(LlmMessage.tool(call.getId(),call.getName(),json));
        AiMessage tm=new AiMessage();tm.setConversationId(cid);tm.setRole("tool");tm.setToolName(call.getName());tm.setToolCallId(call.getId());tm.setToolResult(json);conversations.saveMessage(tm);
        trace.add(new ToolTraceDTO(call.getName(),call.getName(),"failed"));
    }
    private LlmMessage toLlmMessage(AiMessage m){
        if("tool".equals(m.getRole()))return LlmMessage.tool(m.getToolCallId(),m.getToolName(),m.getToolResult());
        if("assistant".equals(m.getRole()) && m.getToolArguments()!=null && !m.getToolArguments().isBlank()) {
            try {
                List<Map<String,String>> rawCalls=objectMapper.readValue(m.getToolArguments(),new TypeReference<List<Map<String,String>>>(){});
                List<LlmToolCall> calls=rawCalls.stream()
                        .map(raw -> new LlmToolCall(raw.get("id"),raw.get("name"),raw.getOrDefault("arguments","{}")))
                        .toList();
                return LlmMessage.assistantToolCalls(m.getContent(),calls);
            } catch(Exception ignored) { }
        }
        return LlmMessage.messageForHistory(m.getRole(),m.getContent());
    }
    private void saveAssistantToolCalls(Long cid,LlmResponse response){
        try {
            AiMessage m=new AiMessage();
            m.setConversationId(cid);
            m.setRole("assistant");
            m.setContent(response.getContent());
            m.setToolArguments(objectMapper.writeValueAsString(response.getToolCalls()));
            conversations.saveMessage(m);
        } catch(Exception ignored) { }
    }
    private void saveAssistant(Long cid,String content){AiMessage m=new AiMessage();m.setConversationId(cid);m.setRole("assistant");m.setContent(content);conversations.saveMessage(m);}
    private void saveLog(Long cid,LoginUser u,AgentTool tool,Object args,Object result,String status,long duration){
        try{AiActionLog l=new AiActionLog();l.setConversationId(cid);l.setUserId(u.getUserId());l.setUserType(u.getUserType());l.setToolName(tool.getName());l.setRiskLevel(tool.getRiskLevel().name());l.setArgumentsJson(objectMapper.writeValueAsString(args));l.setResultJson(objectMapper.writeValueAsString(result));l.setStatus(status);l.setDurationMs(duration);actionLogMapper.insert(l);}catch(Exception ignored){}
    }
    public List<AiConversation> conversations(LoginUser user){return conversations.list(user);}
    public List<AiMessage> messages(Long id,LoginUser user){return conversations.listMessages(id,user);}
    public boolean deleteConversation(Long id, LoginUser user){return conversations.delete(id,user);}
    public AgentActionService.ActionExecutionResult confirm(Long id){return actions.confirm(id,CurrentUserUtil.get(),registry);}
    public boolean cancel(Long id){return actions.cancel(id,CurrentUserUtil.get());}
}


