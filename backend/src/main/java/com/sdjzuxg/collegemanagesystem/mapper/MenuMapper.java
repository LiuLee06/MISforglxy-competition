package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {
    List<Menu> selectAll();
    Menu selectById(@Param("menuId") Integer menuId);
    int insert(Menu menu);
    int update(Menu menu);
    int deleteById(@Param("menuId") Integer menuId);
    List<Menu> selectByRoleId(@Param("roleId") Integer roleId);
}
