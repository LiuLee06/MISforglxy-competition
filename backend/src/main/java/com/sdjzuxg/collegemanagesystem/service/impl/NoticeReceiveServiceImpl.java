package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.Notice;
import com.sdjzuxg.collegemanagesystem.entity.NoticeReceive;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.mapper.NoticeReceiveMapper;
import com.sdjzuxg.collegemanagesystem.service.NoticeReceiveService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NoticeReceiveServiceImpl implements NoticeReceiveService {
    @Resource
    private NoticeReceiveMapper noticeReceiveMapper;

    @Override
    public List<NoticeReceive> findAll() {
        return noticeReceiveMapper.selectAll();
    }

    @Override
    public NoticeReceive findById(Integer id) {
        return noticeReceiveMapper.selectById(id);
    }

    @Override
    public boolean save(NoticeReceive noticeReceive) {
        return noticeReceiveMapper.insert(noticeReceive) > 0;
    }

    @Override
    public boolean update(NoticeReceive noticeReceive) {
        return noticeReceiveMapper.update(noticeReceive) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return noticeReceiveMapper.deleteById(id) > 0;
    }

    @Override
    public List<NoticeReceive> findByTeacherId(Integer teacherId) {
        return noticeReceiveMapper.selectByTeacherId(teacherId);
    }

    @Override
    public boolean markAsRead(Integer noticeId, Integer teacherId) {
        NoticeReceive existing = noticeReceiveMapper.selectByNoticeIdAndTeacherId(noticeId, teacherId);
        if (existing == null) {
            NoticeReceive nr = new NoticeReceive();
            Notice n = new Notice();
            n.setNoticeId(noticeId);
            Teacher t = new Teacher();
            t.setTeacherId(teacherId);
            nr.setNotice(n);
            nr.setTeacher(t);
            nr.setIsReceived(1);
            nr.setIsTodo(0);
            nr.setConfirmTime(new Date());
            return noticeReceiveMapper.insert(nr) > 0;
        }
        return noticeReceiveMapper.markAsRead(noticeId, teacherId) > 0;
    }

    @Override
    public boolean markAsTodo(Integer noticeId, Integer teacherId) {
        NoticeReceive existing = noticeReceiveMapper.selectByNoticeIdAndTeacherId(noticeId, teacherId);
        if (existing == null) {
            NoticeReceive nr = new NoticeReceive();
            Notice n = new Notice();
            n.setNoticeId(noticeId);
            Teacher t = new Teacher();
            t.setTeacherId(teacherId);
            nr.setNotice(n);
            nr.setTeacher(t);
            nr.setIsReceived(0);
            nr.setIsTodo(1);
            // 待办状态下仍为未读，confirmTime 保持为 null
            return noticeReceiveMapper.insert(nr) > 0;
        }
        return noticeReceiveMapper.markAsTodo(noticeId, teacherId) > 0;
    }

    @Override
    public boolean unmarkTodo(Integer noticeId, Integer teacherId) {
        return noticeReceiveMapper.unmarkTodo(noticeId, teacherId) > 0;
    }

    @Override
    public java.util.Map<String, Object> getReadStats(Integer noticeId) {
        return noticeReceiveMapper.selectReadStats(noticeId);
    }

    @Override
    public List<java.util.Map<String, Object>> getReadList(Integer noticeId) {
        return noticeReceiveMapper.selectReadList(noticeId);
    }

    @Override
    public boolean remindUnread(Integer noticeId) {
        return noticeReceiveMapper.remindUnread(noticeId) > 0;
    }

    @Override
    public Map<String, Object> getCounts(Integer teacherId, Boolean hasAuditPermission) {
        Map<String, Object> counts = new HashMap<>();
        counts.put("unreadCount", noticeReceiveMapper.countUnreadByTeacherId(teacherId));
        counts.put("todoCount", noticeReceiveMapper.countTodoByTeacherId(teacherId));
        // 与预约审核列表口径一致：有审核权限统计全部待审核申请，无权限恒为0
        int pendingAudit = Boolean.TRUE.equals(hasAuditPermission)
                ? noticeReceiveMapper.countPendingAudits()
                : 0;
        counts.put("pendingAuditCount", pendingAudit);
        return counts;
    }
}
