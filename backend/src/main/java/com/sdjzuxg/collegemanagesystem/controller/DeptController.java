package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.entity.Dept;
import com.sdjzuxg.collegemanagesystem.service.DeptService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/dept")
public class DeptController {
    @Resource
    private DeptService deptService;

    @GetMapping
    public Result findAll() {
        return Result.success(deptService.findAll());
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(deptService.findById(id));
    }

    @RequirePermission("dept:add")
    @PostMapping
    public Result save(@RequestBody Dept dept) {
        return deptService.save(dept) ? Result.success() : Result.error();
    }

    @RequirePermission("dept:edit")
    @PutMapping
    public Result update(@RequestBody Dept dept) {
        return deptService.update(dept) ? Result.success() : Result.error();
    }

    @RequirePermission("dept:delete")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Integer id) {
        return deptService.deleteById(id) ? Result.success() : Result.error();
    }
}
