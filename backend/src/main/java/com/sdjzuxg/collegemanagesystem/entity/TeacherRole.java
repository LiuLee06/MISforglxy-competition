package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

@Data
public class TeacherRole {
    private Integer teacherId;
    private Integer roleId;
    private Integer oldRoleId;
    private Teacher teacher;
    private Role role;
}
