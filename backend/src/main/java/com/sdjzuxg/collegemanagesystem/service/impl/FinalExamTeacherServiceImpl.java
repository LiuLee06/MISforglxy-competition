package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.FinalExamTeacher;
import com.sdjzuxg.collegemanagesystem.mapper.FinalExamTeacherMapper;
import com.sdjzuxg.collegemanagesystem.service.FinalExamTeacherService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinalExamTeacherServiceImpl implements FinalExamTeacherService {
    @Resource
    private FinalExamTeacherMapper finalExamTeacherMapper;

    @Override
    public boolean batchInsert(List<FinalExamTeacher> list) {
        if (list == null || list.isEmpty()) return false;
        // 过滤掉同一考试下已存在的监考教师记录（基于 exam_id + teacher_id + invigilate_role）
        List<FinalExamTeacher> toInsert = new ArrayList<>();
        for (FinalExamTeacher t : list) {
            if (t.getExam() == null || t.getExam().getExamId() == null
                    || t.getTeacher() == null || t.getTeacher().getTeacherId() == null) {
                continue;
            }
            int exists = finalExamTeacherMapper.countExistByExamAndTeacher(
                    t.getExam().getExamId(), t.getTeacher().getTeacherId(), t.getInvigilateRole());
            if (exists == 0) {
                toInsert.add(t);
            }
        }
        if (toInsert.isEmpty()) return false;
        return finalExamTeacherMapper.batchInsert(toInsert) == toInsert.size();
    }
}
