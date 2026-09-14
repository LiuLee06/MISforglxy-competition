package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;
import com.sdjzuxg.collegemanagesystem.entity.RoomApply;
import com.sdjzuxg.collegemanagesystem.mapper.MeetingRoomMapper;
import com.sdjzuxg.collegemanagesystem.mapper.RoomApplyMapper;
import com.sdjzuxg.collegemanagesystem.service.RoomApplyService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomApplyServiceImpl implements RoomApplyService {
    @Resource
    private RoomApplyMapper roomApplyMapper;

    @Resource
    private MeetingRoomMapper meetingRoomMapper;

    @Override
    public List<RoomApply> findAll() {
        return roomApplyMapper.selectAll();
    }

    @Override
    public RoomApply findById(Integer applyId) {
        return roomApplyMapper.selectById(applyId);
    }

    @Override
    public List<RoomApply> findByTeacherId(Integer teacherId) {
        return roomApplyMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<RoomApply> findPending() {
        return roomApplyMapper.selectByStatus(0);
    }

    @Override
    public List<RoomApply> findByStatus(Integer status) {
        return roomApplyMapper.selectByStatus(status);
    }

    @Override
    public List<MeetingRoom> findAvailableRooms(String date, String startTime, String endTime, String roomName) {
        List<MeetingRoom> allRooms = meetingRoomMapper.selectAvailable();

        if (roomName != null && !roomName.isEmpty()) {
            allRooms = allRooms.stream()
                    .filter(r -> r.getRoomName().contains(roomName))
                    .collect(Collectors.toList());
        }

        if (date == null || startTime == null || endTime == null) {
            return allRooms;
        }

        return allRooms.stream()
                .filter(room -> !isRoomOccupied(room.getRoomId(), date, startTime, endTime))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isRoomOccupied(Integer roomId, String date, String startTime, String endTime) {
        List<RoomApply> conflicts = roomApplyMapper.findConflicts(roomId, date, startTime, endTime);
        return !conflicts.isEmpty();
    }

    @Override
    public boolean save(RoomApply roomApply) {
        roomApply.setApplyTime(new Date());
        roomApply.setApplyStatus(0);

        if (roomApply.getTeacher() != null && roomApply.getTeacher().getTeacherId() != null
                && roomApply.getTeacher().getTeacherId() == -1) {
            roomApply.setTeacher(null);
            if (roomApply.getApplicantName() == null || roomApply.getApplicantName().isEmpty()) {
                roomApply.setApplicantName("ADMIN");
            }
        }

        return roomApplyMapper.insert(roomApply) > 0;
    }

    @Override
    public boolean audit(Integer applyId, Integer status, Integer auditorId) {
        RoomApply apply = roomApplyMapper.selectById(applyId);
        if (apply == null) {
            return false;
        }

        // 申请关联的会议室必须存在
        MeetingRoom room = meetingRoomMapper.selectById(apply.getRoom().getRoomId());
        if (room == null) {
            return false;
        }

        // ========== 情况1：已通过 → 驳回（撤销） ==========
        // 拥有 /meeting-audit 菜单权限的人均可撤销（切面已校验，不再判断是否为会议室负责人）
        if (apply.getApplyStatus() == 1 && status == 2) {
            return roomApplyMapper.updateStatus(applyId, status) > 0;
        }

        // ========== 情况2：待审核 → 通过/驳回 ==========
        if (apply.getApplyStatus() != 0) {
            return false;
        }

        // 有 room:audit 权限的人均可审核所有会议室，不再判断是否为会议室负责人
        return roomApplyMapper.updateStatus(applyId, status) > 0;
    }

    @Override
    public boolean update(RoomApply roomApply) {
        return roomApplyMapper.update(roomApply) > 0;
    }

    @Override
    public boolean deleteById(Integer applyId) {
        return roomApplyMapper.deleteById(applyId) > 0;
    }

    @Override
    public List<RoomApply> getAppliesByUser(Integer userId, String userType) {
        if (userId == null) {
            return roomApplyMapper.selectAll();
        }
        if ("admin".equals(userType) || userId == 0) {
            return roomApplyMapper.selectAll();
        }
        return roomApplyMapper.selectByTeacherId(userId);
    }
}