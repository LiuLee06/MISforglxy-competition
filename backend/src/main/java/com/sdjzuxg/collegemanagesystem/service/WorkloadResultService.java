package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.WorkloadResult;

import java.util.List;

/**
 * 教师工作量计算结果 Service
 * 粒度：教师 × 学期，一人一学期一条汇总记录
 */
public interface WorkloadResultService {

    /** 管理端：传 year（学年）返回聚合视图（该学年各学期按教师合计），否则按学期返回明细 */
    List<WorkloadResult> findResults(Integer semesterId, Integer year);

    /** 教师端：只查自己的计算结果 */
    List<WorkloadResult> findMyResults(String currentTeacherName, Integer semesterId);

    /**
     * 按学期计算工作量并覆盖落库。
     * G = Σ a × b × 执行学时（实际学时）
     *   a 课程系数：GL 开头 = 1.0，其他 = 1.15
     *   b 人数系数：人数<=c 为 1；c<人数<=2c 为 (人数-c)/c*0.5+1；人数>2c 为 (人数-2c)/c*0.4+1.5
     *
     * @return 本次写入的教师条数
     * @throws IllegalArgumentException 学期/限值无效、该学期无记录、存在人数缺失记录（附明细）
     */
    int calculate(Integer semesterId, Integer studentLimit);
}
