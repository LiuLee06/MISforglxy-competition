package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Workload;
import com.sdjzuxg.collegemanagesystem.entity.WorkloadResult;
import com.sdjzuxg.collegemanagesystem.mapper.WorkloadMapper;
import com.sdjzuxg.collegemanagesystem.mapper.WorkloadResultMapper;
import com.sdjzuxg.collegemanagesystem.service.WorkloadResultService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkloadResultServiceImpl implements WorkloadResultService {

    @Resource
    private WorkloadMapper workloadMapper;

    @Resource
    private WorkloadResultMapper workloadResultMapper;

    @Override
    public List<WorkloadResult> findResults(Integer semesterId, Integer year) {
        if (year != null) {
            // 学年视图：同一学年（两学期共用同一 year 值）的记录按教师合并成一条
            return workloadResultMapper.selectResultsGroupedByYear(year);
        }
        return workloadResultMapper.selectResults(semesterId);
    }

    @Override
    public List<WorkloadResult> findMyResults(String currentTeacherName, Integer semesterId) {
        String currentName = trimToNull(currentTeacherName);
        if (currentName == null) {
            return List.of(); // 没有当前教师直接返回空列表，防御性编程
        }
        return workloadResultMapper.selectMyResults(currentName, semesterId);
    }

    @Override
    @Transactional
    public int calculate(Integer semesterId, Integer studentLimit) {
        if (semesterId == null || semesterId <= 0) {
            throw new IllegalArgumentException("学期参数无效");
        }
        if (studentLimit == null || studentLimit <= 0) {
            throw new IllegalArgumentException("人数限值 c 必须为正整数");
        }

        // 该学期全部课时记录（不区分确认状态：由教学办确认全员确认后再点计算）
        List<Workload> records = workloadMapper.selectByConditions(
                null, null, null, null, semesterId, null);
        if (records.isEmpty()) {
            throw new IllegalArgumentException("该学期没有课时记录，无法计算");
        }

        // 第一步：人数缺失校验。任何一条缺人数都整体不计算，并列出全部问题记录
        List<String> missing = new ArrayList<>();
        for (Workload w : records) {
            if (w.getStudentCount() == null || w.getStudentCount() <= 0) {
                missing.add(w.getTeacherName() + " - " + w.getCourseName()
                        + (w.getClassName() == null ? "" : "（" + w.getClassName() + "）"));
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("以下记录人数缺失，计算失败：" + String.join("；", missing));
        }

        // 第二步：按教师分组累加。每行课时记录已是该教师本人的学时份额（含合作教师拆分后的行）
        Map<String, WorkloadResult> grouped = new LinkedHashMap<>();
        for (Workload w : records) {
            BigDecimal hours = w.getActualHours() == null ? BigDecimal.ZERO : w.getActualHours();
            BigDecimal a = courseCoefficient(w.getCourseNo());
            BigDecimal b = studentCoefficient(w.getStudentCount(), studentLimit);
            BigDecimal g = a.multiply(b).multiply(hours).setScale(2, RoundingMode.HALF_UP);

            WorkloadResult result = grouped.computeIfAbsent(w.getTeacherNo(), key -> {
                WorkloadResult nr = new WorkloadResult();
                nr.setTeacherNo(w.getTeacherNo());
                nr.setTeacherName(w.getTeacherName());
                nr.setSemesterId(semesterId);
                nr.setStudentLimit(studentLimit);
                nr.setTotalHours(BigDecimal.ZERO);
                nr.setWorkloadHours(BigDecimal.ZERO);
                nr.setCalcTime(LocalDateTime.now());
                return nr;
            });
            result.setTotalHours(result.getTotalHours().add(hours));
            result.setWorkloadHours(result.getWorkloadHours().add(g));
        }

        // 第三步：覆盖落库（先删该学期旧结果，再批量插入），同一事务保证一致性
        workloadResultMapper.deleteBySemesterId(semesterId);
        List<WorkloadResult> list = new ArrayList<>(grouped.values());
        for (WorkloadResult r : list) {
            r.setTotalHours(r.getTotalHours().setScale(2, RoundingMode.HALF_UP));
            r.setWorkloadHours(r.getWorkloadHours().setScale(2, RoundingMode.HALF_UP));
        }
        workloadResultMapper.batchInsert(list);
        return list.size();
    }

    /**
     * 课程系数 a：课程编号 GL 开头为一般课程 1.0，其他 1.15
     */
    private BigDecimal courseCoefficient(String courseNo) {
        return courseNo != null && courseNo.trim().toUpperCase().startsWith("GL")
                ? new BigDecimal("1.0")
                : new BigDecimal("1.15");
    }

    /**
     * 人数系数 b（分段函数，以人数限值 c 为基准）：
     *   人数 <= c            → b = 1
     *   c < 人数 <= 2c       → b = (人数-c)/c*0.5 + 1
     *   人数 > 2c            → b = (人数-2c)/c*0.4 + 1.5
     */
    private BigDecimal studentCoefficient(Integer studentCount, int c) {
        int n = studentCount;
        if (n <= c) {
            return BigDecimal.ONE;
        }
        if (n <= 2 * c) {
            return new BigDecimal(n - c)
                    .multiply(new BigDecimal("0.5"))
                    .divide(new BigDecimal(c), 4, RoundingMode.HALF_UP)
                    .add(BigDecimal.ONE);
        }
        return new BigDecimal(n - 2 * c)
                .multiply(new BigDecimal("0.4"))
                .divide(new BigDecimal(c), 4, RoundingMode.HALF_UP)
                .add(new BigDecimal("1.5"));
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
