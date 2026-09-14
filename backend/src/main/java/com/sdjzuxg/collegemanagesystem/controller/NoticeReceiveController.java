package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.entity.NoticeReceive;
import com.sdjzuxg.collegemanagesystem.service.NoticeReceiveService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/notice-receive")
public class NoticeReceiveController {
    @Resource
    private NoticeReceiveService noticeReceiveService;

    @GetMapping
    public Result findAll() {
        return Result.success(noticeReceiveService.findAll());
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(noticeReceiveService.findById(id));
    }

    @GetMapping("/teacher/{teacherId}")
    public Result findByTeacherId(@PathVariable Integer teacherId) {
        return Result.success(noticeReceiveService.findByTeacherId(teacherId));
    }

    @GetMapping("/stats/{noticeId}")
    public Result getReadStats(@PathVariable Integer noticeId) {
        return Result.success(noticeReceiveService.getReadStats(noticeId));
    }

    @GetMapping("/list/{noticeId}")
    public Result getReadList(@PathVariable Integer noticeId) {
        return Result.success(noticeReceiveService.getReadList(noticeId));
    }

    // 获取用户的通知统计（未读、待办、待审核会议室）
    @GetMapping("/counts")
    public Result getCounts(@RequestParam Integer teacherId,
                            @RequestParam(required = false, defaultValue = "false") Boolean hasAuditPermission) {
        // 只能查自己的未读统计/待办统计
        LoginUser caller = CurrentUserUtil.get();
        if (caller == null) throw new ForbiddenException("未登录");
        if (!caller.isAdmin() && (caller.getUserId() == null || !caller.getUserId().equals(teacherId))) {
            throw new ForbiddenException("无权查看他人的通知统计");
        }
        return Result.success(noticeReceiveService.getCounts(teacherId, hasAuditPermission));
    }

    // ===== CRUD 只允许 admin 直接操作 =====

    @AdminOnly
    @PostMapping
    public Result save(@RequestBody NoticeReceive noticeReceive) {
        return noticeReceiveService.save(noticeReceive) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody NoticeReceive noticeReceive) {
        return noticeReceiveService.update(noticeReceive) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        return noticeReceiveService.deleteById(id) ? Result.success() : Result.error();
    }

    // ===== 教师端的状态变更:只能改自己的 =====

    /**
     * 标记自己的一条通知为已读。
     * teacherId 必须等于调用者 ID(admin 也允许代操作任意人,用于审计)
     */
    @PutMapping("/mark-read")
    public Result markAsRead(@RequestParam Integer noticeId, @RequestParam Integer teacherId) {
        assertSelfOrAdmin(teacherId, "标记他人的通知为已读");
        return noticeReceiveService.markAsRead(noticeId, teacherId) ? Result.success() : Result.error();
    }

    @PutMapping("/mark-todo")
    public Result markAsTodo(@RequestParam Integer noticeId, @RequestParam Integer teacherId) {
        assertSelfOrAdmin(teacherId, "标记他人的通知为待办");
        return noticeReceiveService.markAsTodo(noticeId, teacherId) ? Result.success() : Result.error();
    }

    @PutMapping("/unmark-todo")
    public Result unmarkTodo(@RequestParam Integer noticeId, @RequestParam Integer teacherId) {
        assertSelfOrAdmin(teacherId, "取消他人的通知待办");
        return noticeReceiveService.unmarkTodo(noticeId, teacherId) ? Result.success() : Result.error();
    }

    /**
     * 提醒未读教师:仅允许 admin 或该通知的发布人操作
     */
    @PutMapping("/remind/{noticeId}")
    public Result remindUnread(@PathVariable Integer noticeId) {
        LoginUser caller = CurrentUserUtil.get();
        if (caller == null) throw new ForbiddenException("未登录");
        // admin 放行;其他调用者交给 Service 进一步校验是否为发布人
        if (!caller.isAdmin()) {
            // teacher 调用 remind:这里先不做细查,Service 层判断是否为发布人
        }
        boolean ok = noticeReceiveService.remindUnread(noticeId);
        return ok ? Result.success() : Result.error();
    }

    private void assertSelfOrAdmin(Integer targetTeacherId, String forbiddenMsg) {
        LoginUser caller = CurrentUserUtil.get();
        if (caller == null) throw new ForbiddenException("未登录");
        if (caller.isAdmin()) return;
        if (targetTeacherId == null || !targetTeacherId.equals(caller.getUserId())) {
            throw new ForbiddenException("无权" + forbiddenMsg);
        }
    }
}
