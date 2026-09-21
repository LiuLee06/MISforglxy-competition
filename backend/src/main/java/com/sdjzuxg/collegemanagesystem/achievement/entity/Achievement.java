package com.sdjzuxg.collegemanagesystem.achievement.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 成果信息（对应成果本地库 achievement_system 的 achievement 表）。
 *
 * 该表为成果管理模块新增表，存于本地库（spring.achievement.datasource 配置的连接），
 * 与现有教学/会议室/通知书等业务的云端库表相互独立，互不影响。
 */
@Data
public class Achievement {
    private Long id;
    /** 成果类别 */
    private String category;
    /** 成果分类等级（随类别级联） */
    private String classifyLevel;
    /** 成果名称 */
    private String name;
    /** 姓名，多人用中文顿号分隔 */
    private String persons;
    /** 成果级别：国家级/省级/市级/校级 */
    private String level;
    /** 成果等级：一等奖/二等奖/三等奖/优秀奖 等 */
    private String grade;
    /** 获得时间（时区必须显式指定 GMT+8，否则默认 UTC 会差一天） */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date achieveDate;
    /** 发证单位 */
    private String issuer;
    /** 比赛名称（可选） */
    private String contestName;
    /** 附件原始文件名 */
    private String fileName;
    /** 附件访问地址 */
    private String fileUrl;
    /** 是否经 OCR 识别填充：0 否 1 是 */
    private Integer ocrFilled;
    /** 状态：0 待验证 1 通过 2 驳回 */
    private Integer status;
    /** 验证时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date verifyTime;
    /** 验证备注 */
    private String verifyRemark;
    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitTime;
    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
