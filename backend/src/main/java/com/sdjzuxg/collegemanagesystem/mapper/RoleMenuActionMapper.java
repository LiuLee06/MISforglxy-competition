package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.RoleMenuAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoleMenuActionMapper {
    List<RoleMenuAction> selectAll();
    RoleMenuAction selectById(@Param("id") Integer id);
    List<RoleMenuAction> selectByRoleId(@Param("roleId") Integer roleId);
    List<String> selectActionCodesByRoleId(@Param("roleId") Integer roleId);
    int insert(RoleMenuAction roleMenuAction);
    int update(RoleMenuAction roleMenuAction);
    int deleteById(@Param("id") Integer id);
    int deleteByRoleId(@Param("roleId") Integer roleId);
    int deleteByRoleIdAndActionId(@Param("roleId") Integer roleId, @Param("actionId") Integer actionId);
    List<String> selectActionCodesByRoleIds(@Param("roleIds") List<Integer> roleIds);

    // 超级管理员专用：读取 MENU_ACTION 表的全量按钮权限，不依赖 ROLE_MENU_ACTION 关联
    List<String> selectAllActionCodes();

    /**
     * 校验指定教师是否拥有某 actionCode。
     * 通过 TEACHER_ROLE -> ROLE_MENU_ACTION -> MENU_ACTION 三表 JOIN 查存在性。
     * 返回匹配行数,> 0 表示有权限。
     */
    int existsByTeacherIdAndCode(@Param("teacherId") Integer teacherId, @Param("code") String code);
}
