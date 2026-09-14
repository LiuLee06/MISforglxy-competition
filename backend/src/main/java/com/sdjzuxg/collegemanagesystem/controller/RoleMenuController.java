package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.RoleMenu;
import com.sdjzuxg.collegemanagesystem.service.RoleMenuService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/role-menu")
public class RoleMenuController {
    @Resource
    private RoleMenuService roleMenuService;

    @GetMapping
    public Result findAll() {
        return Result.success(roleMenuService.findAll());
    }

    @GetMapping("/role/{roleId}/menu/{menuId}")
    public Result findByRoleIdAndMenuId(@PathVariable Integer roleId, @PathVariable Integer menuId) {
        return Result.success(roleMenuService.findByRoleIdAndMenuId(roleId, menuId));
    }

    @GetMapping("/role/{roleId}")
    public Result findByRoleId(@PathVariable Integer roleId) {
        return Result.success(roleMenuService.findByRoleId(roleId));
    }

    @GetMapping("/role-ids/{roleId}")
    public Result findMenuIdsByRoleId(@PathVariable Integer roleId) {
        return Result.success(roleMenuService.findMenuIdsByRoleId(roleId));
    }

    @AdminOnly
    @PostMapping
    public Result save(@RequestBody RoleMenu roleMenu) {
        return roleMenuService.save(roleMenu) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody Map<String, Integer> request) {
        Integer roleId = request.get("roleId");
        Integer oldMenuId = request.get("oldMenuId");
        Integer newMenuId = request.get("newMenuId");
        return roleMenuService.update(roleId, oldMenuId, newMenuId) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/role/{roleId}/menu/{menuId}")
    public Result deleteByRoleIdAndMenuId(@PathVariable Integer roleId, @PathVariable Integer menuId) {
        return roleMenuService.deleteByRoleIdAndMenuId(roleId, menuId) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/role/{roleId}")
    public Result deleteByRoleId(@PathVariable Integer roleId) {
        int result = roleMenuService.deleteByRoleId(roleId);
        return result > 0 ? Result.success("删除成功") : Result.success("无数据可删");
    }

    @AdminOnly
    @PostMapping("/batch")
    public Result batchInsert(@RequestBody List<RoleMenu> list) {
        int result = roleMenuService.batchInsert(list);
        return result > 0 ? Result.success("批量添加成功") : Result.error("400", "批量添加失败");
    }
}
