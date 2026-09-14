package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {
    List<Admin> selectAll();
    Admin selectById(@Param("adminId") Integer adminId);
    Admin selectByUsername(@Param("username") String username);
    int insert(Admin admin);
    int update(Admin admin);
    int updatePassword(@Param("adminId") Integer adminId, @Param("password") String password);
    int deleteById(@Param("adminId") Integer adminId);
}
