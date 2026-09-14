package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Dept;
import com.sdjzuxg.collegemanagesystem.mapper.DeptMapper;
import com.sdjzuxg.collegemanagesystem.service.DeptService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class DeptServiceImpl implements DeptService {
    @Resource
    private DeptMapper deptMapper;

    @Override
    public List<Dept> findAll() {
        return deptMapper.selectAll();
    }

    @Override
    public Dept findById(Integer deptId) {
        return deptMapper.selectById(deptId);
    }

    @Override
    public boolean save(Dept dept) {
        return deptMapper.insert(dept) > 0;
    }

    @Override
    public boolean update(Dept dept) {
        return deptMapper.update(dept) > 0;
    }

    @Override
    public boolean deleteById(Integer deptId) {
        return deptMapper.deleteById(deptId) > 0;
    }
}
