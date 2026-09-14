package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.MenuAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuActionMapper {
    List<MenuAction> selectAll();
    MenuAction selectById(@Param("actionId") Integer actionId);
    List<MenuAction> selectByMenuId(@Param("menuId") Integer menuId);
    MenuAction selectByActionCode(@Param("actionCode") String actionCode);
    int insert(MenuAction menuAction);
    int update(MenuAction menuAction);
    int deleteById(@Param("actionId") Integer actionId);
}
