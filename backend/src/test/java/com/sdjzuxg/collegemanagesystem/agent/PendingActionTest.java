package com.sdjzuxg.collegemanagesystem.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdjzuxg.collegemanagesystem.agent.entity.AiPendingAction;
import com.sdjzuxg.collegemanagesystem.agent.mapper.AiActionLogMapper;
import com.sdjzuxg.collegemanagesystem.agent.mapper.AiPendingActionMapper;
import com.sdjzuxg.collegemanagesystem.agent.service.impl.AgentActionServiceImpl;
import com.sdjzuxg.collegemanagesystem.agent.tool.*;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PendingActionTest {
    @Test
    void writeToolIsNotExecutedWhenPendingActionIsCreated() {
        AiPendingActionMapper mapper=mock(AiPendingActionMapper.class);
        AiActionLogMapper logMapper=mock(AiActionLogMapper.class);
        when(mapper.insert(any())).thenAnswer(invocation -> { ((AiPendingAction)invocation.getArgument(0)).setActionId(31L); return 1; });
        AtomicBoolean executed=new AtomicBoolean(false);
        AgentTool tool=new BuiltinAgentTool("create_room_application","",ToolRiskLevel.WRITE_CONFIRM,Map.of(),"提交会议室预约",false,a -> {executed.set(true);return AgentToolResult.ok(Map.of());});
        AgentActionServiceImpl service=new AgentActionServiceImpl(mapper,logMapper,new ObjectMapper());
        var pending=service.create(12L,new LoginUser(7,"teacher",java.util.List.of()),tool,Map.of("roomId",3,"date","2026-09-15","startTime","14:00","endTime","16:00","purpose","会议"));
        assertEquals(31L,pending.getActionId());
        assertFalse(executed.get());
        verify(mapper,never()).claim(anyLong(),anyInt(),anyString());
    }
}
