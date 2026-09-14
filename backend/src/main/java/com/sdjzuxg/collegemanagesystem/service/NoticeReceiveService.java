package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.NoticeReceive;

import java.util.List;

public interface NoticeReceiveService {
    List<NoticeReceive> findAll();
    NoticeReceive findById(Integer id);
    boolean save(NoticeReceive noticeReceive);
    boolean update(NoticeReceive noticeReceive);
    boolean deleteById(Integer id);
    List<NoticeReceive> findByTeacherId(Integer teacherId);
    boolean markAsRead(Integer noticeId, Integer teacherId);
    boolean markAsTodo(Integer noticeId, Integer teacherId);
    boolean unmarkTodo(Integer noticeId, Integer teacherId);
    java.util.Map<String, Object> getReadStats(Integer noticeId);
    List<java.util.Map<String, Object>> getReadList(Integer noticeId);
    boolean remindUnread(Integer noticeId);
    java.util.Map<String, Object> getCounts(Integer teacherId, Boolean hasAuditPermission);
}
