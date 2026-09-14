package com.sdjzuxg.collegemanagesystem.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 写方法上,表示该接口仅允许管理员(admin)调用。
 * 不依赖任何 actionCode,只看 CurrentUserUtil 的 userType 是否为 admin。
 * 适用于:系统配置、角色/菜单/按钮权限管理、管理员账号管理等纯系统级操作。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOnly {
}
