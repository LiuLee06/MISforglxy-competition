package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.RequirePermission;
import com.sdjzuxg.collegemanagesystem.entity.Workload;
import com.sdjzuxg.collegemanagesystem.service.WorkloadService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workload")
public class WorkloadController {
    @Resource
    private WorkloadService workloadService;

    @GetMapping
    public Result list(@RequestParam(required = false) String teacherNo,
                       @RequestParam(required = false) String teacherName,
                       @RequestParam(required = false) String exactTeacherName,
                       @RequestParam(required = false) String courseName,
                       @RequestParam(required = false) Integer semesterId,
                       @RequestParam(required = false) Integer confirmStatus) {
        return Result.success(workloadService.findByConditions(
                teacherNo, teacherName, exactTeacherName, courseName, semesterId, confirmStatus));
    }

    @GetMapping("/related")
    public Result myRelated(@RequestParam String currentTeacherName,
                            @RequestParam(required = false) String courseName,
                            @RequestParam(required = false) String teacherName,
                            @RequestParam(required = false) Integer semesterId) {
        return Result.success(workloadService.findMyRelated(
                currentTeacherName, courseName, teacherName, semesterId));
    }

    // 前端无页面调用该接口（前端 workloadApi.getDepartmentWorkloads 已同步注释），暂不启用
    /*
    @GetMapping("/department")
    public Result department(@RequestParam Integer teacherId,
                             @RequestParam(required = false) String teacherName,
                             @RequestParam(required = false) String courseName,
                             @RequestParam(required = false) Integer semesterId,
                             @RequestParam(required = false) Integer confirmStatus) {
        return Result.success(workloadService.findDepartmentWorkloads(
                teacherId, teacherName, courseName, semesterId, confirmStatus));
    }
    */

    @GetMapping("/completion")
    public Result completion(@RequestParam(required = false) Integer semesterId) {
        return Result.success(workloadService.findCompletion(semesterId));
    }

    @GetMapping("/department-completion")
    public Result departmentCompletion(@RequestParam Integer teacherId,
                                       @RequestParam(required = false) Integer semesterId) {
        return Result.success(workloadService.findDepartmentCompletion(teacherId, semesterId));
    }

    // 前端编辑回填直接使用表格行数据，无页面调用该接口（前端 workloadApi.getById 已同步注释），暂不启用
    /*
    @GetMapping("/{id}")
    public ResponseEntity<Result> findById(@PathVariable Integer id) {
        Workload workload = workloadService.findById(id);
        return workload == null
                ? ResponseEntity.status(404).body(Result.error("404", "工作量记录不存在"))
                : ResponseEntity.ok(Result.success(workload));
    }
    */

    /**
     * 批量导入工作量,对应 plan:import 权限码
     */
    @RequirePermission("plan:import")
    @PostMapping("/imports")
    public ResponseEntity<Result> batchImport(@RequestBody List<Workload> list) {
        try {
            return workloadService.batchImport(list)
                    ? ResponseEntity.ok(Result.success(list.size()))
                    : ResponseEntity.badRequest().body(Result.error("400", "导入数据不能为空"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        }
    }

    @AdminOnly
    @DeleteMapping(params = "semesterId")
    public ResponseEntity<Result> deleteBySemesterId(@RequestParam Integer semesterId) {
        try {
            int deleted = workloadService.deleteBySemesterId(semesterId);
            return ResponseEntity.ok(Result.success(deleted));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        }
    }

    /**
     * 教师修改自己名下的工作量实际学时。
     * 数据级权限:Service 层根据 currentTeacherName 做匹配,这里不加注解。
     */
    @PutMapping("/{id}/actual-hours")
    public ResponseEntity<Result> updateActualHours(@PathVariable Integer id,
                                                    @RequestBody Map<String, Object> body) {
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body(Result.error("400", "请求体不能为空"));
            }
            String currentTeacherName = body.get("currentTeacherName") == null
                    ? null : String.valueOf(body.get("currentTeacherName"));
            BigDecimal actualHours = body.get("actualHours") == null
                    || String.valueOf(body.get("actualHours")).trim().isEmpty()
                    ? null : new BigDecimal(String.valueOf(body.get("actualHours")));
            return workloadService.updateActualHours(id, currentTeacherName, actualHours)
                    ? ResponseEntity.ok(Result.success())
                    : ResponseEntity.badRequest()
                            .body(Result.error("400", "修改失败，记录不存在、当前教师无效或已经确认"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        }
    }

    /**
     * 教师确认自己名下的工作量。
     */
    @PutMapping("/{id}/confirmation")
    public ResponseEntity<Result> confirm(@PathVariable Integer id,
                                          @RequestBody Map<String, Object> body) {
        if (body == null) {
            return ResponseEntity.badRequest().body(Result.error("400", "请求体不能为空"));
        }
        String currentTeacherName = body.get("currentTeacherName") == null
                ? null : String.valueOf(body.get("currentTeacherName"));
        return workloadService.confirm(id, currentTeacherName)
                ? ResponseEntity.ok(Result.success())
                : ResponseEntity.badRequest()
                        .body(Result.error("400", "确认失败，记录不存在、当前教师无效或已经确认"));
    }
}
