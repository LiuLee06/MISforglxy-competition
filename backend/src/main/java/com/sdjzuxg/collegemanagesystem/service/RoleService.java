package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.entity.Role;

import java.util.List;

public interface RoleService {
    List<Role> findAll();
    Role findById(Integer roleId);
    boolean save(Role role);
    boolean update(Role role);
    boolean deleteById(Integer roleId);
    PageResult<Role> findByPage(Integer pageNum, Integer pageSize, String roleName, String searchMode);
}
