package com.sdjzuxg.collegemanagesystem.dto;

import lombok.Data;

/**
 * 教学计划可编辑字段DTO
 * 用于查询和更新教师可编辑的字段数据
 */
@Data
public class TeachingScheduleUpdateDTO {
    private String noticeId;        // 通知ID（用于定位记录）
    private String teacherName;     // 教师姓名
    private String lectureWeeks;    // 理论课周次
    private String experimentWeeks; // 实验课周次
    private String practiceWeeks;   // 实践环节周次
    private String labWeeks;        // 上机课周次
    private String weeklyHours;     // 每周学时
    private String functionalArea;  // 功能区教室
}