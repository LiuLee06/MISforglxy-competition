package com.sdjzuxg.collegemanagesystem.achievement.service.impl;

import com.sdjzuxg.collegemanagesystem.achievement.dict.AchievementDict;
import com.sdjzuxg.collegemanagesystem.achievement.dto.AchievementQueryDTO;
import com.sdjzuxg.collegemanagesystem.achievement.entity.Achievement;
import com.sdjzuxg.collegemanagesystem.achievement.mapper.AchievementMapper;
import com.sdjzuxg.collegemanagesystem.achievement.service.AchievementService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AchievementServiceImpl implements AchievementService {

    @Resource
    private AchievementMapper achievementMapper;

    @Override
    public Map<String, Object> options() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categoryLevels", AchievementDict.CATEGORY_LEVELS);
        data.put("levels", AchievementDict.ACHIEVE_LEVELS);
        data.put("grades", AchievementDict.ACHIEVE_GRADES);
        return data;
    }

    @Override
    public Map<String, Object> page(AchievementQueryDTO query) {
        normalizePage(query);
        List<Achievement> rows = achievementMapper.selectByConditions(query);
        rows.forEach(this::normalizeFileUrl);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", achievementMapper.countByConditions(query));
        data.put("list", rows);
        return data;
    }

    @Override
    public Achievement findById(Long id) {
        Achievement achievement = achievementMapper.selectById(id);
        normalizeFileUrl(achievement);
        return achievement;
    }

    @Override
    public boolean save(Achievement achievement) {
        if (achievement.getOcrFilled() == null) {
            achievement.setOcrFilled(0);
        }
        return achievementMapper.insert(achievement) > 0;
    }

    @Override
    public boolean update(Achievement achievement) {
        return achievementMapper.update(achievement) > 0;
    }

    @Override
    public boolean verify(Long id, Integer status) {
        if (status == null || (status != 1 && status != 2)) {
            return false;
        }
        return achievementMapper.updateStatus(id, status) > 0;
    }

    @Override
    public int batchPass(List<Long> ids) {
        return achievementMapper.batchPass(ids);
    }

    @Override
    public int batchDelete(List<Long> ids) {
        return achievementMapper.deleteBatch(ids);
    }

    @Override
    public boolean deleteById(Long id) {
        return achievementMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> stats(AchievementQueryDTO query) {
        // 成果展示页只统计已通过的记录
        query.setStatus(1);
        query.setPage(null);
        query.setSize(null);
        query.setOffset(null);

        Map<String, Object> data = new LinkedHashMap<>();
        long total = achievementMapper.countPassed(query);
        data.put("total", total);

        // 国家级 / 省级 / 校级 数量，缺失补 0
        Map<String, Long> levelCount = new HashMap<>();
        for (Map<String, Object> row : achievementMapper.selectLevelStats(query)) {
            levelCount.put(String.valueOf(row.get("lvl")), ((Number) row.get("cnt")).longValue());
        }
        data.put("nationalCount", levelCount.getOrDefault("国家级", 0L));
        data.put("provinceCount", levelCount.getOrDefault("省级", 0L));
        data.put("schoolCount", levelCount.getOrDefault("校级", 0L));

        // 图表：级别分布 / 类别分布 / 年度趋势
        data.put("byLevel", toChartData(achievementMapper.selectLevelStats(query), "lvl"));
        data.put("byCategory", toChartData(achievementMapper.selectCategoryStats(query), "cate"));
        data.put("byYear", toChartData(achievementMapper.selectYearStats(query), "yr"));
        return data;
    }

    @Override
    public List<Achievement> exportList(AchievementQueryDTO query) {
        query.setPage(null);
        query.setSize(null);
        query.setOffset(null);
        List<Achievement> rows = achievementMapper.selectExport(query);
        rows.forEach(this::normalizeFileUrl);
        return rows;
    }

    /**
     * 附件访问地址归一：历史数据中遗留的 /uploads/xxx 统一改成本项目静态映射的 /upload/xxx，
     * 保证新旧附件在页面上都能正常预览读取（只读转换，不落库）。
     */
    private void normalizeFileUrl(Achievement achievement) {
        if (achievement == null) {
            return;
        }
        String url = achievement.getFileUrl();
        if (url == null || url.isEmpty()) {
            return;
        }
        String lower = url.toLowerCase();
        int idx = lower.indexOf("uploads/");
        if (idx >= 0) {
            String tail = url.substring(idx + "uploads/".length());
            achievement.setFileUrl("/upload/" + tail);
        }
    }

    /** 统一图表数据格式：[{name: xxx, value: n}] */
    private List<Map<String, Object>> toChartData(List<Map<String, Object>> rows, String nameKey) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object name = row.get(nameKey);
            if (name == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", String.valueOf(name));
            item.put("value", ((Number) row.get("cnt")).longValue());
            list.add(item);
        }
        return list;
    }

    /** 页码兜底并计算偏移量（偏移量作为预编译参数传入，避免拼接 SQL） */
    private void normalizePage(AchievementQueryDTO query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : Math.min(query.getSize(), 200);
        query.setPage(page);
        query.setSize(size);
        query.setOffset((page - 1) * size);
    }
}
