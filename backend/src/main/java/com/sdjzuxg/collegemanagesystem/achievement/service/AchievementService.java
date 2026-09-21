package com.sdjzuxg.collegemanagesystem.achievement.service;

import com.sdjzuxg.collegemanagesystem.achievement.dto.AchievementQueryDTO;
import com.sdjzuxg.collegemanagesystem.achievement.entity.Achievement;

import java.util.List;
import java.util.Map;

public interface AchievementService {

    /** 下拉字典：类别->分类等级、级别、等级 */
    Map<String, Object> options();

    /** 分页列表（返回 total + list） */
    Map<String, Object> page(AchievementQueryDTO query);

    Achievement findById(Long id);

    boolean save(Achievement achievement);

    boolean update(Achievement achievement);

    /** 单条审核：1 通过 2 驳回 */
    boolean verify(Long id, Integer status);

    /** 批量通过（正式入库），只处理待验证记录 */
    int batchPass(List<Long> ids);

    /** 批量删除 */
    int batchDelete(List<Long> ids);

    boolean deleteById(Long id);

    /** 成果展示页统计（总数 + 国家级/省级/校级 + 类别分布 + 级别分布 + 年度趋势） */
    Map<String, Object> stats(AchievementQueryDTO query);

    /** 导出数据（不分页） */
    List<Achievement> exportList(AchievementQueryDTO query);
}
