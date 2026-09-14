package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.PageResult;
import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.Role;
import com.sdjzuxg.collegemanagesystem.service.RoleService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Resource
    private RoleService roleService;

    @GetMapping
    public Result findAll() {
        return Result.success(roleService.findAll());
    }

    @GetMapping("/page")
    public Result findByPage(
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String searchMode) {
        PageResult<Role> result = roleService.findByPage(pageNum, pageSize, roleName, searchMode);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(roleService.findById(id));
    }

    @AdminOnly
    @PostMapping
    public Result save(@RequestBody Role role) {
        return roleService.save(role) ? Result.success() : Result.error();
    }

    @AdminOnly
    @PutMapping
    public Result update(@RequestBody Role role) {
        return roleService.update(role) ? Result.success() : Result.error();
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        return roleService.deleteById(id) ? Result.success() : Result.error();
    }
}
