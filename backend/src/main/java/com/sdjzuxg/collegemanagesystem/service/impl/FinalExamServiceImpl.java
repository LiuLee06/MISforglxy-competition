package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.FinalExam;
import com.sdjzuxg.collegemanagesystem.mapper.FinalExamMapper;
import com.sdjzuxg.collegemanagesystem.mapper.FinalExamTeacherMapper;
import com.sdjzuxg.collegemanagesystem.service.FinalExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FinalExamServiceImpl implements FinalExamService {
    @Resource
    private FinalExamMapper finalExamMapper;

    @Resource
    private FinalExamTeacherMapper finalExamTeacherMapper;

    @Override
    public List<FinalExam> findBySemesterId(Integer semesterId) {
        return finalExamMapper.selectBySemesterId(semesterId);
    }

    @Override
    public List<FinalExam> findBySemesterAndTeacher(Integer semesterId, Integer teacherId) {
        if (semesterId == null || teacherId == null) return List.of();
        return finalExamMapper.selectBySemesterAndTeacher(semesterId, teacherId);
    }

    @Override
    public PageResult<FinalExam> pageQuery(ExamQueryDTO queryDTO) {
        long total = finalExamMapper.countByCondition(queryDTO);
        List<FinalExam> records = finalExamMapper.selectByCondition(queryDTO);
        return new PageResult<>(total, records);
    }

    @Override
    public List<FinalExam> batchInsert(List<FinalExam> list) {
        if (list == null || list.isEmpty()) return null;

        // 获取待导入数据所属学期
        Integer semesterId = null;
        for (FinalExam exam : list) {
            if (exam.getSemester() != null && exam.getSemester().getSemesterId() != null) {
                semesterId = exam.getSemester().getSemesterId();
                break;
            }
        }

        // 查询同一学期已存在的考试数据，构建签名集合
        Set<String> existingSignatures = new HashSet<>();
        if (semesterId != null) {
            List<FinalExam> existingExams = finalExamMapper.selectBySemesterId(semesterId);
            if (existingExams != null) {
                for (FinalExam exam : existingExams) {
                    existingSignatures.add(examSignature(exam));
                }
            }
        }

        // 过滤掉与已存在数据完全相同的重复数据
        List<FinalExam> toInsert = new ArrayList<>();
        for (FinalExam exam : list) {
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

        finalExamMapper.batchInsert(toInsert);
        return toInsert;
    }

    /** 生成考试数据签名，用于判断"完全相同"的重复数据 */
    private String examSignature(FinalExam exam) {
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
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        finalExamTeacherMapper.deleteByExamIds(ids);
        return finalExamMapper.batchDeleteByIds(ids);
    }
}
