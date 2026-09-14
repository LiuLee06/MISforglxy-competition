package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.TeacherDept;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherDeptMapper;
import com.sdjzuxg.collegemanagesystem.service.TeacherDeptService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class TeacherDeptServiceImpl implements TeacherDeptService {
    @Resource
    private TeacherDeptMapper teacherDeptMapper;

    @Override
    public List<TeacherDept> findAll() {
        return teacherDeptMapper.selectAll();
    }

    @Override
    public TeacherDept findById(Integer id) {
        return teacherDeptMapper.selectById(id);
    }

    @Override
    public boolean save(TeacherDept teacherDept) {
        return teacherDeptMapper.insert(teacherDept) > 0;
    }

    @Override
    public boolean update(TeacherDept teacherDept) {
        return teacherDeptMapper.update(teacherDept) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return teacherDeptMapper.deleteById(id) > 0;
    }

    @Override
    public List<TeacherDept> findByTeacherId(Integer teacherId) {
        return teacherDeptMapper.selectByTeacherId(teacherId);
    }
}
