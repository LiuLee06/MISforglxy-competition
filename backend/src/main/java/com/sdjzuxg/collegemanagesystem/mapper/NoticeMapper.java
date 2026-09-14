package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<Notice> selectAll();
    Notice selectById(Integer noticeId);
    List<Notice> selectByTeacherId(Integer teacherId);
    List<Notice> selectByPublisherId(@Param("publisherId") Integer publisherId, @Param("publisherType") String publisherType);
    int insert(Notice notice);
    int update(Notice notice);
    int deleteById(Integer noticeId);
}
