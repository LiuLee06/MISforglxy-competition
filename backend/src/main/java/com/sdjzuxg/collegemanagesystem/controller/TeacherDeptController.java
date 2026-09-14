package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.TeacherDept;
import com.sdjzuxg.collegemanagesystem.service.TeacherDeptService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/teacher-dept")
public class TeacherDeptController {
    @Resource
    private TeacherDeptService teacherDeptService;

    @GetMapping
    public Result findAll() {
        return Result.success(teacherDeptService.findAll());
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(teacherDeptService.findById(id));
    }

    @GetMapping("/teacher/{teacherId}")
    public Result findByTeacherId(@PathVariable Integer teacherId) {
        return Result.success(teacherDeptService.findByTeacherId(teacherId));
    }

    /**
     * 直接新建教师-部门关联(绕过 TeacherServiceImpl.syncTeacherDept 流程)。
     * 仅限 admin;普通的教师部门变更应该走 PUT /teacher 更新 TEACHER 触发同步
     */
    @AdminOnly
    @PostMapping
    public Result save(@RequestBody TeacherDept teacherDept) {
        return teacherDeptService.save(teacherDept) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody TeacherDept teacherDept) {
        return teacherDeptService.update(teacherDept) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        return teacherDeptService.deleteById(id) ? Result.success() : Result.error();
    }
}
