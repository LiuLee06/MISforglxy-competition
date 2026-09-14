package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.common.auth.RequireMenu;
import com.sdjzuxg.collegemanagesystem.dto.RoomApplyAuditDTO;
import com.sdjzuxg.collegemanagesystem.entity.RoomApply;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.service.NoticeService;
import com.sdjzuxg.collegemanagesystem.service.RoomApplyService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/room-applies")
public class RoomApplyController {

    @Resource
    private RoomApplyService roomApplyService;

    @Resource
    private NoticeService noticeService;

    @GetMapping
    public Result findAll(@RequestParam(required = false) Integer userId,
                          @RequestParam(required = false) String userType,
                          @RequestParam(required = false) Boolean hasAuditPermission) {
        if (hasAuditPermission != null && hasAuditPermission) {
            return Result.success(roomApplyService.findAll());
        }
        if (userId != null) {
            return Result.success(roomApplyService.findByTeacherId(userId));
        }
        return Result.success(roomApplyService.findAll());
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        RoomApply apply = roomApplyService.findById(id);
        return apply == null ? Result.error("404", "申请不存在") : Result.success(apply);
    }

    @GetMapping("/status/{status}")
    public Result findByStatus(@PathVariable Integer status,
                               @RequestParam(required = false) Integer userId,
                               @RequestParam(required = false) Boolean hasAuditPermission) {
        if (status < 0 || status > 2) {
            return Result.error("400", "状态值无效，取值范围：0-待审核，1-已通过，2-已驳回");
        }
        List<RoomApply> allList;
        if (hasAuditPermission != null && hasAuditPermission) {
            allList = roomApplyService.findAll();
        } else if (userId != null) {
            allList = roomApplyService.findByTeacherId(userId);
        } else {
            allList = roomApplyService.findAll();
        }
        List<RoomApply> filtered = allList.stream()
                .filter(a -> a.getApplyStatus() != null && a.getApplyStatus().equals(status))
                .collect(java.util.stream.Collectors.toList());
        return Result.success(filtered);
    }

    @GetMapping("/teacher/{teacherId}")
    public Result findByTeacherId(@PathVariable Integer teacherId) {
        return Result.success(roomApplyService.findByTeacherId(teacherId));
    }

    @GetMapping("/available")
    public Result findAvailableRooms(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String roomName) {
        return Result.success(roomApplyService.findAvailableRooms(date, startTime, endTime, roomName));
    }

    @GetMapping("/check")
    public Result checkOccupied(
            @RequestParam Integer roomId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return Result.success(!roomApplyService.isRoomOccupied(roomId, date, startTime, endTime));
    }

    /**
     * 提交申请:教师只能代表自己提交。
     * 用 CurrentUserUtil 的 teacherId 覆写 applicant_id,防止前端伪造他人身份申请。
     */
    @PostMapping
    public Result save(@RequestBody RoomApply roomApply) {
        if (roomApply.getRoom() == null || roomApply.getRoom().getRoomId() == null) {
            return Result.error("400", "请选择会议室");
        }
        if (roomApply.getStartTime() == null || roomApply.getEndTime() == null) {
            return Result.error("400", "请选择预约时间");
        }
        if (roomApply.getEndTime().before(roomApply.getStartTime())) {
            return Result.error("400", "结束时间必须晚于开始时间");
        }

        LoginUser caller = CurrentUserUtil.get();
        if (caller == null) throw new ForbiddenException("未登录");

        if (caller.isAdmin()) {
            // admin 申请:applicantName 按项目规则默认 "ADMIN"(Service 落库兜底),teacher 置空
            roomApply.setApplicantName("ADMIN");
            if (roomApply.getTeacher() != null) {
                roomApply.getTeacher().setTeacherId(null);
            }
        } else {
            // 教师身份提交:强制用 CurrentUserUtil 的 teacherId,防前端伪造
            Teacher t = roomApply.getTeacher();
            if (t == null) {
                t = new Teacher();
                roomApply.setTeacher(t);
            }
            t.setTeacherId(caller.getUserId());
        }

        return roomApplyService.save(roomApply) ? Result.success() : Result.error();
    }

    /**
     * 修改申请:只能改自己的、且申请状态为待审核(0)
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody RoomApply roomApply) {
        RoomApply existing = roomApplyService.findById(id);
        if (existing == null) return Result.error("404", "申请不存在");
        assertCanModify(existing, "修改");
        roomApply.setApplyId(id);
        return roomApplyService.update(roomApply) ? Result.success() : Result.error();
    }

    /**
     * 审核申请:与前端规则一致,拥有 /meeting-audit 菜单访问权限即可审核。
     * 切面已保证权限足够,这里直接走 Service 记录 auditorId
     */
    @RequireMenu("/meeting-audit")
    @PutMapping("/{id}/audit")
    public Result audit(@PathVariable Integer id, @RequestBody RoomApplyAuditDTO auditDTO) {
        RoomApply existing = roomApplyService.findById(id);
        if (existing == null) return Result.error("404", "申请不存在");
        if (auditDTO.getStatus() == null) return Result.error("400", "请指定审核状态");
        if (auditDTO.getStatus() != 1 && auditDTO.getStatus() != 2) {
            return Result.error("400", "审核状态无效：1-通过，2-驳回");
        }

        Integer auditorId = auditDTO.getAuditorId();
        if (auditorId == null) {
            LoginUser caller = CurrentUserUtil.get();
            auditorId = caller != null ? caller.getUserId() : 0;
        }

        boolean result = roomApplyService.audit(id, auditDTO.getStatus(), auditorId);
        if (!result) return Result.error("403", "无权限审核该会议室");

        try {
            noticeService.sendRoomApplyResult(existing, auditDTO.getStatus(), auditorId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    /**
     * 删除(取消)申请:只能删自己的、且必须是待审核(0)
     * admin 也可以强制取消任意申请
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        RoomApply existing = roomApplyService.findById(id);
        if (existing == null) return Result.error("404", "申请不存在");
        if (existing.getApplyStatus() != 0) return Result.error("400", "只能取消待审核的申请");
        assertCanModify(existing, "取消");
        return roomApplyService.deleteById(id) ? Result.success() : Result.error();
    }

    /**
     * 断言当前调用者能修改该申请:
     *  - admin 直接放行
     *  - teacher 要求申请的 teacher.teacherId == caller.userId
     */
    private void assertCanModify(RoomApply existing, String action) {
        LoginUser caller = CurrentUserUtil.get();
        if (caller == null) throw new ForbiddenException("未登录");
        if (caller.isAdmin()) return;
        // teacher 只能修改自己的申请
        Integer applicantTeacherId = (existing.getTeacher() != null) ? existing.getTeacher().getTeacherId() : null;
        if (applicantTeacherId == null || !applicantTeacherId.equals(caller.getUserId())) {
            throw new ForbiddenException("无权" + action + "他人的会议室申请");
        }
    }
}
