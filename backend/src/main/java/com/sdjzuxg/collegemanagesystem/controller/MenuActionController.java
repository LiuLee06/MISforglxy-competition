package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.MenuAction;
import com.sdjzuxg.collegemanagesystem.service.MenuActionService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/menu-action")
public class MenuActionController {

    @Resource
    private MenuActionService menuActionService;

    @GetMapping
    public Result selectAll() {
        return Result.success(menuActionService.selectAll());
    }

    @GetMapping("/{actionId}")
    public Result selectById(@PathVariable Integer actionId) {
        return Result.success(menuActionService.selectById(actionId));
    }

    @GetMapping("/menu/{menuId}")
    public Result selectByMenuId(@PathVariable Integer menuId) {
        return Result.success(menuActionService.selectByMenuId(menuId));
    }

    @GetMapping("/code/{actionCode}")
    public Result selectByActionCode(@PathVariable String actionCode) {
        return Result.success(menuActionService.selectByActionCode(actionCode));
    }

    @AdminOnly
    @PostMapping
    public Result insert(@RequestBody MenuAction menuAction) {
        int result = menuActionService.insert(menuAction);
        return result > 0 ? Result.success("添加成功") : Result.error("400", "添加失败");
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody MenuAction menuAction) {
        int result = menuActionService.update(menuAction);
        return result > 0 ? Result.success("更新成功") : Result.error("400", "更新失败");
    }

    @AdminOnly
    @DeleteMapping("/{actionId}")
    public Result deleteById(@PathVariable Integer actionId) {
        int result = menuActionService.deleteById(actionId);
        return result > 0 ? Result.success("删除成功") : Result.error("400", "删除失败");
    }
}
