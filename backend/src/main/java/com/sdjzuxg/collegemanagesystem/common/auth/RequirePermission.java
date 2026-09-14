package com.sdjzuxg.collegemanagesystem.common.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法上,声明所需 actionCode。
 * 由 PermissionAspect 切面拦截:管理员免校验,普通用户需 ROLE_MENU_ACTION 表中存在该 code。
 *
 * 用法:
 *   @RequirePermission("teacher:add")
 *   @PostMapping
 *   public Result save(@RequestBody Teacher teacher) { ... }
 *
 * 与前端 v-if="hasPermission('xxx')" 使用的 code 保持一致。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /** 所需的 actionCode,例如 "teacher:add" / "teacher:edit" / "dept:delete" */
    String value();
}
