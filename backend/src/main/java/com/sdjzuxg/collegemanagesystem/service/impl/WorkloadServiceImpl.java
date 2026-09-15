package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Workload;
import com.sdjzuxg.collegemanagesystem.dto.WorkloadCompletionDTO;
import com.sdjzuxg.collegemanagesystem.dto.WorkloadTeacherCompletionDTO;
import com.sdjzuxg.collegemanagesystem.mapper.WorkloadMapper;
import com.sdjzuxg.collegemanagesystem.service.WorkloadService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WorkloadServiceImpl implements WorkloadService {
    @Resource
    private WorkloadMapper workloadMapper;

    @Override
    public List<Workload> findByConditions(String teacherNo, String teacherName, String exactTeacherName,
                                           String courseName,
                                           Integer semesterId, Integer confirmStatus) {
        return workloadMapper.selectByConditions(
                trimToNull(teacherNo), trimToNull(teacherName), trimToNull(exactTeacherName),
                trimToNull(courseName),
                semesterId, confirmStatus);
    }

    @Override
    public List<Workload> findMyRelated(String currentTeacherName, String courseName,
                                         String teacherName, Integer semesterId) {
        String currentName = trimToNull(currentTeacherName);
        if (currentName == null) {
            return List.of();
        }
        return workloadMapper.selectMyRelated(currentName, trimToNull(courseName),
                trimToNull(teacherName), semesterId);
    }

    @Override
    public List<Workload> findMyRelatedByTeacherNo(String currentTeacherNo, String courseName,
                                                   String teacherNo, Integer semesterId) {
        String currentNo = trimToNull(currentTeacherNo);
        if (currentNo == null) return List.of();
        return workloadMapper.selectMyRelatedByTeacherNo(currentNo, trimToNull(courseName),
                trimToNull(teacherNo), semesterId);
    }

    // 前端无页面调用，暂不启用（与 Service 接口、Controller、Mapper 同步注释）
    /*
    @Override
    public List<Workload> findDepartmentWorkloads(Integer teacherId, String teacherName,
                                                  String courseName, Integer semesterId,
                                                  Integer confirmStatus) {
        if (teacherId == null) {
            return List.of();
        }
        return workloadMapper.selectDepartmentWorkloads(teacherId, trimToNull(teacherName),
                trimToNull(courseName), semesterId, confirmStatus);
    }

    @Override
    public Workload findById(Integer wlId) {
        return workloadMapper.selectById(wlId);
    }
    */

    @Override
    @Transactional
    public boolean batchImport(List<Workload> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (Workload workload : list) {
            if (workload.getSemesterId() == null) {
                throw new IllegalArgumentException("学期不能为空");
            }
            workload.setWlId(null);
            workload.setConfirmStatus(0);
            workload.setActualHourRatio(null);
        }
        int inserted = workloadMapper.batchInsert(list);
        workloadMapper.recalculateAllRatios();
        return inserted == list.size();
    }

    @Override
    @Transactional
    public boolean updateActualHours(Integer wlId, String currentTeacherName, BigDecimal actualHours) {
        if (wlId == null || trimToNull(currentTeacherName) == null) {
            return false;
        }
        if (actualHours != null && actualHours.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("实际学时不能小于0");
        }
        int updated = workloadMapper.updateActualHours(wlId, currentTeacherName.trim(), actualHours);
        if (updated > 0) {
            workloadMapper.recalculateAllRatios();
        }
        return updated > 0;
    }

    @Override
    public boolean confirm(Integer wlId, String currentTeacherName) {
        if (wlId == null || trimToNull(currentTeacherName) == null) {
            return false;
        }
        return workloadMapper.confirm(wlId, currentTeacherName.trim()) > 0;
    }

    @Override
    @Transactional
    public int deleteBySemesterId(Integer semesterId) {
        if (semesterId == null || semesterId <= 0) {
            throw new IllegalArgumentException("学期参数无效");
        }
        return workloadMapper.deleteBySemesterId(semesterId);
    }

    @Override
    public WorkloadCompletionDTO findCompletion(Integer semesterId) {
        return buildCompletion(workloadMapper.selectTeacherCompletion(semesterId));
    }

    @Override
    public WorkloadCompletionDTO findDepartmentCompletion(Integer teacherId, Integer semesterId) {
        if (teacherId == null) {
            return buildCompletion(List.of());
        }
        return buildCompletion(workloadMapper.selectDepartmentTeacherCompletion(teacherId, semesterId));
    }

    private WorkloadCompletionDTO buildCompletion(List<WorkloadTeacherCompletionDTO> details) {
        long totalRecords = 0;
        long confirmedRecords = 0;
        long completedTeachers = 0;
        for (WorkloadTeacherCompletionDTO detail : details) {
            long total = detail.getTotalRecords() == null ? 0 : detail.getTotalRecords();
            long confirmed = detail.getConfirmedRecords() == null ? 0 : detail.getConfirmedRecords();
            long pending = total - confirmed;
            detail.setTotalRecords(total);
            detail.setConfirmedRecords(confirmed);
            detail.setPendingRecords(pending);
            detail.setCompletionRate(total == 0 ? 0D : Math.round(confirmed * 10000D / total) / 100D);
            detail.setCompleted(pending == 0);
            totalRecords += total;
            confirmedRecords += confirmed;
            if (pending == 0) completedTeachers++;
        }

        WorkloadCompletionDTO result = new WorkloadCompletionDTO();
        result.setTotalTeachers((long) details.size());
        result.setCompletedTeachers(completedTeachers);
        result.setUncompletedTeachers((long) details.size() - completedTeachers);
        result.setTotalRecords(totalRecords);
        result.setConfirmedRecords(confirmedRecords);
        result.setPendingRecords(totalRecords - confirmedRecords);
        result.setCompletionRate(totalRecords == 0 ? 0D
                : Math.round(confirmedRecords * 10000D / totalRecords) / 100D);
        result.setTeacherDetails(details);
        return result;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
