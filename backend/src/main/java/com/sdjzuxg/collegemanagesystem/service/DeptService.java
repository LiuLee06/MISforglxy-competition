package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.Dept;

import java.util.List;

public interface DeptService {
    List<Dept> findAll();
    Dept findById(Integer deptId);
    boolean save(Dept dept);
    boolean update(Dept dept);
    boolean deleteById(Integer deptId);
}
