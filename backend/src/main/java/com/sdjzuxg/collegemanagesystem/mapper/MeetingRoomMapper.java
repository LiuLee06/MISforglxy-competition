package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MeetingRoomMapper {

    List<MeetingRoom> selectAll();

    MeetingRoom selectById(@Param("roomId") Integer roomId);

    List<MeetingRoom> selectAvailable();

    int insert(MeetingRoom meetingRoom);

    int update(MeetingRoom meetingRoom);

    int deleteById(@Param("roomId") Integer roomId);

    // 名称唯一性校验：新增时用
    int countByName(@Param("roomName") String roomName);

    // 名称唯一性校验：修改时排除自身
    int countByNameExcludeSelf(@Param("roomName") String roomName, @Param("roomId") Integer roomId);
}