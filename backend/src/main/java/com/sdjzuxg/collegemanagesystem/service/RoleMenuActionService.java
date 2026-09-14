package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.RoleMenuAction;

import java.util.List;

public interface RoleMenuActionService {
    List<RoleMenuAction> selectAll();
    RoleMenuAction selectById(Integer id);
    List<RoleMenuAction> selectByRoleId(Integer roleId);
    List<String> selectActionCodesByRoleId(Integer roleId);
    List<String> selectActionCodesByRoleIds(List<Integer> roleIds);
    int insert(RoleMenuAction roleMenuAction);
    int update(RoleMenuAction roleMenuAction);
    int deleteById(Integer id);
    int deleteByRoleId(Integer roleId);
    int deleteByRoleIdAndActionId(Integer roleId, Integer actionId);
    int batchInsert(List<RoleMenuAction> list);
}
