package com.sdjzuxg.collegemanagesystem.dto;
import com.sdjzuxg.collegemanagesystem.common.PageQuery;
import lombok.Data;

@Data
public class ExamQueryDTO extends PageQuery {
    private String courseName;
    private Integer semesterId;
    private String status;
    private String room;
    private Integer teacherId;
    private String teacherName;
    private String startTime;
    private String endTime;
    private String dept;
}