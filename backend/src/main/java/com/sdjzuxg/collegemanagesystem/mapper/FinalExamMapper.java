package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.FinalExam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinalExamMapper {
    List<FinalExam> selectBySemesterId(Integer semesterId);

    // 条件分页查询
    List<FinalExam> selectByCondition(@Param("query") ExamQueryDTO query);
    // 条件查询总数
    long countByCondition(@Param("query") ExamQueryDTO query);
    // 批量插入（生成定稿用）
    int batchInsert(@Param("list") List<FinalExam> list);
    // 批量删除
    int batchDeleteByIds(@Param("ids") List<Integer> ids);
}
