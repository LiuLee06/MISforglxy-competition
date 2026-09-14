package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    List<Role> selectAll();
    Role selectById(@Param("roleId") Integer roleId);
    int insert(Role role);
    int update(Role role);
    int deleteById(@Param("roleId") Integer roleId);
    List<Role> selectPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize, @Param("roleName") String roleName, @Param("searchMode") String searchMode);
    long countByCondition(@Param("roleName") String roleName, @Param("searchMode") String searchMode);
}
