package com.sdjzuxg.collegemanagesystem.knowledge.entity;

import lombok.Data;

import java.time.LocalDateTime;

/** 学院文件知识库目录记录。原始二进制保存在 FILE_STORE。 */
@Data
public class KnowledgeDocument {
    private Long documentId;
    private String originalFileName;
    private String storageFileName;
    private String sourceFormat;
    private Long fileSize;
    private String title;
    private String summary;
    private String keywords;
    private String documentType;
    private String markdownContent;
    private String status;
    private String errorMessage;
    private Integer uploadedBy;
    private Integer processingAttempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
