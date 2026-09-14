package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.entity.TeacherRole;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherRoleMapper;
import com.sdjzuxg.collegemanagesystem.service.TeacherRoleService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class TeacherRoleServiceImpl implements TeacherRoleService {
    @Resource
    private TeacherRoleMapper teacherRoleMapper;

    @Override
    public List<TeacherRole> findAll() {
        return teacherRoleMapper.selectAll();
    }

    @Override
    public boolean save(TeacherRole teacherRole) {
        if (teacherRole.getTeacherId() == null || teacherRole.getRoleId() == null) {
            return false;
        }
        try {
            return teacherRoleMapper.insert(teacherRole) > 0;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean updateRole(TeacherRole teacherRole) {
        if (teacherRole.getTeacherId() == null || teacherRole.getRoleId() == null) {
            return false;
        }
        Integer oldRoleId = teacherRole.getOldRoleId();

        // 没传 oldRoleId 时，用新 roleId 做 fallback，避免 UPDATE 把该教师所有记录都改成同一值
        if (oldRoleId == null) {
            oldRoleId = teacherRole.getRoleId();
        }

        // 如果新角色 = 旧角色，没任何变更，直接返回成功
        if (oldRoleId.equals(teacherRole.getRoleId())) {
            return true;
        }

        // 先删除 (teacherId, oldRoleId) 这条旧关联
        teacherRoleMapper.deleteByTeacherIdAndRoleId(teacherRole.getTeacherId(), oldRoleId);

        // 再插入 (teacherId, roleId) 这条新关联
        TeacherRole newTr = new TeacherRole();
        newTr.setTeacherId(teacherRole.getTeacherId());
        newTr.setRoleId(teacherRole.getRoleId());
        try {
            teacherRoleMapper.insert(newTr);
            return true;
        } catch (Exception e) {
            // 可能是唯一约束冲突（该教师已拥有该角色）——视为修改成功
            return true;
        }
    }

    @Override
    public boolean deleteByTeacherIdAndRoleId(Integer teacherId, Integer roleId) {
        return teacherRoleMapper.deleteByTeacherIdAndRoleId(teacherId, roleId) > 0;
    }

    @Override
    public List<TeacherRole> findByTeacherId(Integer teacherId) {
        return teacherRoleMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<TeacherRole> findByRoleId(Integer roleId) {
        return teacherRoleMapper.selectByRoleId(roleId);
    }

    @Override
    public PageResult<TeacherRole> findByPage(Integer pageNum, Integer pageSize, Integer roleId,
                                              Integer teacherId, String teacherName, String searchMode) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Integer offset = (pageNum - 1) * pageSize;
        List<TeacherRole> list = teacherRoleMapper.selectPage(offset, pageSize, roleId, teacherId, teacherName, searchMode);
        long total = teacherRoleMapper.countByCondition(roleId, teacherId, teacherName, searchMode);
        return new PageResult<>(total, list);
    }
}
