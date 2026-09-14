package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.dto.TeachingScheduleUpdateDTO;
import com.sdjzuxg.collegemanagesystem.entity.TeachingSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TeachingScheduleMapper {
    
    List<TeachingSchedule> findByConditions(@Param("teacherName") String teacherName, 
                                            @Param("courseName") String courseName,
                                            @Param("major") String major,
                                            @Param("semesterId") Integer semesterId,
                                            @Param("confirmStatus") Integer confirmStatus);
    
    /*
    List<TeachingScheduleUpdateDTO> findEditableFieldsByTeacher(@Param("teacherName") String teacherName);
    */
    
    int updateEditableFields(TeachingScheduleUpdateDTO updateDTO);

    int confirmByNoticeId(@Param("noticeId") String noticeId);

    /*
    List<TeachingSchedule> findAll();
    */

    TeachingSchedule findByNoticeId(@Param("noticeId") String noticeId);

    int insert(TeachingSchedule teachingSchedule);

    int update(TeachingSchedule teachingSchedule);

    int batchUpsert(@Param("list") List<TeachingSchedule> list);

    int deleteByNoticeId(@Param("noticeId") String noticeId);

    int deleteBySemesterId(@Param("semesterId") Integer semesterId);
}
