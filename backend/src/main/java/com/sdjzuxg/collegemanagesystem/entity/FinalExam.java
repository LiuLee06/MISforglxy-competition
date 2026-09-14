package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class FinalExam {
    private Integer examId;
    private String courseName;
    private Semester semester;
    private Date startTime;
    private Date endTime;
    private String room;
    private String status;
    private List<Teacher> teachers;
    private String mainTeacherName;
    private String subTeacherName;
    private String subTeacher2Name;
    private String subTeacher3Name;
    private String teachingCampus;
    private String examCampus;
    private String teachingDept;
    private String teachingTeacher;
    private String invigilationCollege;
    private String className;
    private Integer studentCount;
}
