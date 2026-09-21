package com.sdjzuxg.collegemanagesystem.achievement.dict;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 成果字典（类别/分类等级级联、级别、等级）。
 * 与成果收集表单里的下拉选项一一对应。
 */
public class AchievementDict {

    /** 成果类别 -> 可选成果分类等级 */
    public static final Map<String, List<String>> CATEGORY_LEVELS = new LinkedHashMap<>();
    /** 成果级别 */
    public static final List<String> ACHIEVE_LEVELS = Arrays.asList("国家级", "省级", "市级", "校级");
    /** 成果等级 */
    public static final List<String> ACHIEVE_GRADES = Arrays.asList("特等奖", "一等奖", "二等奖", "三等奖", "优秀奖", "优秀", "合格", "其他");

    static {
        // 依据《教学业绩分类评价体系》表1~表7 的「分类等级」列整理
        CATEGORY_LEVELS.put("教学成果获奖", Arrays.asList("一类A", "一类", "二类A", "二类", "三类", "四类"));
        CATEGORY_LEVELS.put("教材建设成果", Arrays.asList("一类A", "一类", "二类", "三类", "四类", "五类"));
        CATEGORY_LEVELS.put("教学研究项目", Arrays.asList("一类A", "一类", "二类", "三类A", "三类", "四类", "五类", "六类"));
        CATEGORY_LEVELS.put("教学研究论文", Arrays.asList("一类", "二类", "三类", "四类", "五类"));
        CATEGORY_LEVELS.put("学科专业建设成果", Arrays.asList("一类", "二类", "三类", "四类"));
        CATEGORY_LEVELS.put("教师职业能力", Arrays.asList("一类A", "一类", "二类", "三类", "四类", "五类"));
        CATEGORY_LEVELS.put("指导学生竞赛获奖", Arrays.asList("一类", "二类", "三类", "四类", "五类", "六类"));
    }

    private AchievementDict() {
    }
}
