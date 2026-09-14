package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.entity.Notice;
import com.sdjzuxg.collegemanagesystem.entity.Teacher;
import com.sdjzuxg.collegemanagesystem.mapper.TeacherMapper;
import com.sdjzuxg.collegemanagesystem.service.NoticeService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Resource
    private NoticeService noticeService;

    @Resource
    private TeacherMapper teacherMapper;

    @GetMapping
    public Result findAll() {
        return Result.success(noticeService.findAll());
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(noticeService.findById(id));
    }

    @GetMapping("/teacher/{teacherId}")
    public Result findByTeacherId(@PathVariable Integer teacherId) {
        return Result.success(noticeService.findByTeacherId(teacherId));
    }

    // 按发布人ID查询其发布的所有通知（用于"我的发布"）
    @GetMapping("/publisher/{publisherId}")
    public Result findByPublisherId(@PathVariable Integer publisherId,
                                    @RequestParam(required = false) String userType) {
        // 按 publisher_type 过滤，防止 admin 与 teacher 撞号导致"我的发布"串数据
        String publisherType = "admin".equals(userType) ? "admin" : "teacher";
        return Result.success(noticeService.findByPublisherId(publisherId, publisherType));
    }

    /**
     * 通用保存(不附带接收人,直接存 Notice 本体)。只允许 admin 调用
     * (实际业务用 /publish 路径,此接口保留兼容)
     */
    @AdminOnly
    @PostMapping
    public Result save(@RequestBody Notice notice) {
        return noticeService.save(notice) ? Result.success() : Result.error();
    }

    /**
     * 发布通知并指定接收对象。
     * 数据级权限:发布人 publisherId / publisherType 必须与 CurrentUserUtil 中的调用者一致,
     * 或调用者为 admin(admin 可代表系统发布,publishDept 固定为"系统")。
     */
    @PostMapping("/publish")
    public Result publish(@RequestBody Map<String, Object> params) {
        Notice notice = new Notice();
        List<Integer> receiverIds = parsePublishParams(params, notice, /*isEdit*/false);
        notice.setReceiveRoles("");
        notice.setReceiveDepts("");

        // 参数校验：标题、正文、接收对象必填，避免产生无人可见的"静默"通知
        String validMsg = validatePublishParams(notice, receiverIds);
        if (validMsg != null) {
            return Result.error("400", validMsg);
        }

        boolean success = noticeService.saveWithReceivers(notice, receiverIds);
        return success ? Result.success() : Result.error();
    }

    /**
     * 原子性编辑通知(同事务中删旧+发新)。
     * 数据级权限:必须是该通知原发布人(admin 也允许,因为 admin 也能代表系统编辑)
     */
    @PostMapping("/edit-publish")
    public Result editPublish(@RequestBody Map<String, Object> params) {
        Object oldIdObj = params.get("oldNoticeId");
        if (oldIdObj == null) {
            return Result.error("400", "缺少原通知ID");
        }
        Integer oldNoticeId = Integer.parseInt(oldIdObj.toString());

        Notice oldNotice = noticeService.findById(oldNoticeId);
        if (oldNotice == null) {
            return Result.error("404", "原通知不存在");
        }

        // 若传了旧通知 ID,验证调用者就是原发布人或 admin
        if (oldNoticeId != null) {
            Notice old = noticeService.findById(oldNoticeId);
            if (old != null) {
                LoginUser caller = CurrentUserUtil.get();
                if (caller != null && !caller.isAdmin()) {
                    boolean isOwner =
                            ("admin".equals(old.getPublisherType()) && "admin".equals(caller.getUserType())
                                    && old.getPublisherId() != null && old.getPublisherId().equals(caller.getUserId()))
                            || ("teacher".equals(old.getPublisherType()) && "teacher".equals(caller.getUserType())
                                    && old.getPublisherId() != null && old.getPublisherId().equals(caller.getUserId()));
                    if (!isOwner) {
                        throw new ForbiddenException("无权编辑他人发布的通知");
                    }
                }
            }
        }

        Notice notice = new Notice();
        List<Integer> receiverIds = parsePublishParams(params, notice, /*isEdit*/true);
        notice.setReceiveRoles("");
        notice.setReceiveDepts("");

        // 参数校验：标题、正文、接收对象必填
        String validMsg = validatePublishParams(notice, receiverIds);
        if (validMsg != null) {
            return Result.error("400", validMsg);
        }

        boolean success = noticeService.editPublish(oldNoticeId, notice, receiverIds);
        return success ? Result.success() : Result.error();
    }

    /**
     * 发布/编辑通用参数校验
     * @return 校验失败提示信息，通过则返回 null
     */
    private String validatePublishParams(Notice notice, List<Integer> receiverIds) {
        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            return "通知标题不能为空";
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            return "通知内容不能为空";
        }
        if (receiverIds == null || receiverIds.isEmpty()) {
            return "请选择接收对象";
        }
        return null;
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody Notice notice) {
        return noticeService.update(notice) ? Result.success() : Result.error();
    }

    /**
     * 删除通知:admin 可删任意;teacher 仅可删自己发布的。
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        Notice target = noticeService.findById(id);
        if (target == null) {
            return Result.error("404", "通知不存在");
        }
        LoginUser caller = CurrentUserUtil.get();
        if (caller != null && !caller.isAdmin()) {
            boolean isOwner =
                    ("teacher".equals(target.getPublisherType()) && "teacher".equals(caller.getUserType())
                            && target.getPublisherId() != null && target.getPublisherId().equals(caller.getUserId()))
                            || ("admin".equals(target.getPublisherType()) && "admin".equals(caller.getUserType())
                                    && target.getPublisherId() != null && target.getPublisherId().equals(caller.getUserId()));
            if (!isOwner) {
                throw new ForbiddenException("无权删除他人发布的通知");
            }
        }
        return noticeService.deleteById(id) ? Result.success() : Result.error();
    }

    /**
     * 解析发布参数:将前端传来的发布人信息(publisherId/publisherType/publisherName/publishDept)
     * 用 CurrentUserUtil 中的真实调用者覆写,防前端伪造。
     */
    private List<Integer> parsePublishParams(Map<String, Object> params, Notice notice, boolean isEdit) {
        notice.setTitle((String) params.get("title"));
        notice.setContent((String) params.get("content"));
        notice.setNoticeType((String) params.get("noticeType"));
        notice.setAttachments((String) params.get("attachments"));

        LoginUser caller = CurrentUserUtil.get();
        if (caller == null) {
            throw new ForbiddenException("未登录");
        }

        // 用真实调用者覆写发布人信息,防止前端伪造
        boolean isAdminCaller = caller.isAdmin();
        Integer realPublisherId = caller.getUserId();
        String realPublisherType = isAdminCaller ? "admin" : "teacher";
        String realPublisherName;
        String realPublishDept;

        if (isAdminCaller) {
            // admin 发布:发布部门按项目规则固定为"系统"
            realPublisherName = "admin";
            realPublishDept = "系统";
        } else {
            // teacher 发布:姓名和部门从 TEACHER 实表取,不相信前端传的
            Teacher t = teacherMapper.selectById(realPublisherId);
            realPublisherName = (t != null && t.getName() != null) ? t.getName() : "teacher";
            if (t != null && t.getDept() != null && !t.getDept().isEmpty()) {
                realPublishDept = t.getDept();
            } else {
                String frontendDept = (String) params.get("publishDept");
                realPublishDept = (frontendDept != null && !frontendDept.isEmpty()) ? frontendDept : "院办";
            }
        }

        notice.setPublisherId(realPublisherId);
        notice.setPublisherType(realPublisherType);
        notice.setPublisherName(realPublisherName);
        notice.setPublishDept(realPublishDept);
        notice.setStatus("published");
        notice.setPublishTime(new java.util.Date());

        // 接收人 ID 列表(仅传真实的整型,兼容前端字符串/数字混合)
        List<Integer> receiverIds = new ArrayList<>();
        Object receiversObj = params.get("receiverIds");
        if (receiversObj instanceof List) {
            List<?> list = (List<?>) receiversObj;
            for (Object item : list) {
                if (item != null) {
                    try {
                        receiverIds.add(Integer.parseInt(item.toString()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return receiverIds;
    }
}
