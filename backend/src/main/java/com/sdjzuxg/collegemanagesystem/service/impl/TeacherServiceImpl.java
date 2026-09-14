package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.mapper.DeptMapper;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherDeptMapper;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherMapper;
import com.sdjzuxg.collegemanagesystem.service.TeacherService;
import com.sdjzuxg.collegemanagesystem.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherServiceImpl implements TeacherService {
    @Resource
    private TeacherMapper teacherMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private TeacherDeptMapper teacherDeptMapper;

    @Override
    public List<Teacher> findAll() {
        return teacherMapper.selectAll();
    }

    @Override
    public Teacher findById(Integer teacherId) {
        return teacherMapper.selectById(teacherId);
    }

    @Override
    public Teacher findByPhone(String phone) {
        return teacherMapper.selectByPhone(phone);
    }

    @Override
    public boolean deleteById(Integer teacherId) {
        // 1. 先删除教师-部门关联
        teacherDeptMapper.deleteByTeacherId(teacherId);
        // 2. 再删除教师
        return teacherMapper.deleteById(teacherId) > 0;
    }

    @Override
    @Transactional
    public boolean save(Teacher teacher) {
        // 新增教师：密码为空则使用默认密码，统一 BCrypt 加密
        teacher.setPassword(PasswordUtil.hash(teacher.getPassword()));
        boolean result = teacherMapper.insert(teacher) > 0;
        if (result) {
            // 同步 TEACHER_DEPT 关联表（insert 后 teacherId 已通过 useGeneratedKeys 回填）
            syncTeacherDept(teacher.getTeacherId(), teacher.getDept());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean update(Teacher teacher) {
        // 修改教师：密码为空表示不改密码，查库回填原哈希，避免被清空（Profile 页面保存信息时不传密码）
        if (teacher.getPassword() == null || teacher.getPassword().isEmpty()) {
            Teacher original = teacherMapper.selectById(teacher.getTeacherId());
            if (original != null) {
                teacher.setPassword(original.getPassword());
            }
        } else {
            // 传了新明文密码，BCrypt 加密
            teacher.setPassword(PasswordUtil.hash(teacher.getPassword()));
        }
        boolean result = teacherMapper.update(teacher) > 0;
        if (result) {
            // 同步 TEACHER_DEPT 关联表
            syncTeacherDept(teacher.getTeacherId(), teacher.getDept());
        }
        return result;
    }

    /**
     * 同步教师-部门关联表：先删旧关联，再根据部门名称批量插入新关联
     * dept 字段格式：逗号分隔的部门名称字符串，如 "计算机系,数学系"
     */
    private void syncTeacherDept(Integer teacherId, String deptStr) {
        if (teacherId == null) return;
        // 先删除旧关联
        teacherDeptMapper.deleteByTeacherId(teacherId);
        // 解析部门名称（兼容英文逗号、中文逗号、顿号）
        if (deptStr == null || deptStr.trim().isEmpty()) return;
        List<String> deptNames = Arrays.stream(deptStr.split("[,，、]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (deptNames.isEmpty()) return;
        // 根据部门名称查 dept_id
        List<Integer> deptIds = deptMapper.selectIdsByNames(deptNames);
        if (!deptIds.isEmpty()) {
            teacherDeptMapper.batchInsert(teacherId, deptIds);
        }
    }

    @Override
    public boolean updatePassword(Integer teacherId, String password) {
        // 改密码：BCrypt 加密后写入
        return teacherMapper.updatePassword(teacherId, PasswordUtil.hash(password)) > 0;
    }

    @Override
    public boolean resetPassword(Integer teacherId) {
        // 管理员重置密码：重置为默认密码 123456
        return teacherMapper.updatePassword(teacherId, PasswordUtil.hash(PasswordUtil.DEFAULT_RAW_PASSWORD)) > 0;
    }

    @Override
    public Map<String, Object> getCardStats(Integer deptId) {
        List<Map<String, Object>> list = teacherMapper.countGroupByProfessionalTitle(deptId);
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("教授", 0);
        result.put("副教授", 0);
        result.put("讲师", 0);

        int total = 0;
        for (Map<String, Object> item : list) {
            String title = (String) item.get("professional_title");
            Long count = (Long) item.get("count");
            if (title != null) {
                result.put(title, count);
                total += count;
            }
        }
        result.put("total", total);
        return result;
    }

    @Override
    public List<Map<String, Object>> getAgeDistribution(Integer deptId) {
        return teacherMapper.countGroupByAgeGroup(deptId);
    }

    @Override
    public List<Map<String, Object>> getEducationDistribution(Integer deptId) {
        return teacherMapper.countGroupByEducation(deptId);
    }

    @Override
    public List<Map<String, Object>> getDeptStatistics() {
        // 获取总教师数（这里简单统计所有教师，如果需过滤在职可加条件）
        int totalTeachers = teacherMapper.selectAll().size();
        List<Map<String, Object>> list = deptMapper.selectDeptStatistics();
        for (Map<String, Object> item : list) {
            Long count = (Long) item.get("teacherCount");
            if (totalTeachers > 0) {
                double percentage = count * 100.0 / totalTeachers;
                item.put("percentage", String.format("%.2f", percentage) + "%");
            } else {
                item.put("percentage", "0.00%");
            }
        }
        return list;
    }

    @Override
    public PageResult<Teacher> findByPage(Integer pageNum, Integer pageSize, String searchKey) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Integer offset = (pageNum - 1) * pageSize;
        List<Teacher> list = teacherMapper.selectPage(offset, pageSize, searchKey);
        long total = teacherMapper.countByCondition(searchKey);
        return new PageResult<>(total, list);
    }
}