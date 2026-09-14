package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Admin;
import com.sdjzuxg.collegemanagesystem.entity.Menu;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.entity.TeacherRole;
import com.sdjzuxg.collegemanagesystem.mapper.AdminMapper;
import com.sdjzuxg.collegemanagesystem.mapper.MenuMapper;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuActionMapper;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherMapper;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherRoleMapper;
import com.sdjzuxg.collegemanagesystem.service.LoginService;
import com.sdjzuxg.collegemanagesystem.util.JwtUtil;
import com.sdjzuxg.collegemanagesystem.util.PasswordUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private TeacherRoleMapper teacherRoleMapper;

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuActionMapper roleMenuActionMapper;

    @Override
    public Map<String, Object> login(String username, String password, String loginType) {
        // 1. 管理员登录：按账号查询
        Admin admin = adminMapper.selectByUsername(username);
        if (admin != null && PasswordUtil.matches(password, admin.getPassword())) {
            return buildAdminUserInfo(admin);
        }

        // 2. 教师登录：根据 loginType 分别按手机号、姓名、工号查询
        Teacher teacher;
        if ("name".equals(loginType)) {
            teacher = teacherMapper.selectByName(username);
        } else if ("staffNo".equals(loginType)) {
            teacher = teacherMapper.selectByStaffNo(username);
        } else {
            teacher = teacherMapper.selectByPhone(username);
        }

        if (teacher != null && PasswordUtil.matches(password, teacher.getPassword())) {
            return buildTeacherUserInfo(teacher);
        }

        // 3. 账号或密码错误
        return null;
    }

    private Map<String, Object> buildAdminUserInfo(Admin admin) {
        // 修复 NPE：判断 role 是否为空
        Integer roleId = (admin.getRole() != null) ? admin.getRole().getRoleId() : 2; // 给一个默认角色ID（如2），防止空指针

        List<Menu> menus;
        List<String> actionCodes;
        if (roleId != null && roleId == 1) {
            menus = menuMapper.selectAll();
            actionCodes = roleMenuActionMapper.selectAllActionCodes();
        } else {
            menus = menuMapper.selectByRoleId(roleId);
            actionCodes = roleMenuActionMapper.selectActionCodesByRoleId(roleId);
        }
        List<String> menuUrls = menus.stream()
                .map(Menu::getMenuUrl)
                .filter(url -> url != null && !url.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", admin.getAdminId());
        userInfo.put("username", admin.getUsername());
        userInfo.put("name", admin.getUsername());
        userInfo.put("roleId", roleId);
        // 防止 roleName 空指针
        userInfo.put("roleName", admin.getRole() != null ? admin.getRole().getRoleName() : "普通管理员");
        userInfo.put("userType", "admin");
        userInfo.put("menus", menuUrls);
        userInfo.put("actionCodes", actionCodes);

        // 修复缺少 Token 的问题：管理员也要发 Token
        List<Integer> adminRoleIds = roleId != null ? List.of(roleId) : new ArrayList<>();
        userInfo.put("token", JwtUtil.generate(admin.getAdminId(), "admin", adminRoleIds));

        return userInfo;
    }

    private Map<String, Object> buildTeacherUserInfo(Teacher teacher) {
        List<TeacherRole> teacherRoles = teacherRoleMapper.selectByTeacherId(teacher.getTeacherId());
        if (teacherRoles.isEmpty()) {
            throw new RuntimeException("该教师未分配角色，请联系管理员");
        }

        List<String> menuUrls;
        List<Integer> roleIds = new ArrayList<>();
        List<String> roleNames = new ArrayList<>();
        List<String> actionCodes;

        boolean teacherIsSuperAdmin = false;
        for (TeacherRole teacherRole : teacherRoles) {
            Integer roleId = teacherRole.getRoleId();
            if (roleId != null && roleId == 1) {
                teacherIsSuperAdmin = true;
                break;
            }
        }

        if (teacherIsSuperAdmin) {
            List<Menu> allMenus = menuMapper.selectAll();
            menuUrls = allMenus.stream().map(Menu::getMenuUrl).filter(url -> url != null && !url.isEmpty()).distinct().collect(Collectors.toList());
            for (TeacherRole teacherRole : teacherRoles) {
                roleIds.add(teacherRole.getRoleId());
                roleNames.add(teacherRole.getRole().getRoleName());
            }
            actionCodes = roleMenuActionMapper.selectAllActionCodes();
        } else {
            menuUrls = new ArrayList<>();
            for (TeacherRole teacherRole : teacherRoles) {
                Integer roleId = teacherRole.getRoleId();
                roleIds.add(roleId);
                roleNames.add(teacherRole.getRole().getRoleName());
                List<Menu> menus = menuMapper.selectByRoleId(roleId);
                List<String> roleMenuUrls = menus.stream().map(Menu::getMenuUrl).filter(url -> url != null && !url.isEmpty()).collect(Collectors.toList());
                menuUrls.addAll(roleMenuUrls);
            }
            menuUrls = menuUrls.stream().distinct().collect(Collectors.toList());
            actionCodes = roleMenuActionMapper.selectActionCodesByRoleIds(roleIds);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", teacher.getTeacherId());
        userInfo.put("username", teacher.getPhone());
        userInfo.put("name", teacher.getName());
        userInfo.put("dept", teacher.getDept());
        userInfo.put("professionalTitle", teacher.getProfessionalTitle());
        userInfo.put("roleId", roleIds);
        userInfo.put("roleName", roleNames);
        userInfo.put("userType", "teacher");
        userInfo.put("menus", menuUrls);
        userInfo.put("actionCodes", actionCodes);
        userInfo.put("token", JwtUtil.generate(teacher.getTeacherId(), "teacher", roleIds));
        return userInfo;
    }

    @Override
    public boolean checkPermission(Integer roleId, String path) {
        List<Menu> menus = menuMapper.selectByRoleId(roleId);
        List<String> menuUrls = menus.stream().map(Menu::getMenuUrl).filter(url -> url != null && !url.isEmpty()).collect(Collectors.toList());
        return menuUrls.contains(path);
    }

    @Override
    public List<Menu> getMenusByRoleId(Integer roleId) {
        return menuMapper.selectByRoleId(roleId);
    }
}