package com.sdjzuxg.collegemanagesystem.common.auth;

import com.sdjzuxg.collegemanagesystem.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 认证拦截器:解析 Authorization: Bearer <token>,把当前用户塞进 ThreadLocal。
 * 不校验权限(由 PermissionAspect 处理),只解决"是谁在调用"。
 * token 缺失/过期/非法 → 返回 401,前端跳登录页。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检直接放行(CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        String token = header.substring(7).trim();
        try {
            Claims claims = JwtUtil.parse(token);
            Integer userId = JwtUtil.getUserId(claims);
            String userType = JwtUtil.getUserType(claims);
            List<Integer> roleIds = JwtUtil.getRoleIds(claims);
            CurrentUserUtil.set(new LoginUser(userId, userType, roleIds));
            return true;
        } catch (Exception e) {
            sendUnauthorized(response, "登录态无效或已过期,请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 防止 ThreadLocal 在线程池复用时串号
        CurrentUserUtil.clear();
    }

    private void sendUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"401\",\"msg\":\"" + msg + "\",\"data\":null}");
    }
}
