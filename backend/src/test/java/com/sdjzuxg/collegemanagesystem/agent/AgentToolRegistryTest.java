package com.sdjzuxg.collegemanagesystem.agent;

import com.sdjzuxg.collegemanagesystem.agent.security.AgentCapabilityService;
import com.sdjzuxg.collegemanagesystem.agent.tool.*;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuActionMapper;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentToolRegistryTest {
    private AgentTool tool(String name, boolean adminOnly) {
        return new BuiltinAgentTool(name, name, adminOnly ? ToolRiskLevel.WRITE_CONFIRM : ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of(), List.of()), name, adminOnly, a -> AgentToolResult.ok(Map.of()));
    }

    @Test
    void unknownToolIsRejectedAndCapabilitiesAreFiltered() {
        AgentToolRegistry registry = new AgentToolRegistry(
                List.of(tool("read_tool", false), tool("admin_tool", true)),
                new AgentCapabilityService(mock(RoleMenuMapper.class), mock(RoleMenuActionMapper.class)));
        LoginUser teacher = new LoginUser(7, "teacher", List.of(2));
        LoginUser admin = new LoginUser(1, "admin", List.of());
        assertNotNull(registry.get("read_tool"));
        assertNull(registry.get("not_registered"));
        assertEquals(List.of("read_tool"), registry.getAvailable(teacher).stream().map(AgentTool::getName).toList());
        assertEquals(2, registry.getAvailable(admin).size());
    }

    @Test
    void toolMustDeclareAndPassItsMenuPermission() {
        var menus = mock(RoleMenuMapper.class);
        var actions = mock(RoleMenuActionMapper.class);
        when(menus.existsByTeacherIdAndMenuUrl(7, "/notice-publish")).thenReturn(1);
        AgentTool notice = new BuiltinAgentTool("publish_notice", "", ToolRiskLevel.WRITE_CONFIRM,
                AgentToolRegistry.schema(Map.of(), List.of()), "发布通知", "/notice-publish", null, false,
                args -> AgentToolResult.ok(Map.of()));
        AgentToolRegistry registry = new AgentToolRegistry(List.of(notice), new AgentCapabilityService(menus, actions));
        assertEquals("/notice-publish", notice.definition().getRequiredMenu());
        assertEquals(List.of(notice), registry.getAvailable(new LoginUser(7, "teacher", List.of())));
        when(menus.existsByTeacherIdAndMenuUrl(7, "/notice-publish")).thenReturn(0);
        assertEquals(List.of(), registry.getAvailable(new LoginUser(7, "teacher", List.of())));
    }
}
