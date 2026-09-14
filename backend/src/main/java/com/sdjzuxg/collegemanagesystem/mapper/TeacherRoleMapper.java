package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.TeacherRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TeacherRoleMapper {
    List<TeacherRole> selectAll();
    int insert(TeacherRole teacherRole);
    int updateRole(TeacherRole teacherRole);
    int deleteByTeacherIdAndRoleId(@Param("teacherId") Integer teacherId, @Param("roleId") Integer roleId);
    List<TeacherRole> selectByTeacherId(Integer teacherId);
    List<TeacherRole> selectByRoleId(Integer roleId);

    // 删除某教师的所有角色分配(级联删除教师主体时用)
    int deleteByTeacherId(@Param("teacherId") Integer teacherId);

    List<TeacherRole> selectPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize,
                                  @Param("roleId") Integer roleId, @Param("teacherId") Integer teacherId,
                                  @Param("teacherName") String teacherName, @Param("searchMode") String searchMode);
    long countByCondition(@Param("roleId") Integer roleId, @Param("teacherId") Integer teacherId,
                          @Param("teacherName") String teacherName, @Param("searchMode") String searchMode);
}
