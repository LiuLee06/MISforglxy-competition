package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;
import com.sdjzuxg.collegemanagesystem.mapper.MeetingRoomMapper;
import com.sdjzuxg.collegemanagesystem.service.MeetingRoomService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class MeetingRoomServiceImpl implements MeetingRoomService {

    @Resource
    private MeetingRoomMapper meetingRoomMapper;

    @Override
    public List<MeetingRoom> findAll() {
        return meetingRoomMapper.selectAll();
    }

    @Override
    public MeetingRoom findById(Integer roomId) {
        return meetingRoomMapper.selectById(roomId);
    }

    @Override
    public List<MeetingRoom> findAvailable() {
        return meetingRoomMapper.selectAvailable();
    }

    @Override
    public boolean save(MeetingRoom meetingRoom) {

        if (meetingRoom.getRoomStatus() == null) {
            meetingRoom.setRoomStatus(1);
        }
        return meetingRoomMapper.insert(meetingRoom) > 0;
    }

    @Override
    public boolean update(MeetingRoom meetingRoom) {
        return meetingRoomMapper.update(meetingRoom) > 0;
    }

    @Override
    public boolean deleteById(Integer roomId) {
        return meetingRoomMapper.deleteById(roomId) > 0;
    }

    @Override
    public boolean isRoomNameExist(String roomName) {
        return meetingRoomMapper.countByName(roomName) > 0;
    }

    @Override
    public boolean isRoomNameExistExcludeSelf(String roomName, Integer roomId) {
        return meetingRoomMapper.countByNameExcludeSelf(roomName, roomId) > 0;
    }
}