package com.sdjzuxg.collegemanagesystem.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希工具类（BCrypt）。
 * BCrypt 自带随机盐，同一明文每次哈希结果不同；
 * 存储的是哈希值，校验时用 matches 比对明文与哈希。
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /** 默认密码（存量重置 / 新增用户未指定密码时使用） */
    public static final String DEFAULT_RAW_PASSWORD = "123456";

    private PasswordUtil() {
    }

    /**
     * 对明文密码进行 BCrypt 哈希。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 哈希字符串（含随机盐），如 $2a$10$...
     */
    public static String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            rawPassword = DEFAULT_RAW_PASSWORD;
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码是否与哈希匹配。
     * 兼容历史明文数据：若库中存的不是 BCrypt 哈希（不以 $2 开头），
     * 则退化为明文比对，便于灰度迁移。
     *
     * @param rawPassword 用户输入的明文密码
     * @param storedPassword 数据库中存储的密码（哈希或历史明文）
     * @return true 表示匹配
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || rawPassword.isEmpty() || storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }
        // 兼容历史明文：BCrypt 哈希固定以 $2a/$2b/$2y 开头
        if (storedPassword.startsWith("$2")) {
            return ENCODER.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }
}
