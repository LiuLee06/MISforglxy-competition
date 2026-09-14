package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.Menu;
import com.sdjzuxg.collegemanagesystem.service.MenuService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {
    @Resource
    private MenuService menuService;

    @GetMapping
    public Result findAll() {
        return Result.success(menuService.findAll());
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(menuService.findById(id));
    }

    @GetMapping("/role/{roleId}")
    public Result findByRoleId(@PathVariable Integer roleId) {
        return Result.success(menuService.findByRoleId(roleId));
    }

    @AdminOnly
    @PostMapping
    public Result save(@RequestBody Menu menu) {
        return menuService.save(menu) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody Menu menu) {
        return menuService.update(menu) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        return menuService.deleteById(id) ? Result.success() : Result.error();
    }
}
