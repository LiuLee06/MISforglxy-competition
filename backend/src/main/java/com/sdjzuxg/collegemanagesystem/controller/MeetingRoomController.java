package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;
import com.sdjzuxg.collegemanagesystem.service.MeetingRoomService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 会议室管理 Controller (RESTful API)
 */
@RestController
@RequestMapping("/meeting-rooms")
public class MeetingRoomController {

    @Resource
    private MeetingRoomService meetingRoomService;

    /**
     * GET /api/meeting-rooms - 查询所有会议室
     */
    @GetMapping
    public Result findAll() {
        return Result.success(meetingRoomService.findAll());
    }

    /**
     * GET /api/meeting-rooms/{id} - 查询单个会议室
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        MeetingRoom room = meetingRoomService.findById(id);
        if (room == null) {
            return Result.error("404", "会议室不存在");
        }
        return Result.success(room);
    }

    /**
     * GET /api/meeting-rooms/available - 查询可用会议室图4.121添加会议室功能顺序图
     */
    @GetMapping("/available")
    public Result findAvailable() {
        return Result.success(meetingRoomService.findAvailable());
    }

    /**
     * POST /api/meeting-rooms - 新增会议室
     */
    @RequirePermission("room:add")
    @PostMapping
    public Result save(@RequestBody MeetingRoom meetingRoom) {
        // 1. 非空校验
        if (meetingRoom.getRoomName() == null || meetingRoom.getRoomName().trim().isEmpty()) {
            return Result.error("400", "会议室名称不能为空");
        }
        if (meetingRoom.getCapacity() == null || meetingRoom.getCapacity() <= 0) {
            return Result.error("400", "容纳人数必须大于0");
        }

        // 2. 名称唯一性校验（新增时）
        if (meetingRoomService.isRoomNameExist(meetingRoom.getRoomName().trim())) {
            return Result.error("400", "该会议室名称已存在，请勿重复添加");
        }

        // 3. 保存
        return meetingRoomService.save(meetingRoom) ? Result.success() : Result.error();
    }

    /**
     * PUT /api/meeting-rooms/{id} - 修改会议室
     */
    @RequirePermission("room:edit")
    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody MeetingRoom meetingRoom) {
        // 1. 检查会议室是否存在
        MeetingRoom existing = meetingRoomService.findById(id);
        if (existing == null) {
            return Result.error("404", "会议室不存在");
        }

        // 2. 非空校验
        if (meetingRoom.getRoomName() == null || meetingRoom.getRoomName().trim().isEmpty()) {
            return Result.error("400", "会议室名称不能为空");
        }
        if (meetingRoom.getCapacity() == null || meetingRoom.getCapacity() <= 0) {
            return Result.error("400", "容纳人数必须大于0");
        }

        // 3. 名称唯一性校验（修改时，排除自身）
        String newName = meetingRoom.getRoomName().trim();
        if (!newName.equals(existing.getRoomName())) {
            if (meetingRoomService.isRoomNameExistExcludeSelf(newName, id)) {
                return Result.error("400", "该会议室名称已被使用，请更换");
            }
        }

        // 4. 更新
        meetingRoom.setRoomId(id);
        return meetingRoomService.update(meetingRoom) ? Result.success() : Result.error();
    }

    /**
     * DELETE /api/meeting-rooms/{id} - 删除会议室
     */
    @RequirePermission("room:delete")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        MeetingRoom existing = meetingRoomService.findById(id);
        if (existing == null) {
            return Result.error("404", "会议室不存在");
        }
        return meetingRoomService.deleteById(id) ? Result.success() : Result.error();
    }
}