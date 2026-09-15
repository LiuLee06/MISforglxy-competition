package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;
import com.sdjzuxg.collegemanagesystem.entity.RoomApply;

import java.util.List;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;

public interface RoomApplyService {
    List<RoomApply> findAll();
    RoomApply findById(Integer applyId);
    List<RoomApply> findByTeacherId(Integer teacherId);
    List<RoomApply> findPending();
    List<RoomApply> findByStatus(Integer status);
    List<MeetingRoom> findAvailableRooms(String date, String startTime, String endTime, String roomName);
    boolean isRoomOccupied(Integer roomId, String date, String startTime, String endTime);
    boolean save(RoomApply roomApply);
    boolean saveForCurrentUser(RoomApply roomApply, LoginUser user);
    boolean audit(Integer applyId, Integer status, Integer auditorId);
    boolean update(RoomApply roomApply);
    boolean deleteById(Integer applyId);

    // 新增：根据用户类型获取申请列表（管理员看全部，教师看自己的）
    List<RoomApply> getAppliesByUser(Integer userId, String userType);
}
