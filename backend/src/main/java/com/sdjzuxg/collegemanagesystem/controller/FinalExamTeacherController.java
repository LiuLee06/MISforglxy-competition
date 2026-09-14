package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.entity.FinalExamTeacher;
import com.sdjzuxg.collegemanagesystem.service.FinalExamTeacherService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/final-exam-teacher")
public class FinalExamTeacherController {
    @Resource
    private FinalExamTeacherService finalExamTeacherService;

    /**
     * 期末考试监考安排批量导入,对应 plan:import 权限码
     */
    @RequirePermission("plan:import")
    @PostMapping("/batch")
    public Result batchInsert(@RequestBody List<FinalExamTeacher> teacherList) {
        boolean flag = finalExamTeacherService.batchInsert(teacherList);
        return flag ? Result.success() : Result.error();
    }
}
