package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> findAll();
    Menu findById(Integer menuId);
    boolean save(Menu menu);
    boolean update(Menu menu);
    boolean deleteById(Integer menuId);
    List<Menu> findByRoleId(Integer roleId);
}
