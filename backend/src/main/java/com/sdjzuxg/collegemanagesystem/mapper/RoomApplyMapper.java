package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.RoomApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoomApplyMapper {
    List<RoomApply> selectAll();
    RoomApply selectById(Integer applyId);
    List<RoomApply> selectByTeacherId(Integer teacherId);
    List<RoomApply> selectByStatus(Integer status);
    List<RoomApply> findConflicts(@Param("roomId") Integer roomId,
                                  @Param("date") String date,
                                  @Param("startTime") String startTime,
                                  @Param("endTime") String endTime);
    int insert(RoomApply roomApply);
    int update(RoomApply roomApply);
    int updateStatus(@Param("applyId") Integer applyId, @Param("status") Integer status);
    int deleteById(Integer applyId);
}