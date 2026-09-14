package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;

import java.util.List;
import java.util.Map;

public interface TeacherService {
    List<Teacher> findAll();
    Teacher findById(Integer teacherId);
    Teacher findByPhone(String phone);
    boolean deleteById(Integer teacherId);
    boolean save(Teacher teacher);
    boolean update(Teacher teacher);
    boolean updatePassword(Integer teacherId, String password);
    boolean resetPassword(Integer teacherId);
    // 统计：顶部卡片数据（总人数、教授、副教授、讲师）
    Map<String, Object> getCardStats(Integer deptId);

    // 统计：年龄分布
    List<Map<String, Object>> getAgeDistribution(Integer deptId);

     //统计：学历分布
    List<Map<String, Object>> getEducationDistribution(Integer deptId);

    // 统计：部门列表（含人数、占比）
    List<Map<String, Object>> getDeptStatistics();

    // 分页查询
    PageResult<Teacher> findByPage(Integer pageNum, Integer pageSize, String searchKey);

}
