package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMenuMapper {
    List<RoleMenu> selectAll();
    RoleMenu selectByRoleIdAndMenuId(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);
    int insert(RoleMenu roleMenu);
    int update(@Param("roleId") Integer roleId, @Param("oldMenuId") Integer oldMenuId, @Param("newMenuId") Integer newMenuId);
    int deleteByRoleIdAndMenuId(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);
    List<RoleMenu> selectByRoleId(@Param("roleId") Integer roleId);
    List<Integer> selectMenuIdsByRoleId(@Param("roleId") Integer roleId);
    int deleteByRoleId(@Param("roleId") Integer roleId);
    int batchInsert(@Param("list") List<RoleMenu> list);
    int existsByTeacherIdAndMenuUrl(@Param("teacherId") Integer teacherId, @Param("menuUrl") String menuUrl);
}
