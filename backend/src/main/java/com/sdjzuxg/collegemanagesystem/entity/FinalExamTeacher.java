package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

@Data
public class FinalExamTeacher {
    private Integer id;
    private FinalExam exam;
    private Teacher teacher;
    private String invigilateRole;
}