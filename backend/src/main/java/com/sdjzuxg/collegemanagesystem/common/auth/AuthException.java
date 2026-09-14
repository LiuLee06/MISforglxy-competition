package com.sdjzuxg.collegemanagesystem.common.auth;

/**
 * 认证层异常(未登录、token 无效等),对应 HTTP 语义的 401。
 * 由 AuthInterceptor 或 PermissionAspect 抛出,GlobalExceptionHandler 翻译为 Result.error("401", ...)
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
