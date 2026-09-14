package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.RoleMenuAction;
import com.sdjzuxg.collegemanagesystem.service.RoleMenuActionService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/role-menu-action")
public class RoleMenuActionController {

    @Resource
    private RoleMenuActionService roleMenuActionService;

    @GetMapping
    public Result selectAll() {
        return Result.success(roleMenuActionService.selectAll());
    }

    @GetMapping("/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(roleMenuActionService.selectById(id));
    }

    @GetMapping("/role/{roleId}")
    public Result selectByRoleId(@PathVariable Integer roleId) {
        return Result.success(roleMenuActionService.selectByRoleId(roleId));
    }

    @GetMapping("/action-codes/{roleId}")
    public Result selectActionCodesByRoleId(@PathVariable Integer roleId) {
        return Result.success(roleMenuActionService.selectActionCodesByRoleId(roleId));
    }

    /**
     * 按角色集合查 actionCodes —— 登录/管理都会用。允许登录态用户查询自己角色的权限码。
     * 这里不走 @AdminOnly,因为普通老师前端也要读自己登录时的权限快照(实际上 LoginServiceImpl 走 Service 直查,不调这个接口)
     */
    @PostMapping("/action-codes")
    public Result selectActionCodesByRoleIds(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> roleIds = request.get("roleIds");
        return Result.success(roleMenuActionService.selectActionCodesByRoleIds(roleIds));
    }

    @AdminOnly
    @PostMapping
    public Result insert(@RequestBody RoleMenuAction roleMenuAction) {
        int result = roleMenuActionService.insert(roleMenuAction);
        return result > 0 ? Result.success("添加成功") : Result.error("400", "添加失败");
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody RoleMenuAction roleMenuAction) {
        int result = roleMenuActionService.update(roleMenuAction);
        return result > 0 ? Result.success("更新成功") : Result.error("400", "更新失败");
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        int result = roleMenuActionService.deleteById(id);
        return result > 0 ? Result.success("删除成功") : Result.error("400", "删除失败");
    }

    @AdminOnly
    @DeleteMapping("/role/{roleId}")
    public Result deleteByRoleId(@PathVariable Integer roleId) {
        int result = roleMenuActionService.deleteByRoleId(roleId);
        return result > 0 ? Result.success("删除成功") : Result.error("400", "删除失败");
    }

    @AdminOnly
    @DeleteMapping("/role/{roleId}/action/{actionId}")
    public Result deleteByRoleIdAndActionId(@PathVariable Integer roleId, @PathVariable Integer actionId) {
        int result = roleMenuActionService.deleteByRoleIdAndActionId(roleId, actionId);
        return result > 0 ? Result.success("删除成功") : Result.error("400", "删除失败");
    }

    @AdminOnly
    @PostMapping("/batch")
    public Result batchInsert(@RequestBody List<RoleMenuAction> list) {
        int result = roleMenuActionService.batchInsert(list);
        return result > 0 ? Result.success("批量添加成功") : Result.error("400", "批量添加失败");
    }
}
