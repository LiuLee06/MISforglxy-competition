package com.sdjzuxg.collegemanagesystem.entity;

import lombok.Data;

@Data
public class MeetingRoom {
    private Integer roomId;
    private String roomName;
    private Integer capacity;
    private Integer roomStatus;
    private Integer managerId;  // 会议室负责人，关联TEACHER表的teacher_id
    private String managerName; // 负责人姓名（关联TEACHER表查询，非数据库列）
}
