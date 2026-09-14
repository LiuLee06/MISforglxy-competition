package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.Notice;
import com.sdjzuxg.collegemanagesystem.entity.RoomApply;

import java.util.List;

public interface NoticeService {
    List<Notice> findAll();
    Notice findById(Integer noticeId);
    List<Notice> findByTeacherId(Integer teacherId);
    List<Notice> findByPublisherId(Integer publisherId, String publisherType);
    boolean save(Notice notice);
    boolean saveWithReceivers(Notice notice, List<Integer> receiverIds);
    boolean editPublish(Integer oldNoticeId, Notice notice, List<Integer> receiverIds);
    boolean update(Notice notice);
    boolean deleteById(Integer noticeId);

    /**
     * 会议室预约审核结果通知：根据审核状态向申请人发送通知
     * @param apply 预约申请（含会议室、申请人、时间等信息）
     * @param auditStatus 审核状态：1-通过，2-驳回（含已通过→驳回的撤销）
     * @param auditorId 审核人ID
     */
    void sendRoomApplyResult(RoomApply apply, Integer auditStatus, Integer auditorId);
}
