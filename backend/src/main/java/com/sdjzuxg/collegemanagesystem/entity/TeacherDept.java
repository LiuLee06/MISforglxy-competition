package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

@Data
public class TeacherDept {
    private Integer id;
    private Teacher teacher;
    private Dept dept;
}
