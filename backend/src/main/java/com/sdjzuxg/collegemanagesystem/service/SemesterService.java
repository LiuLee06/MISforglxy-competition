package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.Semester;

import java.util.List;

public interface SemesterService {
    List<Semester> findAll();
    Semester findById(Integer semesterId);
    List<Semester> findByName(String semesterName);
    Semester findCurrent();
    boolean save(Semester semester);
    boolean update(Semester semester);
    boolean deleteById(Integer semesterId);
}
