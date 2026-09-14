package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Workload {
    private Integer wlId;
    private Integer semesterId;
    private String semesterName;
    private String teacherNo;
    private String teacherName;
    private String courseNo;
    private String courseName;
    private String hourType;
    private String notificationNo;
    private BigDecimal transferHours;
    private BigDecimal actualHours;
    private BigDecimal totalHours;
    private BigDecimal totalCredit;
    private BigDecimal actualHourRatio;
    private Integer studentCount;
    private String className;
    private Integer confirmStatus;
}
