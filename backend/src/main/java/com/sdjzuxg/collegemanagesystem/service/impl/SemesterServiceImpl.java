package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Semester;
import com.sdjzuxg.collegemanagesystem.mapper.SemesterMapper;
import com.sdjzuxg.collegemanagesystem.service.SemesterService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class SemesterServiceImpl implements SemesterService {
    @Resource
    private SemesterMapper semesterMapper;

    @Override
    public List<Semester> findAll() {
        return semesterMapper.selectAll();
    }

    @Override
    public Semester findById(Integer semesterId) {
        return semesterMapper.selectById(semesterId);
    }

    @Override
    public List<Semester> findByName(String semesterName) {
        return semesterMapper.selectByName(semesterName);
    }

    @Override
    public Semester findCurrent() {
        return semesterMapper.selectCurrent();
    }

    @Override
    public boolean save(Semester semester) {
        return semesterMapper.insert(semester) > 0;
    }

    @Override
    public boolean update(Semester semester) {
        return semesterMapper.update(semester) > 0;
    }

    @Override
    public boolean deleteById(Integer semesterId) {
        return semesterMapper.deleteById(semesterId) > 0;
    }
}
