package com.sdjzuxg.collegemanagesystem.common.auth;

/**
 * ThreadLocal 持有当前请求的 LoginUser。
 * AuthInterceptor 在请求开始时 set,请求结束时 clear,防止线程池复用导致串号。
 */
public final class CurrentUserUtil {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private CurrentUserUtil() {}

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 便捷方法:取当前用户ID,未登录返回 null */
    public static Integer getUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUserId();
    }

    /** 便捷方法:判断当前是否管理员(未登录返回 false) */
    public static boolean isAdmin() {
        LoginUser u = HOLDER.get();
        return u != null && u.isAdmin();
    }
}
