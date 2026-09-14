package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教师工作量计算结果（表 TEACHER_WORKLOAD）
 * 粒度：教师 × 学期，一人一学期一条汇总记录
 */
@Data
public class WorkloadResult {
    private Integer id;
    private String teacherNo;
    private String teacherName;
    private Integer semesterId;
    private String semesterName;
    private Integer year;
    private Integer studentLimit;
    private BigDecimal totalHours;
    private BigDecimal workloadHours;
    private LocalDateTime calcTime;
}
