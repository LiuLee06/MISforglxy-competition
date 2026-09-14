package com.sdjzuxg.collegemanagesystem.common.auth;

/**
 * 权限不足异常。由 PermissionAspect 抛出,由 GlobalExceptionHandler 捕获后返回 403。
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
