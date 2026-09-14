package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.TeacherRole;
import com.sdjzuxg.collegemanagesystem.service.TeacherRoleService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/teacher-role")
public class TeacherRoleController {
    @Resource
    private TeacherRoleService teacherRoleService;

    @GetMapping
    public Result findAll() {
        return Result.success(teacherRoleService.findAll());
    }

    @GetMapping("/page")
    public Result findByPage(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String searchMode) {
        PageResult<TeacherRole> result = teacherRoleService.findByPage(pageNum, pageSize, roleId, teacherId, teacherName, searchMode);
        return Result.success(result);
    }

    @GetMapping("/teacher/{teacherId}")
    public Result findByTeacherId(@PathVariable Integer teacherId) {
        return Result.success(teacherRoleService.findByTeacherId(teacherId));
    }

    @GetMapping("/role/{roleId}")
    public Result findByRoleId(@PathVariable Integer roleId) {
        return Result.success(teacherRoleService.findByRoleId(roleId));
    }

    @AdminOnly
    @PostMapping
    public Result save(@RequestBody TeacherRole teacherRole) {
        return teacherRoleService.save(teacherRole) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result updateRole(@RequestBody TeacherRole teacherRole) {
        return teacherRoleService.updateRole(teacherRole) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping
    public Result deleteByTeacherIdAndRoleId(
            @RequestParam Integer teacherId,
            @RequestParam Integer roleId) {
        return teacherRoleService.deleteByTeacherIdAndRoleId(teacherId, roleId) ? Result.success() : Result.error();
    }
}
