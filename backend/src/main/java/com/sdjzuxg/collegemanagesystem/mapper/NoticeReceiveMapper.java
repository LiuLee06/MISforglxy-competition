package com.sdjzuxg.collegemanagesystem.mapper;

import com.sdjzuxg.collegemanagesystem.entity.NoticeReceive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeReceiveMapper {
    List<NoticeReceive> selectAll();
    NoticeReceive selectById(Integer id);
    List<NoticeReceive> selectByTeacherId(Integer teacherId);
    NoticeReceive selectByNoticeIdAndTeacherId(@Param("noticeId") Integer noticeId, @Param("teacherId") Integer teacherId);
    int insert(NoticeReceive noticeReceive);
    int batchInsert(List<NoticeReceive> list);
    int update(NoticeReceive noticeReceive);
    int deleteById(Integer id);
    int markAsRead(@Param("noticeId") Integer noticeId, @Param("teacherId") Integer teacherId);
    int markAsTodo(@Param("noticeId") Integer noticeId, @Param("teacherId") Integer teacherId);
    int unmarkTodo(@Param("noticeId") Integer noticeId, @Param("teacherId") Integer teacherId);
    int resetStatusByNoticeId(@Param("noticeId") Integer noticeId);
    int deleteByNoticeId(@Param("noticeId") Integer noticeId);
    java.util.Map<String, Object> selectReadStats(@Param("noticeId") Integer noticeId);
    List<java.util.Map<String, Object>> selectReadList(@Param("noticeId") Integer noticeId);
    int remindUnread(@Param("noticeId") Integer noticeId);
    // 铃铛统计：未读通知数、待办数、待审核会议室数
    Integer countUnreadByTeacherId(@Param("teacherId") Integer teacherId);
    Integer countTodoByTeacherId(@Param("teacherId") Integer teacherId);
    Integer countPendingAudits();
}
