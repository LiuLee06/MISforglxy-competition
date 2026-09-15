package com.sdjzuxg.collegemanagesystem.agent.tool;

import com.sdjzuxg.collegemanagesystem.agent.util.AgentToolSupport;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.entity.*;
import com.sdjzuxg.collegemanagesystem.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class AgentToolConfiguration {
    private final TeacherService teacherService;
    private final WorkloadService workloadService;
    private final WorkloadResultService workloadResultService;
    private final RoomApplyService roomApplyService;
    private final MeetingRoomService meetingRoomService;
    private final SemesterService semesterService;
    private final OriginalExamService originalExamService;
    private final FinalExamService finalExamService;
    private final NoticeService noticeService;
    private final NoticeReceiveService noticeReceiveService;

    public AgentToolConfiguration(TeacherService teacherService, WorkloadService workloadService,
            WorkloadResultService workloadResultService, RoomApplyService roomApplyService,
            MeetingRoomService meetingRoomService, SemesterService semesterService,
            OriginalExamService originalExamService, FinalExamService finalExamService,
            NoticeService noticeService, NoticeReceiveService noticeReceiveService) {
        this.teacherService=teacherService; this.workloadService=workloadService;
        this.workloadResultService=workloadResultService; this.roomApplyService=roomApplyService;
        this.meetingRoomService=meetingRoomService; this.semesterService=semesterService;
        this.originalExamService=originalExamService; this.finalExamService=finalExamService;
        this.noticeService=noticeService; this.noticeReceiveService=noticeReceiveService;
    }

    @Bean
    public AgentTool getCurrentUserTool() {
        return new BuiltinAgentTool("get_current_user","获取当前登录用户的基本信息",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of(),List.of()),"查询当前用户",null,null,false, a -> {
            var u=CurrentUserUtil.get(); if(u==null) return AgentToolResult.fail("未登录");
            Teacher t=teacherService.findById(u.getUserId());
            Map<String,Object> out=new LinkedHashMap<>();
            out.put("userId",u.getUserId()); out.put("userType",u.getUserType());
            if(t!=null){out.put("name",t.getName()); out.put("department",t.getDept()); out.put("title",t.getProfessionalTitle());}
            return AgentToolResult.ok(out);
        });
    }

    @Bean
    public AgentTool searchTeachersTool() {
        return new BuiltinAgentTool("search_teachers","按姓名关键词或部门查询教师，最多返回30条",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of("keyword",AgentToolRegistry.property("string","姓名关键词"),
                        "department",AgentToolRegistry.property("string","部门")) ,List.of()),"查询教师","/teacher-info",null,false,a -> {
            String keyword=AgentToolSupport.string(a,"keyword"), dept=AgentToolSupport.string(a,"department");
            List<Teacher> all=teacherService.findAll();
            List<Map<String,Object>> rows=all.stream().filter(t -> (keyword.isBlank() || safe(t.getName()).contains(keyword))
                    && (dept.isBlank() || safe(t.getDept()).contains(dept))).limit(30).map(t -> {
                Map<String,Object> m=new LinkedHashMap<>(); m.put("teacherId",t.getTeacherId()); m.put("name",t.getName());
                m.put("teacherNo",t.getStaffNo()); m.put("department",t.getDept()); m.put("title",t.getProfessionalTitle()); return m;
            }).toList();
            return AgentToolResult.ok(Map.of("total",all.stream().filter(t -> (keyword.isBlank() || safe(t.getName()).contains(keyword))
                    && (dept.isBlank() || safe(t.getDept()).contains(dept))).count(),"returned",rows.size(),"truncated",rows.size()==30,"teachers",rows));
        });
    }

    @Bean
    public AgentTool queryMyWorkloadTool() {
        return new BuiltinAgentTool("query_my_workload","查询当前登录教师本人的教学工作量",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of("semesterId",AgentToolRegistry.property("integer","学期ID，可选")),List.of()),"查询我的工作量","/teacher-workload",null,false,a -> {
            var u=CurrentUserUtil.get(); if(u==null) return AgentToolResult.fail("未登录");
            Teacher t=teacherService.findById(u.getUserId()); if(t==null) return AgentToolResult.fail("当前用户不是教师");
            Integer semesterId=AgentToolSupport.integer(a,"semesterId");
            List<Workload> list=workloadService.findMyRelatedByTeacherNo(t.getStaffNo(),null,null,semesterId);
            List<Map<String,Object>> rows=list.stream().limit(100).map(w -> {
                Map<String,Object> m=new LinkedHashMap<>(); m.put("semester",w.getSemesterName()); m.put("courseName",w.getCourseName());
                m.put("plannedHours",w.getTotalHours()); m.put("actualHours",w.getActualHours()); m.put("confirmStatus",w.getConfirmStatus()); return m;
            }).toList();
            Map<String,Object> out=new LinkedHashMap<>(); out.put("totalRecords",list.size()); out.put("records",rows); out.put("truncated",list.size()>100);
            return AgentToolResult.ok(out);
        });
    }

    @Bean
    public AgentTool queryWorkloadCompletionTool() {
        return new BuiltinAgentTool("query_workload_completion","查询学院工作量确认完成情况",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of("semesterId",AgentToolRegistry.property("integer","学期ID，可选")),List.of()),"查询工作量完成情况","/workload",null,false,a -> {
            Integer sid=AgentToolSupport.integer(a,"semesterId"); if(sid==null && semesterService.findCurrent()!=null) sid=semesterService.findCurrent().getSemesterId();
            return AgentToolResult.ok(workloadService.findCompletion(sid));
        });
    }

    @Bean
    public AgentTool findAvailableMeetingRoomsTool() {
        return new BuiltinAgentTool("find_available_meeting_rooms","查询指定日期和时间范围内可使用的会议室",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of("date",AgentToolRegistry.property("string","日期 yyyy-MM-dd"),
                        "startTime",AgentToolRegistry.property("string","开始时间 HH:mm"),
                        "endTime",AgentToolRegistry.property("string","结束时间 HH:mm"),
                        "minCapacity",AgentToolRegistry.property("integer","最小容量，可选")),List.of("date","startTime","endTime")),"查询会议室","/meeting-reserve",null,false,a -> {
            String date=AgentToolSupport.requiredString(a,"date"), start=AgentToolSupport.requiredString(a,"startTime"), end=AgentToolSupport.requiredString(a,"endTime");
            int min=Optional.ofNullable(AgentToolSupport.integer(a,"minCapacity")).orElse(0);
            if(min<0) return AgentToolResult.fail("最小容量不能为负数");
            AgentToolSupport.validateDateTimeRange(date,start,end);
            List<MeetingRoom> rooms=roomApplyService.findAvailableRooms(date,start,end,null).stream().filter(r -> Optional.ofNullable(r.getCapacity()).orElse(0)>=min).toList();
            return AgentToolResult.ok(rooms.stream().map(r -> {
                Map<String,Object> row=new LinkedHashMap<>(); row.put("roomId",r.getRoomId()); row.put("roomName",r.getRoomName()); row.put("capacity",r.getCapacity()); row.put("roomStatus",r.getRoomStatus()); return row;
            }).toList());
        });
    }

    @Bean
    public AgentTool queryMyRoomApplicationsTool() {
        return new BuiltinAgentTool("query_my_room_applications","查询当前用户的会议室申请",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of("status",AgentToolRegistry.property("integer","状态：0待审核、1通过、2驳回")),List.of()),"查询我的会议室申请","/meeting-reserve",null,false,a -> {
            var u=CurrentUserUtil.get(); if(u==null)return AgentToolResult.fail("未登录");
            List<RoomApply> list=u.isAdmin()?roomApplyService.findAll():roomApplyService.findByTeacherId(u.getUserId());
            Integer status=AgentToolSupport.integer(a,"status");
            return AgentToolResult.ok(list.stream().filter(x -> status==null || status.equals(x.getApplyStatus())).limit(100).map(x -> {
                Map<String,Object> m=new LinkedHashMap<>(); m.put("applyId",x.getApplyId()); m.put("roomName",x.getRoom()==null?null:x.getRoom().getRoomName());
                m.put("startTime",x.getStartTime()); m.put("endTime",x.getEndTime()); m.put("purpose",x.getPurpose()); m.put("status",x.getApplyStatus()); return m;
            }).toList());
        });
    }

    @Bean
    public AgentTool queryMyExamAssignmentsTool() {
        return new BuiltinAgentTool("query_my_exam_assignments","查询当前登录教师的监考安排",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of("semesterId",AgentToolRegistry.property("integer","学期ID，可选")),List.of()),"查询我的监考安排","/exam-supervise",null,false,a -> {
            var u=CurrentUserUtil.get(); if(u==null)return AgentToolResult.fail("未登录");
            Teacher t=teacherService.findById(u.getUserId()); if(t==null)return AgentToolResult.fail("当前用户不是教师");
            Integer sid=AgentToolSupport.integer(a,"semesterId"); if(sid==null&&semesterService.findCurrent()!=null)sid=semesterService.findCurrent().getSemesterId();
            List<Map<String,Object>> out=new ArrayList<>();
            if(sid!=null) {
                for(OriginalExam e:originalExamService.findBySemesterAndTeacher(sid,u.getUserId())) {
                    Map<String,Object> row=new LinkedHashMap<>(); row.put("type","original"); row.put("courseName",e.getCourseName()); row.put("startTime",e.getStartTime()); row.put("endTime",e.getEndTime()); row.put("room",e.getRoom()); row.put("role",e.getInvigilateRole()); out.add(row);
                }
                for(FinalExam e:finalExamService.findBySemesterAndTeacher(sid,u.getUserId())) {
                    Map<String,Object> row=new LinkedHashMap<>(); row.put("type","final"); row.put("courseName",e.getCourseName()); row.put("startTime",e.getStartTime()); row.put("endTime",e.getEndTime()); row.put("room",e.getRoom()); out.add(row);
                }
            }
            return AgentToolResult.ok(Map.of("count",out.size(),"assignments",out));
        });
    }

    @Bean
    public AgentTool queryNoticeReadStatsTool() {
        return new BuiltinAgentTool("query_notice_read_stats","查询当前用户发布通知的未读接收人，无需提供通知ID，可按标题关键词筛选",ToolRiskLevel.READ,
                AgentToolRegistry.schema(Map.of("titleKeyword",AgentToolRegistry.property("string","通知标题关键词，可选；不填写则查询全部通知")),List.of()),"查询通知未读人员","/notice-maintain",null,false,a -> {
            var u=CurrentUserUtil.get();
            if(u==null)return AgentToolResult.fail("未登录");
            String titleKeyword=AgentToolSupport.string(a,"titleKeyword");
            List<Notice> notices=noticeService.findByPublisherId(u.getUserId(),u.getUserType());
            List<Map<String,Object>> records=new ArrayList<>();
            int matchedNoticeCount=0;
            int noticesWithUnread=0;
            for(Notice notice:notices) {
                if(records.size()>=500) break;
                if(!titleKeyword.isBlank()&&!safe(notice.getTitle()).contains(titleKeyword))continue;
                matchedNoticeCount++;
                if(notice.getNoticeId()==null)continue;
                int unreadForNotice=0;
                for(Map<String,Object> receiver:noticeReceiveService.getReadList(notice.getNoticeId())) {
                    if(isRead(receiver.get("isReceived")))continue;
                    Map<String,Object> row=new LinkedHashMap<>();
                    row.put("noticeTitle",safe(notice.getTitle()));
                    row.put("unreadPerson",safe(Objects.toString(receiver.get("teacherName"),"")));
                    row.put("department",safe(Objects.toString(receiver.get("teacherDept"),"")));
                    row.put("publishTime",notice.getPublishTime());
                    records.add(row);
                    unreadForNotice++;
                }
                if(unreadForNotice>0)noticesWithUnread++;
            }
            Map<String,Object> out=new LinkedHashMap<>();
            out.put("matchedNoticeCount",matchedNoticeCount);
            out.put("noticesWithUnread",noticesWithUnread);
            out.put("totalUnreadPeople",records.size());
            out.put("truncated", records.size() >= 500);
            out.put("records",records);
            return AgentToolResult.ok(out);
        });
    }

    @Bean
    public AgentTool createRoomApplicationTool() {
        return new BuiltinAgentTool("create_room_application","提交会议室预约申请，执行前必须经过用户确认",ToolRiskLevel.WRITE_CONFIRM,
                AgentToolRegistry.schema(Map.of("roomId",AgentToolRegistry.property("integer","会议室ID"),
                        "date",AgentToolRegistry.property("string","日期 yyyy-MM-dd"),"startTime",AgentToolRegistry.property("string","开始时间 HH:mm"),
                        "endTime",AgentToolRegistry.property("string","结束时间 HH:mm"),"purpose",AgentToolRegistry.property("string","预约用途")),List.of("roomId","date","startTime","endTime","purpose")),"提交会议室预约","/meeting-reserve",null,false,a -> {
            try {
                var u=CurrentUserUtil.get(); if(u==null)return AgentToolResult.fail("未登录");
                Integer roomId=AgentToolSupport.requiredInteger(a,"roomId"); String date=AgentToolSupport.requiredString(a,"date");
                String start=AgentToolSupport.requiredString(a,"startTime"), end=AgentToolSupport.requiredString(a,"endTime");
                AgentToolSupport.validateDateTimeRange(date,start,end);
                MeetingRoom room=meetingRoomService.findById(roomId); if(room==null)return AgentToolResult.fail("会议室不存在");
                SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd HH:mm"); f.setLenient(false);
                RoomApply apply=new RoomApply(); apply.setRoom(room); apply.setStartTime(f.parse(date+" "+start)); apply.setEndTime(f.parse(date+" "+end)); apply.setPurpose(AgentToolSupport.requiredString(a,"purpose"));
                if(!roomApplyService.saveForCurrentUser(apply,u))return AgentToolResult.fail("预约申请提交失败");
                Map<String,Object> submitted=new LinkedHashMap<>(); submitted.put("submitted",true); submitted.put("applyId",apply.getApplyId()); submitted.put("status","待审核"); submitted.put("roomName",room.getRoomName());
                return AgentToolResult.ok(submitted);
            } catch(Exception e){return AgentToolResult.fail("预约参数无效或业务校验失败");}
        });
    }

    @Bean
    public AgentTool publishNoticeTool() {
        return new BuiltinAgentTool("publish_notice","发布通知，执行前必须经过用户确认",ToolRiskLevel.WRITE_CONFIRM,
                AgentToolRegistry.schema(Map.of("title",AgentToolRegistry.property("string","通知标题"),
                        "content",AgentToolRegistry.property("string","通知内容"),"receiverIds",Map.of("type","array","items",Map.of("type","integer")),
                        "noticeType",AgentToolRegistry.property("string","通知类型")),List.of("title","content","receiverIds")),"发布通知","/notice-publish",null,false,a -> {
            var u=CurrentUserUtil.get(); if(u==null)return AgentToolResult.fail("未登录");
            String title=AgentToolSupport.requiredString(a,"title"), content=AgentToolSupport.requiredString(a,"content");
            if(title.length()>200)return AgentToolResult.fail("通知标题不能超过200字");
            if(content.length()>10000)return AgentToolResult.fail("通知内容不能超过10000字");
            List<Integer> ids=AgentToolSupport.integerList(a,"receiverIds"); if(ids.isEmpty())return AgentToolResult.fail("请选择接收对象"); if(ids.size()>500)return AgentToolResult.fail("接收人数量不能超过500");
            Notice n=new Notice(); n.setTitle(title);n.setContent(content);n.setNoticeType(AgentToolSupport.string(a,"noticeType"));
            if(!noticeService.publishForCurrentUser(n,ids,u))return AgentToolResult.fail("通知发布失败");
            Map<String,Object> published=new LinkedHashMap<>(); published.put("published",true); published.put("noticeId",n.getNoticeId()); published.put("status","已发布");
            return AgentToolResult.ok(published);
        });
    }

    @Bean
    public AgentTool remindNoticeTool() {
        return new BuiltinAgentTool("remind_notice","按通知标题提醒未读接收人，执行前必须经过用户确认，不需要通知ID",ToolRiskLevel.WRITE_CONFIRM,
                AgentToolRegistry.schema(Map.of("noticeTitle",AgentToolRegistry.property("string","通知标题或标题关键词")),List.of("noticeTitle")),"提醒通知未读人员","/notice-maintain",null,false,a -> {
            var u=CurrentUserUtil.get();
            if(u==null)return AgentToolResult.fail("未登录");
            String title=AgentToolSupport.string(a,"noticeTitle");
            Notice n=noticeService.findByPublisherId(u.getUserId(),u.getUserType()).stream()
                    .filter(x->!title.isBlank()&&safe(x.getTitle()).contains(title)).findFirst().orElse(null);
            // 兼容已创建但尚未确认的旧操作，旧操作的 ID 仍只在后端内部解析。
            if(n==null&&a.containsKey("noticeId")) {
                Integer id=AgentToolSupport.integer(a,"noticeId");
                n=id==null?null:noticeService.findById(id);
            }
            if(n==null||(!u.isAdmin()&&!Objects.equals(n.getPublisherId(),u.getUserId())))return AgentToolResult.fail("找不到你发布的这条通知");
            return noticeReceiveService.remindUnread(n.getNoticeId())?AgentToolResult.ok(Map.of("reminded",true)):AgentToolResult.fail("提醒失败");
        });
    }

    private static boolean isRead(Object value) {
        if(value instanceof Number number)return number.intValue()==1;
        return "1".equals(Objects.toString(value,""));
    }

    private static String safe(String x){return x==null?"":x;}
}



