package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.dto.ExamQueryDTO;
import com.sdjzuxg.collegemanagesystem.entity.FinalExam;

import java.util.List;

public interface FinalExamService {
    List<FinalExam> findBySemesterId(Integer semesterId);

    PageResult<FinalExam> pageQuery(ExamQueryDTO queryDTO);
    List<FinalExam> batchInsert(List<FinalExam> list);
    int batchDelete(List<Integer> ids);
}
