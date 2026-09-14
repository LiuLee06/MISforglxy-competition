package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.RoleMenu;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuMapper;
import com.sdjzuxg.collegemanagesystem.service.RoleMenuService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class RoleMenuServiceImpl implements RoleMenuService {
    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<RoleMenu> findAll() {
        return roleMenuMapper.selectAll();
    }

    @Override
    public RoleMenu findByRoleIdAndMenuId(Integer roleId, Integer menuId) {
        return roleMenuMapper.selectByRoleIdAndMenuId(roleId, menuId);
    }

    @Override
    public boolean save(RoleMenu roleMenu) {
        return roleMenuMapper.insert(roleMenu) > 0;
    }

    @Override
    public boolean update(Integer roleId, Integer oldMenuId, Integer newMenuId) {
        return roleMenuMapper.update(roleId, oldMenuId, newMenuId) > 0;
    }

    @Override
    public boolean deleteByRoleIdAndMenuId(Integer roleId, Integer menuId) {
        return roleMenuMapper.deleteByRoleIdAndMenuId(roleId, menuId) > 0;
    }

    @Override
    public List<RoleMenu> findByRoleId(Integer roleId) {
        return roleMenuMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Integer> findMenuIdsByRoleId(Integer roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public int deleteByRoleId(Integer roleId) {
        return roleMenuMapper.deleteByRoleId(roleId);
    }

    @Override
    public int batchInsert(List<RoleMenu> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return roleMenuMapper.batchInsert(list);
    }
}
