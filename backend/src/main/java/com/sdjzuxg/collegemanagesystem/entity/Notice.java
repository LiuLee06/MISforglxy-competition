package com.sdjzuxg.collegemanagesystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class Notice {
    private Integer noticeId;
    private String title;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
    private Integer publisherId;
    private String publisherType;
    private String noticeType;
    private String publishDept;
    private String attachments;
    private String receiveRoles;
    private String receiveDepts;
    private Integer remindCount;
    private String status;
    private String publisherName;

    @JsonIgnore
    private Teacher publisher;
}