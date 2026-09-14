package com.sdjzuxg.collegemanagesystem.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.entity.Semester;
import com.sdjzuxg.collegemanagesystem.service.SemesterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/semester")
public class SemesterController {
    @Resource
    private SemesterService semesterService;

    @GetMapping
    public Result findAll(@RequestParam(required = false) String semesterName) {
        if (semesterName != null && !semesterName.trim().isEmpty()) {
            return Result.success(semesterService.findByName(semesterName));
        }
        return Result.success(semesterService.findAll());
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id) {
        return Result.success(semesterService.findById(id));
    }

    @GetMapping("/current")
    public Result findCurrent() {
        return Result.success(semesterService.findCurrent());
    }

    @AdminOnly
    @PostMapping
    public ResponseEntity<Result> save(@RequestBody Semester semester) {
        return semesterService.save(semester)
                ? ResponseEntity.ok(Result.success())
                : ResponseEntity.internalServerError().body(Result.error());
    }

    @AdminOnly
    @PutMapping("/{id}")
    public ResponseEntity<Result> update(@PathVariable Integer id, @RequestBody Semester semester) {
        semester.setSemesterId(id);
        return semesterService.update(semester)
                ? ResponseEntity.ok(Result.success())
                : ResponseEntity.status(404).body(Result.error("404", "学期不存在"));
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteById(@PathVariable Integer id) {
        return semesterService.deleteById(id)
                ? ResponseEntity.ok(Result.success())
                : ResponseEntity.status(404).body(Result.error("404", "学期不存在"));
    }
}
