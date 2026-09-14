package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.OriginalExam;
import com.sdjzuxg.collegemanagesystem.service.OriginalExamService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/original-exam")
public class OriginalExamController {
    @Resource
    private OriginalExamService originalExamService;

    @RequirePermission("plan:import")
    @PostMapping("/batch")
    public Result batchInsert(@RequestBody List<OriginalExam> list) {
        List<OriginalExam> result = originalExamService.batchInsert(list);
        if (result == null) return Result.error();
        if (result.isEmpty()) return Result.error("409", "导入失败：同一学期的考试数据已存在，不可重复导入");
        return Result.success(result);
    }

    @GetMapping("/page-flat")
    public Result pageQueryFlat(ExamQueryDTO queryDTO) {
        return Result.success(originalExamService.pageQueryFlat(queryDTO));
    }

    @GetMapping("/confirm-stats-flat")
    public Result getConfirmStatsFlat(@RequestParam(required = false) Integer semesterId, @RequestParam(required = false) String dept) {
        return Result.success(originalExamService.getConfirmStatsFlat(semesterId, dept));
    }

    /**
     * 教师填写自己的监考确认/异议说明。
     * 数据级权限:params 的调用者身份必须与 CurrentUserUtil 一致,或 admin 代填。
     */
    @PutMapping("/teacher-reason")
    public Result updateTeacherReason(@RequestBody Map<String, Object> params) {
        Object idObj = params.get("id");
        Object teacherIdObj = params.get("teacherId");
        String reason = (String) params.get("reason");

        if (idObj == null || reason == null) return Result.error("400", "参数缺失");

        LoginUser caller = CurrentUserUtil.get();
        if (caller == null) throw new ForbiddenException("未登录");

        Integer id;
        try {
            id = Integer.parseInt(idObj.toString().trim());
        } catch (NumberFormatException e) {
            return Result.error("400", "ID必须为合法数字");
        }

        // 非 admin:保证操作的是自己的记录
        if (!caller.isAdmin()) {
            if (teacherIdObj == null) {
                // 前端未传的话,显式用 CurrentUserUtil 的 teacherId 兜底
                params.put("teacherId", caller.getUserId());
            } else {
                Integer frontTeacherId = Integer.parseInt(teacherIdObj.toString());
                if (!frontTeacherId.equals(caller.getUserId())) {
                    throw new ForbiddenException("无权修改他人的监考说明");
                }
            }
        }

        boolean success = originalExamService.updateTeacherReason(id, reason);
        return success ? Result.success("操作成功") : Result.error();
    }

    @GetMapping("/stats-by-teacher")
    public Result getStatsByTeacher(@RequestParam(required = false) Integer semesterId, @RequestParam(required = false) String dept) {
        return Result.success(originalExamService.getStatsByTeacher(semesterId, dept));
    }

    @GetMapping("/distinct-dept")
    public Result getDistinctDept(@RequestParam(required = false) Integer semesterId) {
        return Result.success(originalExamService.getDistinctDept(semesterId));
    }

    @GetMapping("/export-with-reason")
    public Result exportWithReason() {
        return Result.success(originalExamService.findWithReason());
    }

    @RequirePermission("plan:delete")
    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("400", "请选择要删除的数据");
        return Result.success(originalExamService.batchDelete(ids));
    }
}
