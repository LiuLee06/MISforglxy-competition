package com.sdjzuxg.collegemanagesystem.service;

import com.sdjzuxg.collegemanagesystem.entity.Menu;

import java.util.List;
import java.util.Map;

/**
 * 登录服务：负责管理员/教师登录认证、菜单与按钮权限组装、权限校验。
 * 开发者：房笑开 202402104025
 */
public interface LoginService {

    /**
     * 登录认证。
     * 登录方式：phone（默认，手机号）/ name（姓名）。仅对教师生效，admin 仍用账号登录。
     *
     * @param username  用户名（admin 账号 / 教师手机号 / 教师姓名）
     * @param password  明文密码
     * @param loginType 登录方式：phone（默认）/ name
     * @return 登录成功返回 userInfo Map；账号或密码错误返回 null；
     *         教师未分配角色抛出 RuntimeException("该教师未分配角色，请联系管理员")
     */
    Map<String, Object> login(String username, String password, String loginType);

    /**
     * 校验角色是否拥有指定菜单路径权限。
     *
     * @param roleId 角色 ID
     * @param path   菜单路径
     * @return 拥有权限返回 true，否则返回 false
     */
    boolean checkPermission(Integer roleId, String path);

    /**
     * 根据角色 ID 查询其可访问的菜单列表。
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    List<Menu> getMenusByRoleId(Integer roleId);
}
