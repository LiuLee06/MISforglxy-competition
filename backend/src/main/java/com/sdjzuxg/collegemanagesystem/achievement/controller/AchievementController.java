package com.sdjzuxg.collegemanagesystem.achievement.controller;

import com.sdjzuxg.collegemanagesystem.achievement.dto.AchievementQueryDTO;
import com.sdjzuxg.collegemanagesystem.achievement.entity.Achievement;
import com.sdjzuxg.collegemanagesystem.achievement.ocr.AchievementOcrService;
import com.sdjzuxg.collegemanagesystem.achievement.service.AchievementService;
import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 成果管理接口：成果收集 / 人工验证 / 成果展示三个页面共用。
 *
 * 权限：成果管理并入"教学特色专区"，归属系统管理员（admin）使用，
 * 因此所有接口统一以 @AdminOnly 约束（管理员拥有一切权限，普通教师不可调用）。
 * 数据存放于成果本地库，与现有业务库隔离。
 */
@RestController
@RequestMapping("/achievement")
public class AchievementController {

    /** 附件大小上限：10MB（成果证书类图片/PDF，超出提示用户） */
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    @Resource
    private AchievementService achievementService;

    @Resource
    private AchievementOcrService achievementOcrService;

    /** 与 FileController 共用同一份附件存储目录配置 */
    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /**
     * 启动时把相对路径归一化为基于 JVM 启动目录的绝对路径，
     * 与 FileController 处理方式保持一致，避免读写出错。
     */
    @PostConstruct
    public void normalizeUploadPath() {
        File dir = new File(uploadPath);
        if (!dir.isAbsolute()) {
            uploadPath = new File(System.getProperty("user.dir"), uploadPath).getAbsolutePath() + File.separator;
        }
    }

    // ==================== 字典与查询 ====================

    /** 下拉字典：成果类别 -> 分类等级、级别、等级 */
    @AdminOnly
    @GetMapping("/options")
    public Result options() {
        return Result.success(achievementService.options());
    }

    /** 分页列表（人工验证/成果展示共用，靠 status 区分） */
    @AdminOnly
    @GetMapping
    public Result list(@ModelAttribute AchievementQueryDTO query) {
        return Result.success(achievementService.page(query));
    }

    /** 成果展示页统计 + 图表数据 */
    @AdminOnly
    @GetMapping("/stats")
    public Result stats(@ModelAttribute AchievementQueryDTO query) {
        return Result.success(achievementService.stats(query));
    }

    // ==================== 新增 / 编辑 / 审核 / 删除 ====================

    /**
     * 成果提交（成果收集页）。
     * 参数校验与 Publish 系列接口保持同样的风格：缺字段直接返回 400 文案。
     */
    @AdminOnly
    @PostMapping
    public Result save(@RequestBody Achievement achievement) {
        String validMsg = validate(achievement);
        if (validMsg != null) {
            return Result.error("400", validMsg);
        }
        return achievementService.save(achievement) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody Achievement achievement) {
        if (achievement == null || achievement.getId() == null) {
            return Result.error("400", "缺少成果ID");
        }
        String validMsg = validate(achievement);
        if (validMsg != null) {
            return Result.error("400", validMsg);
        }
        return achievementService.update(achievement) ? Result.success() : Result.error();
    }

    /** 单条审核：status=1 通过，2 驳回 */
    @AdminOnly
    @PostMapping("/{id}/verify")
    public Result verify(@PathVariable Long id, @RequestParam Integer status) {
        return achievementService.verify(id, status) ? Result.success() : Result.error("400", "审核失败，请刷新后重试");
    }

    /** 批量通过：确认框中对用户显示的条数与实际生效条数一致 */
    @AdminOnly
    @PostMapping("/batch-pass")
    public Result batchPass(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params);
        if (ids.isEmpty()) {
            return Result.error("400", "未选择记录");
        }
        int updated = achievementService.batchPass(ids);
        Map<String, Object> data = new HashMap<>();
        data.put("updated", updated);
        return Result.success(data);
    }

    /** 批量删除 */
    @AdminOnly
    @PostMapping("/batch-delete")
    public Result batchDelete(@RequestBody Map<String, Object> params) {
        List<Long> ids = parseIds(params);
        if (ids.isEmpty()) {
            return Result.error("400", "未选择记录");
        }
        int deleted = achievementService.batchDelete(ids);
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", deleted);
        return Result.success(data);
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        return achievementService.deleteById(id) ? Result.success() : Result.error("404", "成果不存在或已被删除");
    }

    // ==================== 附件上传 ====================

    /**
     * 成果附件上传。与 FileController 保持一致的磁盘存储方式（支持历史文件兼容），
     * 但成果附件统一走 ./uploads/ 目录并以 UUID 重命名，返回可被静态资源映射的访问地址。
     */
    @AdminOnly
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (CurrentUserUtil.get() == null) {
            throw new ForbiddenException("未登录不可上传文件");
        }
        if (file == null || file.isEmpty()) {
            return Result.error("400", "文件为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("400", "文件大小不能超过 10MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("400", "文件名不合法");
        }
        String lower = originalFilename.toLowerCase();
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".pdf"))) {
            return Result.error("400", "仅支持 JPG/PNG/PDF 格式");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + suffix;
        try {
            File dir = new File(uploadPath);
            if (!dir.exists() && !dir.mkdirs()) {
                return Result.error("500", "上传目录创建失败");
            }
            File target = new File(dir, newFilename);
            file.transferTo(target);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("fileName", originalFilename);
            // 沿用项目已有的附件访问方式（FileController 的预览端点 + /file 已放行静态回访），
            // 文件本体仍落在 ./uploads/ 目录，历史文件也能正常读取
            data.put("fileUrl", "/file/preview/" + newFilename);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("500", "上传失败：" + e.getMessage());
        }
    }

    // ==================== OCR ====================

    /** OCR 连接状态（只读配置，不发起调用，不消耗额度） */
    @AdminOnly
    @GetMapping("/ocr/status")
    public Result ocrStatus() {
        return Result.success(achievementOcrService.status());
    }

    /** 触发 OCR 识别：仅在用户点击「开始OCR识别」时调用 */
    @AdminOnly
    @PostMapping("/ocr")
    public Result ocr(@RequestBody Map<String, String> body) {
        String fileUrl = body == null ? null : body.get("fileUrl");
        if (fileUrl == null || fileUrl.isBlank()) {
            return Result.error("400", "缺少附件地址");
        }
        try {
            return Result.success(achievementOcrService.extract(fileUrl));
        } catch (IllegalStateException e) {
            return Result.error("400", e.getMessage());
        } catch (Exception e) {
            return Result.error("500", "OCR 识别失败：" + e.getMessage());
        }
    }

    /** 成果必填项校验 */
    private String validate(Achievement achievement) {
        if (achievement == null) {
            return "成果信息不能为空";
        }
        if (isBlank(achievement.getCategory())) {
            return "请选择成果类别";
        }
        if (isBlank(achievement.getName())) {
            return "请填写成果名称";
        }
        if (isBlank(achievement.getPersons())) {
            return "请填写姓名";
        }
        if (isBlank(achievement.getLevel())) {
            return "请选择成果级别";
        }
        if (isBlank(achievement.getGrade())) {
            return "请选择成果等级";
        }
        if (achievement.getAchieveDate() == null) {
            return "请选择获得时间";
        }
        if (isBlank(achievement.getIssuer())) {
            return "请填写发证单位";
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** 兼容前端传来的 [1,2,3] 或 {ids:[1,2,3]} 两种写法 */
    @SuppressWarnings("unchecked")
    private List<Long> parseIds(Map<String, Object> params) {
        if (params == null) {
            return java.util.Collections.emptyList();
        }
        Object idsObj = params.containsKey("ids") ? params.get("ids") : null;
        if (!(idsObj instanceof List<?> raw)) {
            return java.util.Collections.emptyList();
        }
        List<Long> ids = new java.util.ArrayList<>();
        for (Object item : raw) {
            if (item == null) {
                continue;
            }
            try {
                ids.add(Long.parseLong(item.toString()));
            } catch (NumberFormatException ignored) {
                // 忽略非法 ID
            }
        }
        return ids;
    }
}
