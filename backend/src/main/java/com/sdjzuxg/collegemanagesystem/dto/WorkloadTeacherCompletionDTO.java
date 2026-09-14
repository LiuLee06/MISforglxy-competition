package com.sdjzuxg.collegemanagesystem.dto;

import lombok.Data;

@Data
public class WorkloadTeacherCompletionDTO {
    private String teacherNo;
    private String teacherName;
    private Long totalRecords;
    private Long confirmedRecords;
    private Long pendingRecords;
    private Double completionRate;
    private boolean completed;
}
