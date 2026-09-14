package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.service.WorkloadResultService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 教师工作量计算结果接口
 * 权限说明：菜单可见性由 MENU + 角色分配控制（前端"角色管理"页面配置），
 * 教师端查询按 currentTeacherName 做 SQL 层归属过滤，这里不加按钮级注解。
 */
@RestController
@RequestMapping("/workload-result")
public class WorkloadResultController {

    @Resource
    private WorkloadResultService workloadResultService;

    /** 管理端：查询计算结果。传 year（学年）= 聚合视图（该学年各学期按教师合计）；传 semesterId = 学期明细；均空 = 全部明细 */
    @GetMapping
    public Result list(@RequestParam(required = false) Integer semesterId,
                       @RequestParam(required = false) Integer year) {
        return Result.success(workloadResultService.findResults(semesterId, year));
    }

    /** 教师端：只查自己的计算结果 */
    @GetMapping("/my")
    public Result my(@RequestParam String currentTeacherName,
                     @RequestParam(required = false) Integer semesterId) {
        return Result.success(workloadResultService.findMyResults(currentTeacherName, semesterId));
    }

    /**
     * 管理端：按学期计算工作量并覆盖旧结果。
     * body: { semesterId: 学期, studentLimit: 人数限值 c（默认 32） }
     * 人数缺失等业务校验失败返回 400，msg 中列出具体记录。
     */
    @PostMapping("/calculate")
    public ResponseEntity<Result> calculate(@RequestBody Map<String, Object> body) {
        try {
            if (body == null) {
                return ResponseEntity.badRequest().body(Result.error("400", "请求体不能为空"));
            }
            Integer semesterId = body.get("semesterId") == null
                    ? null : Integer.valueOf(String.valueOf(body.get("semesterId")));
            Integer studentLimit = body.get("studentLimit") == null
                    || String.valueOf(body.get("studentLimit")).trim().isEmpty()
                    ? null : Integer.valueOf(String.valueOf(body.get("studentLimit")));
            int count = workloadResultService.calculate(semesterId, studentLimit);
            return ResponseEntity.ok(Result.success(count));
        } catch (IllegalArgumentException e) {
            // NumberFormatException 也是 IllegalArgumentException，非法参数统一走 400
            return ResponseEntity.badRequest().body(Result.error("400", e.getMessage()));
        }
    }
}
