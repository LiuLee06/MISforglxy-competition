package com.sdjzuxg.collegemanagesystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class NoticeReceive {
    private Integer id;
    private Notice notice;
    private Teacher teacher;
    private Integer isReceived;
    private Integer isTodo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmTime;
}
