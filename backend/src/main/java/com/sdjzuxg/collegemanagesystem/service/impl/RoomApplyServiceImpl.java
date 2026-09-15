package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;
import com.sdjzuxg.collegemanagesystem.entity.RoomApply;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.mapper.MeetingRoomMapper;
import com.sdjzuxg.collegemanagesystem.mapper.RoomApplyMapper;
import com.sdjzuxg.collegemanagesystem.service.RoomApplyService;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
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
        return saveForCurrentUser(roomApply, com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil.get());
    }

    @Override
    @Transactional
    public boolean saveForCurrentUser(RoomApply roomApply, LoginUser user) {
        if (user == null) throw new IllegalArgumentException("未登录");
        if (roomApply == null || roomApply.getRoom() == null || roomApply.getRoom().getRoomId() == null
                || roomApply.getRoom().getRoomId() <= 0) throw new IllegalArgumentException("请选择会议室");
        if (roomApply.getPurpose() == null || roomApply.getPurpose().trim().isEmpty()) throw new IllegalArgumentException("预约用途不能为空");
        if (roomApply.getPurpose().trim().length() > 500) throw new IllegalArgumentException("预约用途不能超过500字");
        MeetingRoom room = meetingRoomMapper.selectByIdForUpdate(roomApply.getRoom().getRoomId());
        if (room == null) throw new IllegalArgumentException("会议室不存在");
        if (!Integer.valueOf(1).equals(room.getRoomStatus())) throw new IllegalArgumentException("会议室当前不可用");
        if (roomApply.getStartTime() == null || roomApply.getEndTime() == null
                || !roomApply.getEndTime().after(roomApply.getStartTime())) throw new IllegalArgumentException("结束时间必须晚于开始时间");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        String day = dateFormat.format(roomApply.getStartTime());
        String endDay = dateFormat.format(roomApply.getEndTime());
        if (!day.equals(endDay)) throw new IllegalArgumentException("预约必须在同一天内");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        if (!roomApplyMapper.findConflicts(room.getRoomId(), day, timeFormat.format(roomApply.getStartTime()), timeFormat.format(roomApply.getEndTime())).isEmpty())
            throw new IllegalArgumentException("会议室当前时段已被占用");
        roomApply.setRoom(room);
        roomApply.setApplyTime(new Date());
        roomApply.setApplyStatus(0);
        if (user.isAdmin()) {
            roomApply.setTeacher(null);
            roomApply.setApplicantName("ADMIN");
        } else {
            Teacher teacher = new Teacher();
            teacher.setTeacherId(user.getUserId());
            roomApply.setTeacher(teacher);
            roomApply.setApplicantName(null);
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
