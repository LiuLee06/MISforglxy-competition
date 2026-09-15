package com.sdjzuxg.collegemanagesystem.agent;

import com.sdjzuxg.collegemanagesystem.agent.tool.AgentTool;
import com.sdjzuxg.collegemanagesystem.agent.tool.AgentToolConfiguration;
import com.sdjzuxg.collegemanagesystem.agent.tool.AgentToolResult;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.entity.Notice;
import com.sdjzuxg.collegemanagesystem.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoticeUnreadToolTest {
    @AfterEach
    void clearUser() {
        CurrentUserUtil.clear();
    }

    @Test
    void aggregatesUnreadPeopleWithoutExposingNoticeId() {
        NoticeService noticeService = mock(NoticeService.class);
        NoticeReceiveService noticeReceiveService = mock(NoticeReceiveService.class);
        Notice notice = new Notice();
        notice.setNoticeId(101);
        notice.setTitle("期末材料提交通知");
        when(noticeService.findByPublisherId(9, "admin")).thenReturn(List.of(notice));
        when(noticeReceiveService.getReadList(101)).thenReturn(List.of(
                Map.of("isReceived", 0, "teacherName", "张三", "teacherDept", "教务部"),
                Map.of("isReceived", 1, "teacherName", "李四", "teacherDept", "教务部")
        ));
        CurrentUserUtil.set(new LoginUser(9, "admin", List.of()));

        AgentToolConfiguration configuration = new AgentToolConfiguration(
                mock(TeacherService.class), mock(WorkloadService.class), mock(WorkloadResultService.class),
                mock(RoomApplyService.class), mock(MeetingRoomService.class), mock(SemesterService.class),
                mock(OriginalExamService.class), mock(FinalExamService.class), noticeService, noticeReceiveService);
        AgentTool tool = configuration.queryNoticeReadStatsTool();

        assertFalse(String.valueOf(tool.getParametersSchema()).contains("noticeId"));
        AgentToolResult result = tool.execute(Map.of());
        assertTrue(result.isSuccess());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(1, data.get("totalUnreadPeople"));
        assertEquals(1, ((List<?>) data.get("records")).size());
        Map<?, ?> row = (Map<?, ?>) ((List<?>) data.get("records")).get(0);
        assertEquals("期末材料提交通知", row.get("noticeTitle"));
        assertEquals("张三", row.get("unreadPerson"));
        assertFalse(row.containsKey("noticeId"));
    }
}
