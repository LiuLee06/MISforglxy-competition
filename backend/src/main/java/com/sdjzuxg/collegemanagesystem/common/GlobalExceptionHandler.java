package com.sdjzuxg.collegemanagesystem.common;

import com.sdjzuxg.collegemanagesystem.common.auth.AuthException;
import com.sdjzuxg.collegemanagesystem.common.auth.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器:
 *   AuthException      → code 401 (未登录/token 失效,需要重新登录)
 *   ForbiddenException → code 403 (已登录但无权限)
 *   RuntimeException   → code 500 (通用服务器错误,附详细信息便于前端排查)
 * HTTP 响应统一 200,让 axios 走 then 分支;真正的状态放在 Result.code 中。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
        String msg = e.getMessage();
        if (msg == null) msg = e.getClass().getSimpleName();
        // 带上 MyBatis 嵌套的 cause 摘要(最多 1 层),便于排查
        Throwable cause = e.getCause();
        if (cause != null && cause != e) {
            String causeMsg = cause.getMessage();
            if (causeMsg != null) {
                msg = msg + "\n; " + causeMsg;
            }
        }
        return Result.error("500", "服务器内部错误: " + msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Result handleOther(Exception e) {
        return Result.error("500", "服务器内部错误: " + e.getMessage());
    }
}
