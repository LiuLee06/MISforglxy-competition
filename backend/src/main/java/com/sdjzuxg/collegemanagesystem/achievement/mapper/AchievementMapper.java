package com.sdjzuxg.collegemanagesystem.achievement.mapper;

import com.sdjzuxg.collegemanagesystem.achievement.dto.AchievementQueryDTO;
import com.sdjzuxg.collegemanagesystem.achievement.entity.Achievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 成果信息 Mapper。
 * 注意：本模块的 Mapper 绑定到成果专用数据源（本地库 achievement_system），
 * 由 AchievementDataSourceConfig 通过 @MapperScan 指定 sqlSessionFactoryRef 实现，
 * 与现有系统 Mapper（走云端主数据源）互不影响。
 */
@Mapper
public interface AchievementMapper {

    /** 按条件统计总数（供分页使用） */
    long countByConditions(@Param("q") AchievementQueryDTO query);

    /** 按条件分页查询，按提交时间倒序 */
    List<Achievement> selectByConditions(@Param("q") AchievementQueryDTO query);

    Achievement selectById(@Param("id") Long id);

    int insert(Achievement achievement);

    int update(Achievement achievement);

    /** 单条审核（1 通过 2 驳回），同时写入验证时间 */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 批量通过：只处理仍处于待验证(0)的记录，保证幂等 */
    int batchPass(@Param("ids") List<Long> ids);

    int deleteById(@Param("id") Long id);

    int deleteBatch(@Param("ids") List<Long> ids);

    /** 成果展示页：总数 + 各级别数量 */
    long countPassed(@Param("q") AchievementQueryDTO query);

    /** 成果展示页：按成果级别统计 */
    List<Map<String, Object>> selectLevelStats(@Param("q") AchievementQueryDTO query);

    /** 成果展示页：按成果类别统计 */
    List<Map<String, Object>> selectCategoryStats(@Param("q") AchievementQueryDTO query);

    /** 成果展示页：按获得年度统计趋势 */
    List<Map<String, Object>> selectYearStats(@Param("q") AchievementQueryDTO query);

    /** 成果展示页：导出用（不分页） */
    List<Achievement> selectExport(@Param("q") AchievementQueryDTO query);
}
