package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OriginalExam {
    private Integer examId;
    private String courseName;
    private Semester semester;
    private Date startTime;
    private Date endTime;
    private String room;
    private String status;
    private String reason;
    private String teachingCampus;
    private String examCampus;
    private String teachingDept;
    private String teachingTeacher;
    private String invigilationCollege;
    private String className;
    private Integer studentCount;
    // 用于扁平展示
    private String teacherName;
    private String invigilateRole;
    private Integer originalExamTeacherId;
}
