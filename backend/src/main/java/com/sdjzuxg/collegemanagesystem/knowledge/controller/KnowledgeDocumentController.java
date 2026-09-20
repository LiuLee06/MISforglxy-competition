package com.sdjzuxg.collegemanagesystem.knowledge.controller;

import com.sdjzuxg.collegemanagesystem.common.Result;
import com.sdjzuxg.collegemanagesystem.common.auth.AdminOnly;
import com.sdjzuxg.collegemanagesystem.common.auth.CurrentUserUtil;
import com.sdjzuxg.collegemanagesystem.knowledge.service.KnowledgeDocumentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/knowledge/documents")
public class KnowledgeDocumentController {
    private final KnowledgeDocumentService service;

    public KnowledgeDocumentController(KnowledgeDocumentService service) {
        this.service = service;
    }

    @AdminOnly
    @GetMapping
    public Result list() {
        return Result.success(service.list());
    }

    @AdminOnly
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        return Result.success(service.upload(file, CurrentUserUtil.get()));
    }

    @AdminOnly
    @PostMapping("/{documentId}/retry")
    public Result retry(@PathVariable Long documentId) {
        return Result.success(service.retry(documentId));
    }

    @AdminOnly
    @DeleteMapping("/{documentId}")
    public Result delete(@PathVariable Long documentId) {
        return service.delete(documentId) ? Result.success() : Result.error("404", "知识库文件不存在");
    }
}
