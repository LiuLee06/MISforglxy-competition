package com.sdjzuxg.collegemanagesystem.achievement.dto;

import lombok.Data;

/**
 * 成果列表查询条件（成果收集/人工验证/成果展示三个页面共用）。
 * 所有字段均可为空，为空表示不过滤。
 */
@Data
public class AchievementQueryDTO {
    private Integer status;
    private String category;
    private String level;
    /** 成果名称模糊查询 */
    private String name;
    /** 姓名模糊查询 */
    private String person;
    /** 获得时间起（yyyy-MM-dd） */
    private String startDate;
    /** 获得时间止（yyyy-MM-dd） */
    private String endDate;
    /** 提交时间起（yyyy-MM-dd），人工验证页按提交时间筛选 */
    private String submitStart;
    /** 提交时间止（yyyy-MM-dd） */
    private String submitEnd;
    /** 页码，从 1 开始 */
    private Integer page = 1;
    /** 每页条数 */
    private Integer size = 10;
    /** 分页偏移量，由 Service 按页码算出后在 SQL 中作为预编译参数使用（避免拼接 SQL） */
    private Integer offset;
}
