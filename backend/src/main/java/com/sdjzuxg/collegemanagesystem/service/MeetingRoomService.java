package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;

import java.util.List;

public interface MeetingRoomService {
    List<MeetingRoom> findAll();
    MeetingRoom findById(Integer roomId);
    List<MeetingRoom> findAvailable();
    boolean save(MeetingRoom meetingRoom);
    boolean update(MeetingRoom meetingRoom);
    boolean deleteById(Integer roomId);
    boolean isRoomNameExist(String roomName);
    boolean isRoomNameExistExcludeSelf(String roomName, Integer roomId);

}