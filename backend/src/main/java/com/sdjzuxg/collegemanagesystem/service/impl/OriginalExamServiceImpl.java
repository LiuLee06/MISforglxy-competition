package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.OriginalExam;
import com.sdjzuxg.collegemanagesystem.mapper.OriginalExamMapper;
import com.sdjzuxg.collegemanagesystem.mapper.OriginalExamTeacherMapper;
import com.sdjzuxg.collegemanagesystem.service.OriginalExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OriginalExamServiceImpl implements OriginalExamService {
    @Resource
    private OriginalExamMapper originalExamMapper;

    @Resource
    private OriginalExamTeacherMapper originalExamTeacherMapper;

    @Override
    public List<OriginalExam> findBySemesterId(Integer semesterId) {
        return originalExamMapper.selectBySemesterId(semesterId);
    }

    @Override
    public PageResult<OriginalExam> pageQueryFlat(ExamQueryDTO queryDTO) {
        long total = originalExamMapper.countByConditionFlat(queryDTO);
        List<OriginalExam> records = originalExamMapper.selectByConditionFlat(queryDTO);
        return new PageResult<>(total, records);
    }

    @Override
    public List<OriginalExam> batchInsert(List<OriginalExam> list) {
        if (list == null || list.isEmpty()) return null;

        // 获取待导入数据所属学期
        Integer semesterId = null;
        for (OriginalExam exam : list) {
            if (exam.getSemester() != null && exam.getSemester().getSemesterId() != null) {
                semesterId = exam.getSemester().getSemesterId();
                break;
            }
        }

        // 查询同一学期已存在的考试数据，构建签名集合
        Set<String> existingSignatures = new HashSet<>();
        if (semesterId != null) {
            List<OriginalExam> existingExams = originalExamMapper.selectBySemesterId(semesterId);
            if (existingExams != null) {
                for (OriginalExam exam : existingExams) {
                    existingSignatures.add(examSignature(exam));
                }
            }
        }

        // 过滤掉与已存在数据完全相同的重复数据
        List<OriginalExam> toInsert = new ArrayList<>();
        for (OriginalExam exam : list) {
            String sig = examSignature(exam);
            if (existingSignatures.contains(sig)) {
                continue;
            }
            toInsert.add(exam);
            existingSignatures.add(sig); // 避免待导入数据内部重复
        }

        if (toInsert.isEmpty()) {
            // 全部为同一学期已存在的重复数据
            return Collections.emptyList();
        }

        originalExamMapper.batchInsert(toInsert);
        return toInsert;
    }

    /** 生成考试数据签名，用于判断"完全相同"的重复数据 */
    private String examSignature(OriginalExam exam) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.join("|",
                n2e(exam.getCourseName()),
                exam.getStartTime() == null ? "" : sdf.format(exam.getStartTime()),
                exam.getEndTime() == null ? "" : sdf.format(exam.getEndTime()),
                n2e(exam.getRoom()),
                n2e(exam.getTeachingCampus()),
                n2e(exam.getExamCampus()),
                n2e(exam.getTeachingDept()),
                n2e(exam.getTeachingTeacher()),
                n2e(exam.getInvigilationCollege()),
                n2e(exam.getClassName()),
                exam.getStudentCount() == null ? "" : String.valueOf(exam.getStudentCount())
        );
    }

    private static String n2e(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public List<OriginalExam> findWithReason() {
        return originalExamMapper.selectWithReason();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        originalExamTeacherMapper.deleteByExamIds(ids);
        return originalExamMapper.batchDeleteByIds(ids);
    }

    @Override
    public Map<String, Object> getConfirmStatsFlat(Integer semesterId, String dept) {
        Map<String, Object> stats = originalExamMapper.countConfirmStatsFlat(semesterId, dept);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("total", 0);
            stats.put("unconfirmed", 0);
            stats.put("noObjection", 0);
            stats.put("hasFeedback", 0);
        }
        return stats;
    }

    @Override
    public boolean updateTeacherReason(Integer id, String reason) {
        return originalExamMapper.updateTeacherReason(id, reason) > 0;
    }

    @Override
    public List<Map<String, Object>> getStatsByTeacher(Integer semesterId, String dept) {
        return originalExamMapper.countStatsByTeacher(semesterId, dept);
    }

    @Override
    public List<String> getDistinctDept(Integer semesterId) {
        return originalExamMapper.selectDistinctDept(semesterId);
    }
}
