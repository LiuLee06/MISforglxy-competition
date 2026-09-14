package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

@Data
public class Semester {
    private Integer semesterId;
    private String semesterName;
    private Integer year;
    private Integer isCurrent;
}
