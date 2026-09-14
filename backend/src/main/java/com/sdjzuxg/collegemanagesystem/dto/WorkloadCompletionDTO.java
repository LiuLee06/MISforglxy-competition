package com.sdjzuxg.collegemanagesystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkloadCompletionDTO {
    private Long totalTeachers;
    private Long completedTeachers;
    private Long uncompletedTeachers;
    private Long totalRecords;
    private Long confirmedRecords;
    private Long pendingRecords;
    private Double completionRate;
    private List<WorkloadTeacherCompletionDTO> teacherDetails;
}
