package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TeacherMapper {
    List<Teacher> selectAll();
    Teacher selectById(@Param("teacherId") Integer teacherId);
    Teacher selectByPhone(@Param("phone") String phone);
    Teacher selectByName(@Param("name") String name);
    int deleteById(@Param("teacherId") Integer teacherId);
    int insert(Teacher teacher);
    int update(Teacher teacher);
    int updatePassword(@Param("teacherId") Integer teacherId, @Param("password") String password);
    List<Map<String, Object>> countGroupByProfessionalTitle(@Param("deptId") Integer deptId);

    List<Map<String, Object>> countGroupByAgeGroup(@Param("deptId") Integer deptId);


    List<Map<String, Object>> countGroupByEducation(@Param("deptId") Integer deptId);

    // 分页查询
    List<Teacher> selectPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize, 
                            @Param("searchKey") String searchKey);
    // 查询总数
    long countByCondition(@Param("searchKey") String searchKey);
    Teacher selectByStaffNo(@Param("staffNo") String staffNo);
}



