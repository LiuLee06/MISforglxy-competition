package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Semester;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SemesterMapper {
    List<Semester> selectAll();
    Semester selectById(Integer semesterId);
    List<Semester> selectByName(String semesterName);
    Semester selectCurrent();
    int insert(Semester semester);
    int update(Semester semester);
    int deleteById(Integer semesterId);
}
