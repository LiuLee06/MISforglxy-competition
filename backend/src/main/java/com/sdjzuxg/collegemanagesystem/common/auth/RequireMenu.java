package com.sdjzuxg.collegemanagesystem.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法上,声明所需的前端菜单路径访问权限。
 * 由 PermissionAspect 切面拦截:管理员免校验,普通用户需 ROLE_MENU + MENU 中存在该 menu_url。
 *
 * 用法:
 *   @RequireMenu("/meeting-audit")
 *   @PutMapping("/{id}/audit")
 *   public Result audit(...) { ... }
 *
 * 与前端 hasAuditPermission() 判断 menus.includes('/meeting-audit') 的规则保持一致。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireMenu {
    /** 所需的菜单路径,例如 "/meeting-audit" */
    String value();
}
