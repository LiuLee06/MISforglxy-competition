package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.OriginalExamTeacher;
import com.sdjzuxg.collegemanagesystem.mapper.OriginalExamTeacherMapper;
import com.sdjzuxg.collegemanagesystem.service.OriginalExamTeacherService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OriginalExamTeacherServiceImpl implements OriginalExamTeacherService {
    @Resource
    private OriginalExamTeacherMapper originalExamTeacherMapper;

    @Override
    public int batchInsert(List<OriginalExamTeacher> list) {
        List<OriginalExamTeacher> validList = list.stream()
                .filter(t -> t.getTeacher() != null && t.getTeacher().getTeacherId() != null)
                .collect(Collectors.toList());

        if (validList.isEmpty()) {
            return 0;
        }

        return originalExamTeacherMapper.batchInsert(validList);
    }
}
