package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.dto.TeachingScheduleUpdateDTO;
import com.sdjzuxg.collegemanagesystem.entity.TeachingSchedule;
import com.sdjzuxg.collegemanagesystem.service.TeachingScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/teaching-schedule")
public class TeachingScheduleController {

    @Resource
    private TeachingScheduleService teachingScheduleService;

    @GetMapping
    public Result list(
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) Integer semesterId,
            @RequestParam(required = false) Integer confirmStatus) {
        return Result.success(teachingScheduleService.findByConditions(
                teacherName, courseName, major, semesterId, confirmStatus));
    }

    /*
    @GetMapping("/editable")
    public Result getEditableFields(@RequestParam String teacherName) {
        return Result.success(teachingScheduleService.findEditableFieldsByTeacher(teacherName));
    }
    */

    @GetMapping("/{noticeId}")
    public Result findByNoticeId(@PathVariable String noticeId) {
        return Result.success(teachingScheduleService.findByNoticeId(noticeId));
    }

    /**
     * 管理员设置某条教学任务的"可编辑字段"。
     */
    @AdminOnly
    @PutMapping("/{noticeId}/editable-fields")
    public ResponseEntity<Result> updateEditableFields(@PathVariable String noticeId,
                                                        @RequestBody TeachingScheduleUpdateDTO updateDTO) {
        updateDTO.setNoticeId(noticeId);
        try {
            boolean success = teachingScheduleService.updateEditableFields(updateDTO);
            return success ? ResponseEntity.ok(Result.success())
                    : ResponseEntity.badRequest().body(Result.error("400", "更新失败，未找到对应记录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Result.error("500", "系统错误: " + e.getMessage()));
        }
    }

    /**
     * 教师确认自己名下的教学任务。
     * 数据级权限:Service 层根据 currentTeacherName 从 TEACHER 表查 ID 再匹配。
     * 这里不贴 actionCode,任何已登录教师都能确认自己的。
     */
    @PutMapping("/{noticeId}/confirmation")
    public ResponseEntity<Result> confirm(@PathVariable String noticeId) {
        try {
            boolean success = teachingScheduleService.confirmByNoticeId(noticeId);
            return success ? ResponseEntity.ok(Result.success())
                    : ResponseEntity.badRequest().body(Result.error("400", "更新失败，未找到对应记录"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Result.error("500", "系统错误: " + e.getMessage()));
        }
    }

    @AdminOnly
    @PostMapping
    public Result save(@RequestBody TeachingSchedule teachingSchedule) {
        return teachingScheduleService.save(teachingSchedule) ? Result.success() : Result.error();
    }

    /**
     * 批量导入教学任务,对应 plan:import 权限码
     */
    @RequirePermission("plan:import")
    @PostMapping("/imports")
    public Result batchUpsert(@RequestBody List<TeachingSchedule> list) {
        return teachingScheduleService.batchUpsert(list)
                ? Result.success(list.size())
                : Result.error("400", "导入数据不能为空");
    }

    @AdminOnly
    @DeleteMapping(params = "semesterId")
    public Result deleteBySemesterId(@RequestParam Integer semesterId) {
        try {
            int deleted = teachingScheduleService.deleteBySemesterId(semesterId);
            return Result.success(deleted);
        } catch (IllegalArgumentException e) {
            return Result.error("400", e.getMessage());
        }
    }

    @AdminOnly
    @PutMapping("/{noticeId}")
    public Result update(@PathVariable String noticeId, @RequestBody TeachingSchedule teachingSchedule) {
        teachingSchedule.setNoticeId(noticeId);
        return teachingScheduleService.update(teachingSchedule) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/{noticeId}")
    public Result deleteByNoticeId(@PathVariable String noticeId) {
        return teachingScheduleService.deleteByNoticeId(noticeId) ? Result.success() : Result.error();
    }
}
