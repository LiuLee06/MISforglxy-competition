package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.RoleMenu;

import java.util.List;

public interface RoleMenuService {
    List<RoleMenu> findAll();
    RoleMenu findByRoleIdAndMenuId(Integer roleId, Integer menuId);
    boolean save(RoleMenu roleMenu);
    boolean update(Integer roleId, Integer oldMenuId, Integer newMenuId);
    boolean deleteByRoleIdAndMenuId(Integer roleId, Integer menuId);
    List<RoleMenu> findByRoleId(Integer roleId);
    List<Integer> findMenuIdsByRoleId(Integer roleId);
    int deleteByRoleId(Integer roleId);
    int batchInsert(List<RoleMenu> list);
}
