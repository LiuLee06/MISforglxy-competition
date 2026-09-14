package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.entity.OriginalExamTeacher;
import com.sdjzuxg.collegemanagesystem.service.OriginalExamTeacherService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/original-exam-teacher")
public class OriginalExamTeacherController {
    @Resource
    private OriginalExamTeacherService originalExamTeacherService;

    /**
     * 期初考试监考安排批量导入,对应 plan:import 权限码
     */
    @RequirePermission("plan:import")
    @PostMapping("/batch")
    public Result batchInsert(@RequestBody List<OriginalExamTeacher> teacherList) {
        int count = originalExamTeacherService.batchInsert(teacherList);
        return count > 0 ? Result.success(count) : Result.error();
    }
}
