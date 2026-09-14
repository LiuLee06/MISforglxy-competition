package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.OriginalExam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OriginalExamMapper {
    List<OriginalExam> selectBySemesterId(Integer semesterId);

    // 批量导入插入
    int batchInsert(@Param("list") List<OriginalExam> list);
    // 查询有意见反馈的数据
    List<OriginalExam> selectWithReason();
    // 批量删除
    int batchDeleteByIds(@Param("ids") List<Integer> ids);
    // 扁平化查询（基于监考教师表）
    List<OriginalExam> selectByConditionFlat(@Param("query") ExamQueryDTO query);
    long countByConditionFlat(@Param("query") ExamQueryDTO query);
    Map<String, Object> countConfirmStatsFlat(@Param("semesterId") Integer semesterId, @Param("dept") String dept);
    // 基于监考教师记录的更新反馈
    int updateTeacherReason(@Param("id") Integer id, @Param("reason") String reason);
    // 按教师统计完成情况
    List<Map<String, Object>> countStatsByTeacher(@Param("semesterId") Integer semesterId, @Param("dept") String dept);
    // 获取所有有监考任务的系部列表
    List<String> selectDistinctDept(@Param("semesterId") Integer semesterId);
}
