package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.FinalExam;
import com.sdjzuxg.collegemanagesystem.service.FinalExamService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/final-exam")
public class FinalExamController {
    @Resource
    private FinalExamService finalExamService;

    @GetMapping("/page")
    public Result pageQuery(ExamQueryDTO queryDTO) {
        return Result.success(finalExamService.pageQuery(queryDTO));
    }

    @RequirePermission("plan:import")
    @PostMapping("/batch")
    public Result batchInsert(@RequestBody List<FinalExam> list) {
        List<FinalExam> result = finalExamService.batchInsert(list);
        if (result == null) return Result.error();
        if (result.isEmpty()) return Result.error("409", "导入失败：同一学期的监考数据已存在，不可重复导入");
        return Result.success(result);
    }

    @RequirePermission("plan:delete")
    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("400", "请选择要删除的数据");
        return Result.success(finalExamService.batchDelete(ids));
    }
}
