package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.TeacherDept;

import java.util.List;

public interface TeacherDeptService {
    List<TeacherDept> findAll();
    TeacherDept findById(Integer id);
    boolean save(TeacherDept teacherDept);
    boolean update(TeacherDept teacherDept);
    boolean deleteById(Integer id);
    List<TeacherDept> findByTeacherId(Integer teacherId);
}
