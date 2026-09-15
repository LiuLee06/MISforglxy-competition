package com.sdjzuxg.collegemanagesystem.agent;

import com.sdjzuxg.collegemanagesystem.agent.tool.AgentToolConfiguration;
import com.sdjzuxg.collegemanagesystem.agent.tool.AgentToolResult;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.entity.Workload;
import com.sdjzuxg.collegemanagesystem.mapper.WorkloadMapper;
import com.sdjzuxg.collegemanagesystem.service.impl.WorkloadServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadIdentityTest {
    @Mock WorkloadMapper workloadMapper;
    @InjectMocks WorkloadServiceImpl workloadService;

    @AfterEach void clear() { CurrentUserUtil.clear(); }

    @Test
    void workloadLookupUsesUniqueTeacherNoForSameNamedTeachers() {
        Workload mine = new Workload(); mine.setTeacherNo("T001"); mine.setTeacherName("张伟");
        Workload other = new Workload(); other.setTeacherNo("T002"); other.setTeacherName("张伟");
        when(workloadMapper.selectMyRelatedByTeacherNo("T001", null, null, null)).thenReturn(List.of(mine));
        assertEquals(List.of(mine), workloadService.findMyRelatedByTeacherNo("T001", null, null, null));
        verify(workloadMapper).selectMyRelatedByTeacherNo("T001", null, null, null);
        verify(workloadMapper, never()).selectMyRelated(anyString(), any(), any(), any());
        assertEquals("T002", other.getTeacherNo());
    }
}
