package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.WorkloadResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkloadResultMapper {

    List<WorkloadResult> selectResults(@Param("semesterId") Integer semesterId);

    /** 学年聚合视图：同一学年（两个学期共用同一 year 值）按教师合并成一条 */
    List<WorkloadResult> selectResultsGroupedByYear(@Param("year") Integer year);

    List<WorkloadResult> selectMyResults(@Param("currentTeacherName") String currentTeacherName,
                                         @Param("semesterId") Integer semesterId);

    int deleteBySemesterId(@Param("semesterId") Integer semesterId);

    int batchInsert(@Param("list") List<WorkloadResult> list);
}
