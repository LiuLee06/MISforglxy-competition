package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.entity.Role;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMapper;
import com.sdjzuxg.collegemanagesystem.service.RoleService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    private RoleMapper roleMapper;

    @Override
    public List<Role> findAll() {
        return roleMapper.selectAll();
    }

    @Override
    public Role findById(Integer roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public boolean save(Role role) {
        return roleMapper.insert(role) > 0;
    }

    @Override
    public boolean update(Role role) {
        return roleMapper.update(role) > 0;
    }

    @Override
    public boolean deleteById(Integer roleId) {
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    public PageResult<Role> findByPage(Integer pageNum, Integer pageSize, String roleName, String searchMode) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Integer offset = (pageNum - 1) * pageSize;
        List<Role> list = roleMapper.selectPage(offset, pageSize, roleName, searchMode);
        long total = roleMapper.countByCondition(roleName, searchMode);
        return new PageResult<>(total, list);
    }
}
