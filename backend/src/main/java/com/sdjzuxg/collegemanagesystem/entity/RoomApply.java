package com.sdjzuxg.collegemanagesystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class RoomApply {
    private Integer applyId;
    private MeetingRoom room;
    private Teacher teacher;
    private String applicantName;  // 申请人姓名（管理员申请时使用）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applyTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private Integer applyStatus;
}