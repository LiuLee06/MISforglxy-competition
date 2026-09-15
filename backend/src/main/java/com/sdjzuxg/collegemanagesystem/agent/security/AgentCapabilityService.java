package com.sdjzuxg.collegemanagesystem.agent.security;

import com.sdjzuxg.collegemanagesystem.agent.tool.AgentTool;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuActionMapper;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuMapper;
import org.springframework.stereotype.Service;

@Service
public class AgentCapabilityService {
    private final RoleMenuMapper roleMenuMapper;
    private final RoleMenuActionMapper roleMenuActionMapper;

    public AgentCapabilityService(RoleMenuMapper roleMenuMapper, RoleMenuActionMapper roleMenuActionMapper) {
        this.roleMenuMapper=roleMenuMapper; this.roleMenuActionMapper=roleMenuActionMapper;
    }

    public boolean canUse(LoginUser user, AgentTool tool) {
        if (user == null || tool == null || tool.getRiskLevel() == com.sdjzuxg.collegemanagesystem.agent.tool.ToolRiskLevel.FORBIDDEN) return false;
        if (user.isAdmin()) return true;
        if (tool.isAdminOnly()) return false;
        if (tool.getRequiredMenu()!=null && roleMenuMapper.existsByTeacherIdAndMenuUrl(user.getUserId(),tool.getRequiredMenu())<=0) return false;
        if (tool.getRequiredPermission()!=null && roleMenuActionMapper.existsByTeacherIdAndCode(user.getUserId(),tool.getRequiredPermission())<=0) return false;
        return true;
    }
}
