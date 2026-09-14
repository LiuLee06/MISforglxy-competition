package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.FinalExamTeacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinalExamTeacherMapper {
    int batchInsert(@Param("list") List<FinalExamTeacher> list);
    int countExistByExamAndTeacher(@Param("examId") Integer examId, @Param("teacherId") Integer teacherId, @Param("invigilateRole") String invigilateRole);
    int deleteByExamIds(@Param("examIds") List<Integer> examIds);
}
