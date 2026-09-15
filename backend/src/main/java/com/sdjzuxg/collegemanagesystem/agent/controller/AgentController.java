package com.sdjzuxg.collegemanagesystem.agent.controller;

import com.sdjzuxg.collegemanagesystem.agent.dto.AgentChatRequest;
import com.sdjzuxg.collegemanagesystem.agent.orchestrator.AgentOrchestrator;
import com.sdjzuxg.collegemanagesystem.agent.service.AgentActionService;
import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/agent")
public class AgentController {
    private final AgentOrchestrator orchestrator;
    public AgentController(AgentOrchestrator orchestrator){this.orchestrator=orchestrator;}

    @PostMapping("/chat")
    public Result chat(@RequestBody AgentChatRequest request){return Result.success(orchestrator.chat(request));}

    @GetMapping("/conversations")
    public Result conversations(){LoginUser u=CurrentUserUtil.get();return Result.success(orchestrator.conversations(u));}

    @GetMapping("/conversations/{conversationId}/messages")
    public Result messages(@PathVariable Long conversationId){LoginUser u=CurrentUserUtil.get();return Result.success(orchestrator.messages(conversationId,u));}

    @DeleteMapping("/conversations/{conversationId}")
    public Result deleteConversation(@PathVariable Long conversationId){
        LoginUser u=CurrentUserUtil.get();
        return orchestrator.deleteConversation(conversationId,u)
                ? Result.success("会话已删除") : Result.error("404","会话不存在或无权删除");
    }

    @PostMapping("/actions/{actionId}/confirm")
    public Result confirm(@PathVariable Long actionId){
        AgentActionService.ActionExecutionResult r=orchestrator.confirm(actionId);
        if(!r.success() && "FORBIDDEN".equals(r.status()))return Result.error("403",r.message());
        Map<String,Object> body=new java.util.LinkedHashMap<>(); body.put("status",r.status()); body.put("success",r.success()); body.put("message",r.message());
        return Result.success(body);
    }

    @PostMapping("/actions/{actionId}/cancel")
    public Result cancel(@PathVariable Long actionId){
        boolean ok=orchestrator.cancel(actionId);
        return ok?Result.success(Map.of("status","CANCELLED","message","操作已取消")):Result.error("400","操作不存在、已执行或已过期");
    }
}
