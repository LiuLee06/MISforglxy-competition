package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.TeacherDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherDeptMapper {
    List<TeacherDept> selectAll();
    TeacherDept selectById(Integer id);
    int insert(TeacherDept teacherDept);
    int update(TeacherDept teacherDept);
    int deleteById(Integer id);
    List<TeacherDept> selectByTeacherId(Integer teacherId);
    // 按教师ID删除所有部门关联
    int deleteByTeacherId(@Param("teacherId") Integer teacherId);
    // 批量插入教师-部门关联
    int batchInsert(@Param("teacherId") Integer teacherId, @Param("deptIds") List<Integer> deptIds);
}
