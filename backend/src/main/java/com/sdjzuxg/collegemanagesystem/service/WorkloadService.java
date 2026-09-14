package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.Workload;
import com.sdjzuxg.collegemanagesystem.dto.WorkloadCompletionDTO;

import java.math.BigDecimal;
import java.util.List;

public interface WorkloadService {
    List<Workload> findByConditions(String teacherNo, String teacherName, String exactTeacherName,
                                    String courseName,
                                    Integer semesterId, Integer confirmStatus);

    List<Workload> findMyRelated(String currentTeacherName, String courseName,
                                 String teacherName, Integer semesterId);

    // 前端无页面调用，暂不启用（Controller、ServiceImpl、Mapper 已一并注释）
    /*
    List<Workload> findDepartmentWorkloads(Integer teacherId, String teacherName,
                                           String courseName, Integer semesterId,
                                           Integer confirmStatus);

    Workload findById(Integer wlId);
    */

    boolean batchImport(List<Workload> list);

    boolean updateActualHours(Integer wlId, String currentTeacherName, BigDecimal actualHours);

    boolean confirm(Integer wlId, String currentTeacherName);

    int deleteBySemesterId(Integer semesterId);

    WorkloadCompletionDTO findCompletion(Integer semesterId);

    WorkloadCompletionDTO findDepartmentCompletion(Integer teacherId, Integer semesterId);
}
