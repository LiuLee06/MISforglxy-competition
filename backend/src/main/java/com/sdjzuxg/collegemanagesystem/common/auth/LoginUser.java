package com.sdjzuxg.collegemanagesystem.common.auth;

import java.util.List;

/**
 * 当前登录用户上下文。由 AuthInterceptor 从 JWT 解析后塞入 ThreadLocal,
 * Controller / Service / Aspect 可通过 CurrentUserUtil.get() 取出。
 */
public class LoginUser {
    private final Integer userId;
    private final String userType;
    private final List<Integer> roleIds;

    public LoginUser(Integer userId, String userType, List<Integer> roleIds) {
        this.userId = userId;
        this.userType = userType;
        this.roleIds = roleIds;
    }

    public Integer getUserId() { return userId; }

    public String getUserType() { return userType; }

    public List<Integer> getRoleIds() { return roleIds; }

    /** 超级管理员判断:admin 类型,或角色列表含 1 */
    public boolean isAdmin() {
        if ("admin".equals(userType)) return true;
        if (roleIds == null) return false;
        return roleIds.contains(1);
    }
}
