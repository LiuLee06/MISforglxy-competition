package com.sdjzuxg.collegemanagesystem.dto;

import lombok.Data;

/**
 * 会议室申请审核 DTO
 */
@Data
public class RoomApplyAuditDTO {
    private Integer status;  // 1-通过, 2-驳回
    private Integer auditorId;  // 审核人ID（0表示超级管理员）
    private String remark;   // 审核备注（可选）
}