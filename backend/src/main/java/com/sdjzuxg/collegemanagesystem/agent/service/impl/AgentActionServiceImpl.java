package com.sdjzuxg.collegemanagesystem.agent.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdjzuxg.collegemanagesystem.agent.dto.PendingActionDTO;
import com.sdjzuxg.collegemanagesystem.agent.entity.AiActionLog;
import com.sdjzuxg.collegemanagesystem.agent.entity.AiPendingAction;
import com.sdjzuxg.collegemanagesystem.agent.mapper.AiActionLogMapper;
import com.sdjzuxg.collegemanagesystem.agent.mapper.AiPendingActionMapper;
import com.sdjzuxg.collegemanagesystem.agent.service.AgentActionService;
import com.sdjzuxg.collegemanagesystem.agent.tool.*;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AgentActionServiceImpl implements AgentActionService {
    private final AiPendingActionMapper mapper;
    private final AiActionLogMapper logMapper;
    private final ObjectMapper objectMapper;

    public AgentActionServiceImpl(AiPendingActionMapper mapper, AiActionLogMapper logMapper, ObjectMapper objectMapper) {
        this.mapper=mapper; this.logMapper=logMapper; this.objectMapper=objectMapper;
    }

    @Override
    public PendingActionDTO create(Long conversationId, LoginUser user, AgentTool tool, Map<String,Object> args) {
        try {
            AiPendingAction action=new AiPendingAction();
            action.setConversationId(conversationId); action.setUserId(user.getUserId()); action.setUserType(user.getUserType());
            action.setToolName(tool.getName()); action.setArgumentsJson(objectMapper.writeValueAsString(args));
            action.setActionSummary(summary(tool.getName(), args)); action.setExpiresAt(LocalDateTime.now().plusMinutes(10)); action.setStatus("PENDING");
            mapper.insert(action);
            return PendingActionDTO.from(action, tool.getDisplayName());
        } catch(JsonProcessingException e) {
            throw new IllegalStateException("无法创建待确认操作",e);
        }
    }

    @Override
    @Transactional
    public ActionExecutionResult confirm(Long id, LoginUser user, AgentToolRegistry registry) {
        if(user==null)return new ActionExecutionResult(false,"未登录","FORBIDDEN");
        AiPendingAction action=mapper.selectOwned(id,user.getUserId(),user.getUserType());
        if(action==null)return new ActionExecutionResult(false,"操作不存在或无权访问","FORBIDDEN");
        if(action.getExpiresAt()==null||action.getExpiresAt().isBefore(LocalDateTime.now()))return new ActionExecutionResult(false,"该操作已过期","EXPIRED");
        AgentTool tool=registry.get(action.getToolName());
        if(tool==null||!registry.canUse(user,tool))return new ActionExecutionResult(false,"当前账号已无权执行该操作","FORBIDDEN");
        if(tool.getRiskLevel()!=ToolRiskLevel.WRITE_CONFIRM)return new ActionExecutionResult(false,"该操作不是可确认的写操作","REJECTED");
        if(mapper.claim(id,user.getUserId(),user.getUserType())!=1)return new ActionExecutionResult(false,"该操作已经执行、取消或正在执行","REJECTED");
        long started=System.currentTimeMillis();
        try {
            Map<String,Object> args=objectMapper.readValue(action.getArgumentsJson(),Map.class);
            AgentToolResult result=tool.execute(args);
            long duration=System.currentTimeMillis()-started;
            if(result.isSuccess()){
                mapper.markExecuted(id); log(user,action,tool,args,result.getData(),"EXECUTED",duration);
                return new ActionExecutionResult(true,successMessage(tool.getName()),"EXECUTED");
            }
            mapper.markFailed(id); log(user,action,tool,args,Map.of("error",result.getError()),"FAILED",duration);
            return new ActionExecutionResult(false,result.getError(),"FAILED");
        } catch(Exception e) {
            mapper.markFailed(id); log(user,action,tool,Map.of(),"FAILED","FAILED",System.currentTimeMillis()-started);
            return new ActionExecutionResult(false,"操作执行失败，请稍后重试","FAILED");
        }
    }

    @Override
    public boolean cancel(Long id,LoginUser user) {
        return user!=null && mapper.cancel(id,user.getUserId(),user.getUserType())>0;
    }

    private String summary(String name,Map<String,Object> args) {
        if("create_room_application".equals(name)) return "会议室 "+args.get("roomId")+"，日期 "+args.get("date")+"，时间 "+args.get("startTime")+"—"+args.get("endTime")+"，用途："+args.get("purpose");
        if("publish_notice".equals(name)) return "发布通知《"+args.get("title")+"》，接收人数："+((java.util.List<?>)args.getOrDefault("receiverIds",java.util.List.of())).size();
        if("remind_notice".equals(name)) return "提醒通知《"+args.getOrDefault("noticeTitle", "指定通知")+"》的未读人员";
        return "待确认执行："+name;
    }

    private String successMessage(String toolName) {
        if ("create_room_application".equals(toolName)) return "会议室预约申请已提交，当前状态为待审核。";
        if ("publish_notice".equals(toolName)) return "通知已发布。";
        if ("remind_notice".equals(toolName)) return "未读提醒已发送。";
        return "操作已完成。";
    }

    private void log(LoginUser user,AiPendingAction action,AgentTool tool,Object args,Object result,String status,long duration) {
        try {
            AiActionLog log=new AiActionLog();log.setConversationId(action.getConversationId());log.setUserId(user.getUserId());log.setUserType(user.getUserType());
            log.setToolName(tool.getName());log.setRiskLevel(tool.getRiskLevel().name());log.setArgumentsJson(objectMapper.writeValueAsString(args));
            log.setResultJson(objectMapper.writeValueAsString(result));log.setStatus(status);log.setDurationMs(duration);logMapper.insert(log);
        } catch(Exception ignored) {}
    }
}
