package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.entity.Admin;
import com.sdjzuxg.collegemanagesystem.entity.Menu;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.entity.TeacherRole;
import com.sdjzuxg.collegemanagesystem.mapper.AdminMapper;
import com.sdjzuxg.collegemanagesystem.mapper.MenuMapper;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuActionMapper;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherMapper;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherRoleMapper;
import com.sdjzuxg.collegemanagesystem.util.JwtUtil; // 新增导入
import com.sdjzuxg.collegemanagesystem.util.PasswordUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/login")
public class LoginController {

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

    @PostMapping
    public Result login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        // 登录方式：phone（默认，手机号）/ name（姓名）/ staffNo（工号）
        String loginType = loginForm.get("loginType");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return Result.error("400", "账号和密码不能为空");
        }

        // ========== 1. 管理员登录 ==========
        Admin admin = adminMapper.selectByUsername(username);
        if (admin != null && PasswordUtil.matches(password, admin.getPassword())) {
            // 【修复 NPE】防止角色为空
            Integer roleId = (admin.getRole() != null) ? admin.getRole().getRoleId() : 2;
            String roleName = (admin.getRole() != null) ? admin.getRole().getRoleName() : "普通管理员";

            List<Menu> menus;
            List<String> actionCodes;
            if (roleId != null && roleId == 1) {
                menus = menuMapper.selectAll();
                actionCodes = roleMenuActionMapper.selectAllActionCodes();
            } else {
                menus = menuMapper.selectByRoleId(roleId);
                actionCodes = roleMenuActionMapper.selectActionCodesByRoleId(roleId);
            }
            List<String> menuUrls = menus.stream().map(Menu::getMenuUrl).filter(url -> url != null && !url.isEmpty()).distinct().collect(Collectors.toList());

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", admin.getAdminId());
            userInfo.put("username", admin.getUsername());
            userInfo.put("name", admin.getUsername());
            userInfo.put("roleId", roleId);
            userInfo.put("roleName", roleName);
            userInfo.put("userType", "admin");
            userInfo.put("menus", menuUrls);
            userInfo.put("actionCodes", actionCodes);

            // 【修复闪退】手动生成 Token
            List<Integer> adminRoleIds = roleId != null ? List.of(roleId) : new ArrayList<>();
            userInfo.put("token", JwtUtil.generate(admin.getAdminId(), "admin", adminRoleIds));

            return Result.success(userInfo);
        }

        // ========== 2. 教师登录 ==========
        // 【修复工号登录】根据 loginType 分别按手机号、姓名、工号查询
        Teacher teacher;
        if ("name".equals(loginType)) {
            teacher = teacherMapper.selectByName(username);
        } else if ("staffNo".equals(loginType)) {
            teacher = teacherMapper.selectByStaffNo(username);
        } else {
            teacher = teacherMapper.selectByPhone(username);
        }

        if (teacher != null && PasswordUtil.matches(password, teacher.getPassword())) {
            List<TeacherRole> teacherRoles = teacherRoleMapper.selectByTeacherId(teacher.getTeacherId());
            if (teacherRoles.isEmpty()) {
                return Result.error("403", "该教师未分配角色，请联系管理员");
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

            // 【修复闪退】手动生成 Token
            userInfo.put("token", JwtUtil.generate(teacher.getTeacherId(), "teacher", roleIds));

            return Result.success(userInfo);
        }

        return Result.error("401", "账号或密码错误");
    }

    @PostMapping("/check-permission")
    public Result checkPermission(@RequestBody Map<String, Object> request) {
        Integer roleId = (Integer) request.get("roleId");
        String path = (String) request.get("path");

        if (roleId == null || path == null) {
            return Result.error("400", "参数不能为空");
        }

        List<Menu> menus = menuMapper.selectByRoleId(roleId);
        List<String> menuUrls = menus.stream().map(Menu::getMenuUrl).filter(url -> url != null && !url.isEmpty()).collect(Collectors.toList());

        boolean hasPermission = menuUrls.contains(path);
        return Result.success(hasPermission);
    }

    @GetMapping("/menus/{roleId}")
    public Result getMenusByRoleId(@PathVariable Integer roleId) {
        List<Menu> menus = menuMapper.selectByRoleId(roleId);
        return Result.success(menus);
    }
}