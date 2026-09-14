package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.RoleMenuAction;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuActionMapper;
import com.sdjzuxg.collegemanagesystem.service.RoleMenuActionService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleMenuActionServiceImpl implements RoleMenuActionService {

    @Resource
    private RoleMenuActionMapper roleMenuActionMapper;

    @Override
    public List<RoleMenuAction> selectAll() {
        return roleMenuActionMapper.selectAll();
    }

    @Override
    public RoleMenuAction selectById(Integer id) {
        return roleMenuActionMapper.selectById(id);
    }

    @Override
    public List<RoleMenuAction> selectByRoleId(Integer roleId) {
        return roleMenuActionMapper.selectByRoleId(roleId);
    }

    @Override
    public List<String> selectActionCodesByRoleId(Integer roleId) {
        return roleMenuActionMapper.selectActionCodesByRoleId(roleId);
    }

    @Override
    public List<String> selectActionCodesByRoleIds(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleMenuActionMapper.selectActionCodesByRoleIds(roleIds);
    }

    @Override
    public int insert(RoleMenuAction roleMenuAction) {
        return roleMenuActionMapper.insert(roleMenuAction);
    }

    @Override
    public int update(RoleMenuAction roleMenuAction) {
        return roleMenuActionMapper.update(roleMenuAction);
    }

    @Override
    public int deleteById(Integer id) {
        return roleMenuActionMapper.deleteById(id);
    }

    @Override
    public int deleteByRoleId(Integer roleId) {
        return roleMenuActionMapper.deleteByRoleId(roleId);
    }

    @Override
    public int deleteByRoleIdAndActionId(Integer roleId, Integer actionId) {
        return roleMenuActionMapper.deleteByRoleIdAndActionId(roleId, actionId);
    }

    @Transactional
    @Override
    public int batchInsert(List<RoleMenuAction> list) {
        int count = 0;
        for (RoleMenuAction item : list) {
            count += roleMenuActionMapper.insert(item);
        }
        return count;
    }
}
