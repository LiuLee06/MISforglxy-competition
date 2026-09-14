package com.sdjzuxg.collegemanagesystem.common.auth;

import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuActionMapper;
import com.sdjzuxg.collegemanagesystem.mapper.RoleMenuMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;

/**
 * 权限校验切面:拦截贴在 Controller 方法上的 @RequirePermission / @RequireMenu / @AdminOnly。
 * 规则(优先级 @AdminOnly 更高):
 *   1) 标注了 @AdminOnly:要求 userType == "admin",否则 403
 *   2) 标注了 @RequirePermission:
 *        · admin 免校验(项目硬规则:管理员拥有一切 actionCode)
 *        · 普通教师:查 TEACHER_ROLE + ROLE_MENU_ACTION + MENU_ACTION 是否拥有该 actionCode
 *   3) 标注了 @RequireMenu:
 *        · admin 免校验
 *        · 普通教师:查 TEACHER_ROLE + ROLE_MENU + MENU 是否拥有该 menu_url
 *          (与前端 menus 数组判断规则一致,例如 "/meeting-audit")
 *   4) CurrentUserUtil.get() == null → 抛 AuthException(401),AuthInterceptor 本应已拦截,兜底
 */
@Aspect
@Component
public class PermissionAspect {

    @Resource
    private RoleMenuActionMapper roleMenuActionMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Around("@annotation(com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly) " +
            "|| @annotation(com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission) " +
            "|| @annotation(com.sdjzuxg.collegemanagesystem.common.auth.RequireMenu)")
    public Object check(ProceedingJoinPoint pjp) throws Throwable {
        LoginUser user = CurrentUserUtil.get();
        if (user == null) {
            throw new AuthException("未登录");
        }

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        // ① @AdminOnly 优先:只有管理员能调
        if (method.isAnnotationPresent(AdminOnly.class)) {
            if (!user.isAdmin()) {
                throw new ForbiddenException("无权限执行该操作:需要管理员权限");
            }
            return pjp.proceed();
        }

        // 管理员直接放行
        if (user.isAdmin()) {
            return pjp.proceed();
        }

        // ② @RequirePermission:按 actionCode 校验
        RequirePermission permAnno = method.getAnnotation(RequirePermission.class);
        if (permAnno != null) {
            String code = permAnno.value();
            int count = roleMenuActionMapper.existsByTeacherIdAndCode(user.getUserId(), code);
            if (count <= 0) {
                throw new ForbiddenException("无权限执行该操作: " + code);
            }
            return pjp.proceed();
        }

        // ③ @RequireMenu:按菜单路径校验
        RequireMenu menuAnno = method.getAnnotation(RequireMenu.class);
        if (menuAnno != null) {
            String menuUrl = menuAnno.value();
            int count = roleMenuMapper.existsByTeacherIdAndMenuUrl(user.getUserId(), menuUrl);
            if (count <= 0) {
                throw new ForbiddenException("无权限执行该操作");
            }
        }

        return pjp.proceed();
    }
}
