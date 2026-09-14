package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.service.TeacherService;
import com.sdjzuxg.collegemanagesystem.util.PasswordUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.Map;

@RestController
@RequestMapping("/teacher")
public class TeacherController {
    @Resource
    private TeacherService teacherService;

    // ---------- 原有 CRUD 接口 ----------
    @GetMapping
    public Result findAll() {
        return Result.success(teacherService.findAll());
    }

    @GetMapping("/page")
    public Result findByPage(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String searchKey) {
        return Result.success(teacherService.findByPage(pageNum, pageSize, searchKey));
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(teacherService.findById(id));
    }

    @GetMapping("/phone/{phone}")
    public Result findByPhone(@PathVariable String phone) {
        return Result.success(teacherService.findByPhone(phone));
    }

    @GetMapping("/username/{username}")
    public Result findByUsername(@PathVariable String username) {
        return Result.success(teacherService.findByPhone(username));
    }

    @RequirePermission("teacher:delete")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        return teacherService.deleteById(id) ? Result.success() : Result.error();
    }

    @RequirePermission("teacher:add")
    @PostMapping
    public Result save(@RequestBody Teacher teacher) {
        return teacherService.save(teacher) ? Result.success() : Result.error();
    }

    // 不加 @RequirePermission:改自己无需权限;改他人由 Service 数据级校验(需 teacher:edit)
    @PutMapping
    public Result update(@RequestBody Teacher teacher) {
        return teacherService.update(teacher) ? Result.success() : Result.error();
    }

    @GetMapping("/statistics/cards")
    public Result getCards(@RequestParam(required = false) Integer deptId) {
        return Result.success(teacherService.getCardStats(deptId));
    }

    @GetMapping("/statistics/age-distribution")
    public Result getAgeDistribution(@RequestParam(required = false) Integer deptId) {
        return Result.success(teacherService.getAgeDistribution(deptId));
    }

    @GetMapping("/statistics/education-distribution")
    public Result getEducationDistribution(@RequestParam(required = false) Integer deptId) {
        return Result.success(teacherService.getEducationDistribution(deptId));
    }

    @GetMapping("/statistics/dept-statistics")
    public Result getDeptStatistics() {
        return Result.success(teacherService.getDeptStatistics());
    }

    // ---------- 修改密码接口 ----------
    @PostMapping("/change-password")
    public Result changePassword(@RequestBody Map<String, Object> request) {
        String teacherIdStr = (String) request.get("teacherId");
        Integer teacherId = teacherIdStr != null ? Integer.parseInt(teacherIdStr) : null;
        String oldPassword = (String) request.get("oldPassword");
        String newPassword = (String) request.get("newPassword");

        if (teacherId == null || oldPassword == null || newPassword == null) {
            return Result.error("400", "参数不能为空");
        }

        Teacher teacher = teacherService.findById(teacherId);
        if (teacher == null) {
            return Result.error("404", "教师不存在");
        }

        if (!PasswordUtil.matches(oldPassword, teacher.getPassword())) {
            return Result.error("400", "旧密码不正确");
        }

        boolean result = teacherService.updatePassword(teacherId, newPassword);
        if (result) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("400", "密码修改失败");
        }
    }

    // ---------- 管理员重置教师密码接口 ----------
    @AdminOnly
    @PostMapping("/reset-password")
    public Result resetPassword(@RequestBody Map<String, Object> request) {
        Object teacherIdObj = request.get("teacherId");
        if (teacherIdObj == null) {
            return Result.error("400", "教师ID不能为空");
        }
        Integer teacherId = Integer.parseInt(String.valueOf(teacherIdObj));

        Teacher teacher = teacherService.findById(teacherId);
        if (teacher == null) {
            return Result.error("404", "教师不存在");
        }

        boolean result = teacherService.resetPassword(teacherId);
        if (result) {
            return Result.success("密码已重置为默认密码 123456");
        } else {
            return Result.error("400", "重置密码失败");
        }
    }
}