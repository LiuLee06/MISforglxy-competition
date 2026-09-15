package com.sdjzuxg.collegemanagesystem.common;

import com.sdjzuxg.collegemanagesystem.common.auth.AuthException;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局异常处理器:
 *   AuthException      → code 401 (未登录/token 失效,需要重新登录)
 *   ForbiddenException → code 403 (已登录但无权限)
 *   RuntimeException   → code 500 (通用服务器错误,附详细信息便于前端排查)
 * HTTP 响应统一 200,让 axios 走 then 分支;真正的状态放在 Result.code 中。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleAuth(AuthException e) {
        return Result.error("401", e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleForbidden(ForbiddenException e) {
        return Result.error("403", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleRuntime(RuntimeException e) {
        log.error("Unhandled runtime exception type={}", e.getClass().getSimpleName(), e);
        return Result.error("500", "服务器内部错误，请稍后重试");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleValidation(IllegalArgumentException e) {
        return Result.error("400", e.getMessage() == null ? "请求参数无效" : e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleOther(Exception e) {
        log.error("Unhandled checked exception type={}", e.getClass().getSimpleName(), e);
        return Result.error("500", "服务器内部错误，请稍后重试");
    }
}
