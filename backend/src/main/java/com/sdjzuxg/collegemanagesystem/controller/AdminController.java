package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.AuthException;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.entity.Admin;
import com.sdjzuxg.collegemanagesystem.service.AdminService;
import com.sdjzuxg.collegemanagesystem.util.PasswordUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminService adminService;

    @GetMapping
    @AdminOnly
    public Result findAll() {
        return Result.success(adminService.findAll());
    }

    @GetMapping("/{id}")
    @AdminOnly
    public Result findById(@PathVariable Integer id) {
        return Result.success(adminService.findById(id));
    }

    @GetMapping("/username/{username}")
    @AdminOnly
    public Result findByUsername(@PathVariable String username) {
        return Result.success(adminService.findByUsername(username));
    }

    @AdminOnly
    @PostMapping
    public Result save(@RequestBody Admin admin) {
        return adminService.save(admin) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody Admin admin) {
        return adminService.update(admin) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        return adminService.deleteById(id) ? Result.success() : Result.error();
    }

    /**
     * 修改自己的密码。任何登录的管理员都能改自己的密码,所以不走 @AdminOnly,
     * 而是校验 CurrentUserUtil 中的调用者 adminId 与请求体 adminId 一致。
     */
    @PostMapping("/change-password")
    public Result changePassword(@RequestBody Map<String, Object> request) {
        String adminIdStr = (String) request.get("adminId");
        Integer adminId = adminIdStr != null ? Integer.parseInt(adminIdStr) : null;
        String oldPassword = (String) request.get("oldPassword");
        String newPassword = (String) request.get("newPassword");

        if (adminId == null || oldPassword == null || newPassword == null) {
            return Result.error("400", "参数不能为空");
        }

        LoginUser user = CurrentUserUtil.get();
        if (user == null || !"admin".equals(user.getUserType())) {
            throw new AuthException("未登录或登录态无效");
        }
        if (!user.getUserId().equals(adminId)) {
            // 越权改别人密码:只有超级管理员 roleId=1 可改其他管理员
            if (user.getRoleIds() == null || !user.getRoleIds().contains(1)) {
                throw new AuthException("无权修改其他管理员的密码");
            }
        }

        Admin admin = adminService.findById(adminId);
        if (admin == null) {
            return Result.error("404", "管理员不存在");
        }

        if (!PasswordUtil.matches(oldPassword, admin.getPassword())) {
            return Result.error("400", "旧密码不正确");
        }

        boolean result = adminService.updatePassword(adminId, newPassword);
        return result ? Result.success("密码修改成功") : Result.error("400", "密码修改失败");
    }
}
