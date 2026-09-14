package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.OriginalExamTeacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OriginalExamTeacherMapper {
    int batchInsert(@Param("list") List<OriginalExamTeacher> list);
    int deleteByExamIds(@Param("examIds") List<Integer> examIds);
}
