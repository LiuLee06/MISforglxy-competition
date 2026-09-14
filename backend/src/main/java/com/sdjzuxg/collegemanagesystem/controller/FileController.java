package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.mapper.FileStoreMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/file")
@CrossOrigin
public class FileController {

    @Resource
    private FileStoreMapper fileStoreMapper;

    // 磁盘目录仅用于兼容读取历史文件(历史附件仍存于本地磁盘);
    // 新上传的文件统一存入共享数据库 FILE_STORE 表,多节点部署时附件保持一致
    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /**
     * 启动时把相对路径归一化为基于 JVM 启动目录的绝对路径。
     * 必须转绝对路径:MultipartFile.transferTo() 遇到相对路径时会相对 Tomcat 临时目录解析,
     * 与 mkdirs/读取的基准目录不一致,会导致上传失败。
     */
    @jakarta.annotation.PostConstruct
    public void normalizeUploadPath() {
        java.io.File dir = new java.io.File(uploadPath);
        if (!dir.isAbsolute()) {
            uploadPath = new java.io.File(System.getProperty("user.dir"), uploadPath).getAbsolutePath() + java.io.File.separator;
        }
    }

    /** 拼接文件在磁盘上的完整路径(自动补齐配置末尾缺少的斜杠,防止拼出错位路径) */
    private File resolveFile(String filename) {
        String base = uploadPath.endsWith("/") || uploadPath.endsWith("\\") ? uploadPath : uploadPath + "/";
        return new File(base + filename);
    }

    /**
     * 文件上传。
     * 数据级权限:任何已登录用户(admin 或 teacher)都可以上传文件(发通知、填附件都用得到)。
     * 未登录通过 AuthInterceptor 拦截,这里只是防御性再判断一次。
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (CurrentUserUtil.get() == null) {
            throw new ForbiddenException("未登录不可上传文件");
        }
        if (file.isEmpty()) {
            return Result.error("400", "文件为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + suffix;

        try {
            // 文件二进制直接存入共享数据库 FILE_STORE 表:
            // 本地与服务器共用同一数据库,附件内容随之全局一致,不再依赖各自的磁盘目录
            fileStoreMapper.insert(newFilename, file.getBytes());

            Map<String, Object> data = new HashMap<>();
            data.put("filename", originalFilename);
            data.put("url", "/file/download/" + newFilename);
            data.put("size", file.getSize());
            data.put("type", file.getContentType());
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            // 超过 MySQL 单包上限(max_allowed_packet,默认64MB)的大文件也会走到这里
            return Result.error("500", "上传失败:" + e.getMessage());
        }
    }

    /**
     * 文件预览(HTTP 直出)。未登录也允许预览(方便通知附件给多人查看) ——
     * 通过 UUID 随机文件名保证不可枚举。如果未来要加鉴权,在 Preview.vue 调接口时
     * 后端再从 NOTICE_RECEIVE 查可见性。
     */
    @GetMapping("/preview/{filename}")
    public void preview(@PathVariable String filename, HttpServletResponse response) {
        // 优先从数据库读取(新上传的文件);查不到再兜底读磁盘(历史文件)
        byte[] dbData = selectContentFromDb(filename);
        File diskFile = resolveFile(filename);
        boolean fromDb = dbData != null && dbData.length > 0;
        if (!fromDb && !diskFile.exists()) {
            response.setStatus(404);
            return;
        }

        try (OutputStream os = response.getOutputStream()) {
            response.setContentType(getContentType(filename));
            response.setContentLength(fromDb ? dbData.length : (int) diskFile.length());
            response.setHeader("Content-Disposition", "inline; filename=\"" + URLEncoder.encode(filename, "UTF-8") + "\"");
            response.setHeader("Access-Control-Allow-Origin", "*");

            if (fromDb) {
                os.write(dbData);
            } else {
                try (InputStream is = new FileInputStream(diskFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                }
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    /** 从数据库读取文件二进制内容:单行 Map 中取出 content 字段,无记录返回 null */
    private byte[] selectContentFromDb(String filename) {
        Map<String, Object> row = fileStoreMapper.selectContentByName(filename);
        return row == null ? null : (byte[]) row.get("content");
    }

    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".txt")) return "text/plain;charset=UTF-8";
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogg")) return "video/ogg";
        if (lower.endsWith(".avi")) return "video/x-msvideo";
        if (lower.endsWith(".mov")) return "video/quicktime";
        if (lower.endsWith(".wmv")) return "video/x-ms-wmv";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lower.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".zip")) return "application/zip";
        if (lower.endsWith(".rar")) return "application/x-rar-compressed";
        return "application/octet-stream";
    }

    /**
     * 文件下载(和预览一样开放,通过随机文件名不可枚举防越权)。
     */
    @GetMapping("/download/{filename}")
    public void download(@PathVariable String filename, HttpServletResponse response) {
        // 优先从数据库读取(新上传的文件);查不到再兜底读磁盘(历史文件)
        byte[] dbData = selectContentFromDb(filename);
        File diskFile = resolveFile(filename);
        boolean fromDb = dbData != null && dbData.length > 0;
        if (!fromDb && !diskFile.exists()) {
            response.setStatus(404);
            return;
        }

        try (OutputStream os = response.getOutputStream()) {
            response.setContentType("application/octet-stream");
            response.setContentLength(fromDb ? dbData.length : (int) diskFile.length());
            String encodedFilename = URLEncoder.encode(filename, "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

            if (fromDb) {
                os.write(dbData);
            } else {
                try (InputStream is = new FileInputStream(diskFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                }
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
}
