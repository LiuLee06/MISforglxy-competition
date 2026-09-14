package com.sdjzuxg.collegemanagesystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT 工具类:负责登录态 token 的生成与解析。
 * 仅作为登录态载体,不承担权限校验(权限由 PermissionAspect 处理)。
 */
public final class JwtUtil {

    private JwtUtil() {}

    /**
     * 密钥(HS256 要求 >= 32 字节)。
     * 生产环境应改为从配置文件读取,这里硬编码以便单机部署。
     */
    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            "MISforglxySecretKey202402104025房笑开CollegeManageSystem".getBytes(StandardCharsets.UTF_8));

    /** token 有效期:24 小时,与前端 localStorage 24h 过期保持一致 */
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    /**
     * 生成 JWT。
     *
     * @param userId   用户 ID(admin 的 adminId / teacher 的 teacherId)
     * @param userType "admin" / "teacher"
     * @param roleIds  角色ID列表(admin 为单值,teacher 为数组)
     * @return JWT 字符串
     */
    public static String generate(Integer userId, String userType, List<Integer> roleIds) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("roleIds", roleIds);
        Date now = new Date();
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION_MS))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token,返回 claims。token 非法/过期会抛 JwtException。
     */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public static List<Integer> getRoleIds(Claims claims) {
        Object raw = claims.get("roleIds");
        if (raw instanceof List) {
            return (List<Integer>) raw;
        }
        return List.of();
    }

    public static Integer getUserId(Claims claims) {
        Object v = claims.get("userId");
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Number) return ((Number) v).intValue();
        return null;
    }

    public static String getUserType(Claims claims) {
        Object v = claims.get("userType");
        return v == null ? null : v.toString();
    }
}
