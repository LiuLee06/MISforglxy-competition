package com.sdjzuxg.collegemanagesystem.achievement.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdjzuxg.collegemanagesystem.agent.llm.DeepSeekProperties;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 成果凭证 OCR 识别服务。
 *
 * 复用主系统 AI 助手同一套 DeepSeek 连接配置（ai.deepseek.*，见 DeepSeekProperties），
 * 不在本模块另行配置密钥；只有用户在成果收集页手动点击「开始OCR识别」时才会真正发起调用。
 */
@Service
public class AchievementOcrService {
    private static final Logger log = LoggerFactory.getLogger(AchievementOcrService.class);

    @Resource
    private DeepSeekProperties deepSeekProperties;

    @Resource
    private ObjectMapper objectMapper;

    /** 附件磁盘目录，与主系统 FileController 共用同一配置 */
    @Value("${file.upload-path:./uploads/}")
    private String uploadPath;

    /**
     * 只读连接状态，不发起任何模型调用（避免无谓消耗）。
     */
    public Map<String, Object> status() {
        String key = deepSeekProperties.getApiKey();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ready", key != null && !key.isBlank());
        data.put("configured", key != null && !key.isBlank());
        data.put("baseUrl", deepSeekProperties.getBaseUrl());
        data.put("model", deepSeekProperties.getModel());
        data.put("apiKeyMasked", key != null && key.length() > 10 ? key.substring(0, 6) + "****" + key.substring(key.length() - 4) : "");
        data.put("configSource", "ai.deepseek.*");
        return data;
    }

    /**
     * 识别指定附件并提取成果字段。
     *
     * @param fileUrl 附件访问地址，形如 /upload/xxx.png
     * @return 提取到的字段键值对
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> extract(String fileUrl) {
        String key = deepSeekProperties.getApiKey();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("OCR 未配置：未找到 DeepSeek api-key，请在 ai.deepseek.api-key 中配置");
        }
        Path filePath = resolvePath(fileUrl);
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("附件不存在：" + fileUrl);
        }

        String base64;
        String mime;
        try {
            base64 = java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(filePath));
            String lower = filePath.getFileName().toString().toLowerCase();
            mime = lower.endsWith(".pdf") ? "application/pdf" : lower.endsWith(".png") ? "image/png" : "image/jpeg";
        } catch (Exception e) {
            throw new IllegalStateException("读取附件失败：" + e.getMessage());
        }

        String prompt = String.join("\n",
                "你是证书/获奖文件识别助手。请从图中提取以下字段，严格输出 JSON（不要输出任何其他文字）：",
                "{\"name\":\"成果名称\",\"persons\":\"姓名，多人用中文顿号、分隔\",\"level\":\"成果级别，只能取：国家级/省级/市级/校级\",\"grade\":\"成果等级，如一等奖/二等奖/三等奖/优秀奖\",\"achieve_date\":\"获得时间，格式 YYYY-MM-DD\",\"issuer\":\"发证单位\",\"contest_name\":\"比赛名称，图中没有则留空字符串\"}",
                "achieve_date 注意：证书上的日期常为中文数字且只有年月（如\"二零二五年八月\"），请换算成数字并以该月 1 号输出（如 2025-08-01）；只有能确认图上完全没有日期时才留空字符串。",
                "其他无法识别的字段请留空字符串，不要编造。");

        Map<String, Object> textPart = new LinkedHashMap<>();
        textPart.put("type", "text");
        textPart.put("text", prompt);

        Map<String, Object> imageUrlObj = new LinkedHashMap<>();
        imageUrlObj.put("url", "data:" + mime + ";base64," + base64);
        Map<String, Object> imagePart = new LinkedHashMap<>();
        imagePart.put("type", "image_url");
        imagePart.put("image_url", imageUrlObj);

        Map<String, Object> userMessage = new LinkedHashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", java.util.Arrays.asList(textPart, imagePart));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", deepSeekProperties.getModel());
        body.put("messages", java.util.Collections.singletonList(userMessage));
        body.put("stream", false);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(Math.max(1, deepSeekProperties.getTimeoutSeconds())));
        RestClient client = RestClient.builder()
                .baseUrl(deepSeekProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();

        String raw = client.post().uri("/chat/completions")
                .header("Authorization", "Bearer " + key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("OCR 识别失败：模型未返回内容");
        }
        String content;
        try {
            content = objectMapper.readTree(raw).path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception e) {
            log.error("解析 OCR 返回内容失败", e);
            throw new IllegalStateException("OCR 结果解析失败");
        }
        content = content.replace("```json", "").replace("```", "").trim();

        Map<String, Object> fields;
        try {
            fields = objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            log.warn("OCR 返回的不是合法 JSON，原样返回: {}", content);
            Map<String, Object> rawResult = new HashMap<>();
            rawResult.put("_raw", content);
            return rawResult;
        }
        // 兜底把各种写法（含中文数字、只有年月）的日期统一成 yyyy-MM-dd
        Object date = fields.get("achieve_date");
        if (date instanceof String && !((String) date).isBlank()) {
            fields.put("achieve_date", normalizeDate((String) date));
        }
        return fields;
    }

    /**
     * 把附件访问地址还原到磁盘文件，并做路径穿越校验。
     * 兼容三种前缀：项目统一的 /file/preview/、静态映射的 /upload/ 与历史 /uploads/。
     */
    private Path resolvePath(String fileUrl) {
        String prefix = null;
        for (String p : new String[]{"/file/preview/", "/upload/", "/uploads/"}) {
            if (fileUrl != null && fileUrl.startsWith(p)) {
                prefix = p;
                break;
            }
        }
        if (prefix == null) {
            throw new IllegalStateException("文件地址非法");
        }
        String filename = fileUrl.substring(prefix.length());
        java.io.File base = new java.io.File(uploadPath);
        if (!base.isAbsolute()) {
            base = new java.io.File(System.getProperty("user.dir"), uploadPath);
        }
        Path basePath = Paths.get(base.getAbsolutePath()).normalize();
        Path target = basePath.resolve(filename).normalize();
        if (!target.startsWith(basePath)) {
            throw new IllegalStateException("文件地址非法");
        }
        return target;
    }

    /** 中文数字转阿拉伯数字：支持逐位式（二零二五）与组合式（二十五） */
    private static int cnNum(String s) {
        Map<Character, Integer> d = cnMap();
        if (s.chars().allMatch(c -> d.containsKey((char) c))) {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                sb.append(d.get(c));
            }
            return Integer.parseInt(sb.toString());
        }
        int n = 0;
        int temp = 0;
        for (char c : s.toCharArray()) {
            if (d.containsKey(c)) {
                temp = d.get(c);
            } else if (c == '十') {
                n += (temp == 0 ? 1 : temp) * 10;
                temp = 0;
            } else {
                return Integer.MIN_VALUE;
            }
        }
        return n + temp;
    }

    private static Map<Character, Integer> cnMap() {
        Map<Character, Integer> d = new HashMap<>();
        d.put('〇', 0);
        d.put('零', 0);
        d.put('一', 1);
        d.put('二', 2);
        d.put('两', 2);
        d.put('三', 3);
        d.put('四', 4);
        d.put('五', 5);
        d.put('六', 6);
        d.put('七', 7);
        d.put('八', 8);
        d.put('九', 9);
        return d;
    }

    /**
     * 日期归一化：把 "2025年8月" / "2025-08" / "二零二五年八月" 等写法统一成 yyyy-MM-dd，
     * 缺少日时补 01 —— 证书上常常只有年月，若不补全将导致获得时间无法入库。
     */
    static String normalizeDate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String s = value.trim();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d{4})\\s*[年\\-/\\.]\\s*(\\d{1,2})(?:\\s*[月\\-/\\.]\\s*(\\d{1,2}))?")
                .matcher(s);
        if (m.find()) {
            String day = m.group(3) == null ? "1" : m.group(3);
            return m.group(1) + "-" + pad(m.group(2)) + "-" + pad(day);
        }
        java.util.regex.Matcher my = java.util.regex.Pattern
                .compile("([〇零一二三四五六七八九十两]{2,6})\\s*年").matcher(s);
        java.util.regex.Matcher mm = java.util.regex.Pattern
                .compile("([〇零一二三四五六七八九十两]{1,3})\\s*月").matcher(s);
        if (my.find()) {
            int y = cnNum(my.group(1));
            int mo = mm.find() ? cnNum(mm.group(1)) : 1;
            if (y > 1900 && y < 2100 && mo >= 1 && mo <= 12) {
                return y + "-" + pad(String.valueOf(mo)) + "-01";
            }
        }
        return s;
    }

    private static String pad(String v) {
        return v.length() >= 2 ? v : "0" + v;
    }
}
