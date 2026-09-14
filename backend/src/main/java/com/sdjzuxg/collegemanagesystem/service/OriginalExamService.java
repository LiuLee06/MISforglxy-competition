package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.OriginalExam;

import java.util.List;
import java.util.Map;

public interface OriginalExamService {
    List<OriginalExam> findBySemesterId(Integer semesterId);

    PageResult<OriginalExam> pageQueryFlat(ExamQueryDTO queryDTO);
    List<OriginalExam> batchInsert(List<OriginalExam> list);
    List<OriginalExam> findWithReason();
    int batchDelete(List<Integer> ids);
    // 扁平化统计
    Map<String, Object> getConfirmStatsFlat(Integer semesterId, String dept);
    // 更新教师级别反馈
    boolean updateTeacherReason(Integer id, String reason);
    // 按教师统计完成情况
    List<Map<String, Object>> getStatsByTeacher(Integer semesterId, String dept);
    // 获取所有有监考任务的系部列表
    List<String> getDistinctDept(Integer semesterId);
}
