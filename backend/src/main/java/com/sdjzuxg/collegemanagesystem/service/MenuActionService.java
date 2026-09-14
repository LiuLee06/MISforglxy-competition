package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.MenuAction;

import java.util.List;

public interface MenuActionService {
    List<MenuAction> selectAll();
    MenuAction selectById(Integer actionId);
    List<MenuAction> selectByMenuId(Integer menuId);
    MenuAction selectByActionCode(String actionCode);
    int insert(MenuAction menuAction);
    int update(MenuAction menuAction);
    int deleteById(Integer actionId);
}
