package com.sdjzuxg.collegemanagesystem.knowledge.service;

import com.sdjzuxg.collegemanagesystem.knowledge.entity.KnowledgeDocument;
import com.sdjzuxg.collegemanagesystem.knowledge.mapper.KnowledgeDocumentMapper;
import com.sdjzuxg.collegemanagesystem.mapper.FileStoreMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.sql.Blob;
import java.util.Map;

/** 后台执行 Word 转换、模型校验和目录摘要生成。 */
@Component
public class KnowledgeDocumentProcessor {
    private final KnowledgeDocumentMapper documentMapper;
    private final FileStoreMapper fileStoreMapper;
    private final WordDocumentParser parser;
    private final KnowledgeAiService aiService;

    public KnowledgeDocumentProcessor(KnowledgeDocumentMapper documentMapper, FileStoreMapper fileStoreMapper,
                                      WordDocumentParser parser, KnowledgeAiService aiService) {
        this.documentMapper = documentMapper;
        this.fileStoreMapper = fileStoreMapper;
        this.parser = parser;
        this.aiService = aiService;
    }

    @Async("knowledgeTaskExecutor")
    public void processAsync(Long documentId) {
        try {
            KnowledgeDocument document = documentMapper.selectById(documentId);
            if (document == null) throw new IllegalArgumentException("知识库文件不存在");
            Map<String, Object> row = fileStoreMapper.selectContentByName(document.getStorageFileName());
            byte[] content = contentBytes(row == null ? null : row.get("content"));
            if (content == null || content.length == 0) throw new IllegalArgumentException("原始文件内容不存在");

            String draft = parser.parse(document.getOriginalFileName(), content);
            String markdown = aiService.normalizeMarkdown(document.getOriginalFileName(), draft);
            KnowledgeAiService.KnowledgeSummary summary = aiService.summarize(document.getOriginalFileName(), markdown);

            document.setMarkdownContent(markdown);
            document.setTitle(summary.title());
            document.setSummary(summary.summary());
            document.setKeywords(summary.keywords());
            document.setDocumentType(summary.documentType());
            documentMapper.updateProcessed(document);
        } catch (Exception e) {
            String message = e.getMessage() == null || e.getMessage().isBlank() ? "文档处理失败" : e.getMessage();
            if (message.length() > 900) message = message.substring(0, 900);
            documentMapper.markFailed(documentId, message);
        }
    }

    private byte[] contentBytes(Object value) {
        if (value instanceof byte[] bytes) return bytes;
        if (value instanceof Blob blob) {
            try { return blob.getBytes(1, (int) blob.length()); }
            catch (Exception ignored) { return null; }
        }
        return null;
    }
}
