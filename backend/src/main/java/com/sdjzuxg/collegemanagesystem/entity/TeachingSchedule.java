package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

/**
 * 教学计划表实体类
 * 对应数据库表：TEACHING_SCHEDULE
 */
@Data
public class TeachingSchedule {

    private String noticeId; // 教学计划通知单唯一标识
    private String courseCode; // 学校统一课程编号
    private String courseName; // 课程中文全称
    private String teacherName; // 主讲教师姓名（支持多位教师用顿号分隔）
    private String className; // 上课班级名称
    private String courseSeq; // 同一课程的不同课程序号
    private String selectedStudentCount; // 实际选课学生人数
    private String credit; // 课程学分
    private String scheduledHours; // 已完成排课的学时数
    private String offeringDepartment; // 开设课程的学院/部门名称
    private String teacherTitle; // 教师专业技术职务
    private String courseCategory; // 课程分类
    private String courseNature; // 课程性质
    private String courseAttribute; // 课程属性
    private String campus; // 上课所在校区
    private String teacherId; // 教师工号
    private String planHourUnit; // 计划学时的计量单位
    private String groupName; // 课程分组名称
    private String scheduledStudentCount; // 排课系统设定人数
    private String actualClassSize; // 班级实际在册人数
    private Integer isDegreeCourse; // 是否学位课：0-否，1-是
    private String lectureWeeks; // 理论课上课周次
    private String labWeeks; // 上机课上课周次
    private String totalPlanHours; // 课程总计划学时
    private String experimentWeeks; // 实验课上课周次
    private String practiceWeeks; // 实践环节周次
    private String otherWeeks; // 其他教学环节周次
    private String lectureHours; // 理论课学时数
    private String labHours; // 上机课学时数
    private String experimentHours; // 实验课学时数
    private String practiceWeeksCount; // 集中实践周数
    private String arrangedHours; // 已安排的总学时数
    private String otherHours; // 其他教学环节学时数
    private String weeklyHours; // 每周平均学时数
    private String functionalArea; // 排课功能区及教室信息
    private Integer confirmStatus; // 确认状态：0-待确认，1-已确认
    private Integer semesterId; // 学期ID
    private String major; // 专业
}
