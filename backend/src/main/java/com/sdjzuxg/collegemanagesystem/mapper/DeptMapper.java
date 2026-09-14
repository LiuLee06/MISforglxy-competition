package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Dept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeptMapper {
    List<Dept> selectAll();
    Dept selectById(Integer deptId);
    int insert(Dept dept);
    int update(Dept dept);
    int deleteById(Integer deptId);
    // 新增：查询每个部门的教师人数
    List<Map<String, Object>> selectDeptStatistics();
    // 根据部门名称列表查 dept_id
    List<Integer> selectIdsByNames(@Param("names") List<String> names);
}
