package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Workload;
import com.sdjzuxg.collegemanagesystem.dto.WorkloadTeacherCompletionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WorkloadMapper {
    List<Workload> selectByConditions(@Param("teacherNo") String teacherNo,
                                      @Param("teacherName") String teacherName,
                                      @Param("exactTeacherName") String exactTeacherName,
                                      @Param("courseName") String courseName,
                                      @Param("semesterId") Integer semesterId,
                                      @Param("confirmStatus") Integer confirmStatus);

    List<Workload> selectMyRelated(@Param("currentTeacherName") String currentTeacherName,
                                   @Param("courseName") String courseName,
                                   @Param("teacherName") String teacherName,
                                   @Param("semesterId") Integer semesterId);

    List<Workload> selectMyRelatedByTeacherNo(@Param("currentTeacherNo") String currentTeacherNo,
                                              @Param("courseName") String courseName,
                                              @Param("teacherNo") String teacherNo,
                                              @Param("semesterId") Integer semesterId);

    // 前端无页面调用，暂不启用
    /*
    List<Workload> selectDepartmentWorkloads(@Param("teacherId") Integer teacherId,
                                             @Param("teacherName") String teacherName,
                                             @Param("courseName") String courseName,
                                             @Param("semesterId") Integer semesterId,
                                             @Param("confirmStatus") Integer confirmStatus);

    Workload selectById(Integer wlId);
    */

    int batchInsert(@Param("list") List<Workload> list);

    int updateActualHours(@Param("wlId") Integer wlId,
                          @Param("currentTeacherName") String currentTeacherName,
                          @Param("actualHours") BigDecimal actualHours);

    int confirm(@Param("wlId") Integer wlId, @Param("currentTeacherName") String currentTeacherName);

    int recalculateAllRatios();

    int deleteBySemesterId(@Param("semesterId") Integer semesterId);

    List<WorkloadTeacherCompletionDTO> selectTeacherCompletion(@Param("semesterId") Integer semesterId);

    List<WorkloadTeacherCompletionDTO> selectDepartmentTeacherCompletion(@Param("teacherId") Integer teacherId,
                                                                          @Param("semesterId") Integer semesterId);
}
