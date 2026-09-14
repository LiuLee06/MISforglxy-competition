package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

@Data
public class OriginalExamTeacher {
    private Integer id;
    private OriginalExam exam;
    private Teacher teacher;
    private String invigilateRole;
    private String reason;
}