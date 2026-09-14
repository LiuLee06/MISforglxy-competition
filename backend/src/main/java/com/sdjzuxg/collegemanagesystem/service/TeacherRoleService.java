package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.entity.TeacherRole;

import java.util.List;

public interface TeacherRoleService {
    List<TeacherRole> findAll();
    boolean save(TeacherRole teacherRole);
    boolean updateRole(TeacherRole teacherRole);
    boolean deleteByTeacherIdAndRoleId(Integer teacherId, Integer roleId);
    List<TeacherRole> findByTeacherId(Integer teacherId);
    List<TeacherRole> findByRoleId(Integer roleId);
    PageResult<TeacherRole> findByPage(Integer pageNum, Integer pageSize, Integer roleId,
                                       Integer teacherId, String teacherName, String searchMode);
}
