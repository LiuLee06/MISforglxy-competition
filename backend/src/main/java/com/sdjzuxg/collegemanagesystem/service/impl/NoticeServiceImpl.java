package com.sdjzuxg.collegemanagesystem.service.impl;

import com.sdjzuxg.collegemanagesystem.entity.MeetingRoom;
import com.sdjzuxg.collegemanagesystem.entity.Notice;
import com.sdjzuxg.collegemanagesystem.entity.NoticeReceive;
import com.sdjzuxg.collegemanagesystem.entity.RoomApply;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.mapper.NoticeMapper;
import com.sdjzuxg.collegemanagesystem.mapper.NoticeReceiveMapper;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherMapper;
import com.sdjzuxg.collegemanagesystem.service.NoticeService;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements NoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    @Resource
    private NoticeReceiveMapper noticeReceiveMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public List<Notice> findAll() {
        return noticeMapper.selectAll();
    }

    @Override
    public Notice findById(Integer noticeId) {
        return noticeMapper.selectById(noticeId);
    }

    @Override
    public List<Notice> findByTeacherId(Integer teacherId) {
        return noticeMapper.selectByTeacherId(teacherId);
    }

    @Override
    public List<Notice> findByPublisherId(Integer publisherId, String publisherType) {
        return noticeMapper.selectByPublisherId(publisherId, publisherType);
    }

    @Override
    public boolean save(Notice notice) {
        return noticeMapper.insert(notice) > 0;
    }

    @Override
    @Transactional
    public boolean saveWithReceivers(Notice notice, List<Integer> receiverIds) {
        // 1. 先保存通知
        int result = noticeMapper.insert(notice);
        if (result <= 0) {
            return false;
        }

        // 2. 批量创建接收记录（对 receiverIds 去重，避免唯一键冲突）
        if (receiverIds != null && !receiverIds.isEmpty()) {
            List<Integer> uniqueIds = receiverIds.stream().distinct().collect(Collectors.toList());
            List<NoticeReceive> receiveList = new ArrayList<>();
            for (Integer teacherId : uniqueIds) {
                NoticeReceive nr = new NoticeReceive();
                Notice n = new Notice();
                n.setNoticeId(notice.getNoticeId());
                Teacher t = new Teacher();
                t.setTeacherId(teacherId);
                nr.setNotice(n);
                nr.setTeacher(t);
                nr.setIsReceived(0); // 0表示未读
                nr.setIsTodo(0);     // 0表示非待办
                // 未读状态下 confirmTime 保持为 null，仅在教师标记已读时由 markAsRead SQL 设置为 NOW()
                receiveList.add(nr);
            }
            noticeReceiveMapper.batchInsert(receiveList);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean publishForCurrentUser(Notice notice, List<Integer> receiverIds, LoginUser user) {
        if (user == null) throw new IllegalArgumentException("未登录");
        if (notice == null || notice.getTitle() == null || notice.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("通知标题不能为空");
        if (notice.getTitle().trim().length() > 200) throw new IllegalArgumentException("通知标题不能超过200字");
        if (notice.getContent() == null || notice.getContent().trim().isEmpty())
            throw new IllegalArgumentException("通知内容不能为空");
        if (notice.getContent().length() > 10000) throw new IllegalArgumentException("通知内容不能超过10000字");
        if (receiverIds == null || receiverIds.isEmpty()) throw new IllegalArgumentException("请选择接收对象");
        List<Integer> ids = receiverIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty() || ids.size() > 500) throw new IllegalArgumentException("接收人数量必须为1至500人");
        long validCount = teacherMapper.selectAll().stream().filter(t -> t.getTeacherId() != null && ids.contains(t.getTeacherId())).count();
        if (validCount != ids.size()) throw new IllegalArgumentException("接收人列表包含不存在的教师");

        Teacher publisher = user.isAdmin() ? null : teacherMapper.selectById(user.getUserId());
        notice.setPublisherId(user.getUserId());
        notice.setPublisherType(user.isAdmin() ? "admin" : "teacher");
        notice.setPublisherName(user.isAdmin() ? "admin" : publisher == null ? "teacher" : publisher.getName());
        notice.setPublishDept(user.isAdmin() ? "系统" : publisher == null ? "" : publisher.getDept());
        notice.setStatus("published");
        notice.setPublishTime(new Date());
        if (notice.getAttachments() == null) notice.setAttachments("[]");
        return saveWithReceivers(notice, ids);
    }

    /**
     * 原子性编辑通知：在同一个事务中删除旧通知（含接收记录）并插入新通知（含接收记录）
     * 避免前端"先删后增"两次请求之间出现异常导致通知永久丢失
     */
    @Override
    @Transactional
    public boolean editPublish(Integer oldNoticeId, Notice notice, List<Integer> receiverIds) {
        // 1. 先删除旧通知的接收记录和通知本身
        if (oldNoticeId != null) {
            noticeReceiveMapper.deleteByNoticeId(oldNoticeId);
            noticeMapper.deleteById(oldNoticeId);
        }

        // 2. 再插入新通知（复用 saveWithReceivers 的逻辑）
        return saveWithReceivers(notice, receiverIds);
    }

    @Override
    @Transactional
    public boolean update(Notice notice) {
        int result = noticeMapper.update(notice);
        if (result > 0 && notice.getNoticeId() != null) {
            noticeReceiveMapper.resetStatusByNoticeId(notice.getNoticeId());
        }
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteById(Integer noticeId) {
        noticeReceiveMapper.deleteByNoticeId(noticeId);
        return noticeMapper.deleteById(noticeId) > 0;
    }

    /**
     * 会议室预约审核结果通知：根据审核状态向申请人发送通知
     * - auditStatus=1（待审核→通过）：发送"已通过"通知
     * - auditStatus=2 且原状态=0（待审核→驳回）：发送"已驳回"通知
     * - auditStatus=2 且原状态=1（已通过→驳回/撤销）：发送"已撤销"通知
     * 申请人为管理员（teacher 为 null）时不发送
     */
    @Override
    public void sendRoomApplyResult(RoomApply apply, Integer auditStatus, Integer auditorId) {
        // 申请人无法识别（管理员代申请）时跳过
        if (apply == null || apply.getTeacher() == null || apply.getTeacher().getTeacherId() == null) {
            return;
        }
        Integer applicantId = apply.getTeacher().getTeacherId();

        // 解析审核人信息
        String publisherName = "系统";
        String publisherType = "admin";
        Integer publisherId = 999;
        String publishDept = "院办";
        if (auditorId != null && auditorId > 0) {
            Teacher auditor = teacherMapper.selectById(auditorId);
            if (auditor != null) {
                publisherName = auditor.getName();
                publisherType = "teacher";
                publisherId = auditorId;
                publishDept = auditor.getDept() != null ? auditor.getDept() : "院办";
            }
        }

        // 构造标题和内容
        String roomName = apply.getRoom() != null ? apply.getRoom().getRoomName() : "未知会议室";
        String timeRange = formatTimeRange(apply.getStartTime(), apply.getEndTime());
        String title;
        String content;
        if (auditStatus != null && auditStatus == 1) {
            title = "【会议室预约】您的申请已通过";
            content = "您预约的会议室【" + roomName + "】（" + timeRange + "）已审核通过，请按时使用。";
        } else {
            // 判断是"驳回"还是"撤销"：原状态为1（已通过）变为2是撤销
            boolean isRevoke = apply.getApplyStatus() != null && apply.getApplyStatus() == 1;
            if (isRevoke) {
                title = "【会议室预约】您的预约已撤销";
                content = "您预约的会议室【" + roomName + "】（" + timeRange + "）已被审核人撤销，如有疑问请联系审核人。";
            } else {
                title = "【会议室预约】您的申请已驳回";
                content = "您预约的会议室【" + roomName + "】（" + timeRange + "）已被驳回，如有疑问请联系审核人。";
            }
        }

        // 构造通知对象
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublishTime(new Date());
        notice.setPublisherId(publisherId);
        notice.setPublisherType(publisherType);
        notice.setNoticeType("会议室通知");
        notice.setPublishDept(publishDept);
        notice.setAttachments("[]");
        notice.setReceiveRoles("");
        notice.setReceiveDepts("");
        notice.setStatus("published");
        notice.setPublisherName(publisherName);

        // 发送给申请人
        saveWithReceivers(notice, Collections.singletonList(applicantId));
    }

    /**
     * 格式化时间范围：2026-08-14 09:00 ~ 2026-08-14 11:00
     */
    private String formatTimeRange(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startStr = start != null ? sdf.format(start) : "未知时间";
        String endStr = end != null ? sdf.format(end) : "未知时间";
        return startStr + " ~ " + endStr;
    }
}
