package com.sdjzuxg.collegemanagesystem.knowledge.service;

import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.knowledge.entity.KnowledgeDocument;
import com.sdjzuxg.collegemanagesystem.knowledge.mapper.KnowledgeDocumentMapper;
import com.sdjzuxg.collegemanagesystem.mapper.FileStoreMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Service
public class KnowledgeDocumentServiceImpl implements KnowledgeDocumentService {
    private final KnowledgeDocumentMapper documentMapper;
    private final FileStoreMapper fileStoreMapper;
    private final KnowledgeDocumentProcessor processor;
    private final long maxFileSizeBytes;

    public KnowledgeDocumentServiceImpl(KnowledgeDocumentMapper documentMapper, FileStoreMapper fileStoreMapper,
                                       KnowledgeDocumentProcessor processor,
                                       @Value("${ai.knowledge.max-file-size-mb:20}") int maxFileSizeMb) {
        this.documentMapper = documentMapper;
        this.fileStoreMapper = fileStoreMapper;
        this.processor = processor;
        this.maxFileSizeBytes = Math.max(1, maxFileSizeMb) * 1024L * 1024L;
    }

    @Override
    public List<KnowledgeDocument> list() {
        return documentMapper.selectAll();
    }

    @Override
    public synchronized KnowledgeDocument upload(MultipartFile file, LoginUser user) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("文件不能为空");
        if (file.getSize() > maxFileSizeBytes) throw new IllegalArgumentException("文件不能超过 " + (maxFileSizeBytes / 1024 / 1024) + "MB");
        String originalName = safeOriginalName(file.getOriginalFilename());
        String lower = originalName.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".doc") && !lower.endsWith(".docx")) throw new IllegalArgumentException("只支持 .doc 和 .docx 文件");

        try {
            byte[] content = file.getBytes();
            String storageName = UUID.randomUUID() + (lower.endsWith(".docx") ? ".docx" : ".doc");
            fileStoreMapper.insert(storageName, content);
            KnowledgeDocument existing = documentMapper.selectByOriginalFileName(originalName);
            String oldStorageName = existing == null ? null : existing.getStorageFileName();
            KnowledgeDocument target = existing == null ? new KnowledgeDocument() : existing;
            target.setOriginalFileName(originalName);
            target.setStorageFileName(storageName);
            target.setSourceFormat(lower.substring(lower.lastIndexOf('.') + 1));
            target.setFileSize(file.getSize());
            target.setStatus("PROCESSING");
            target.setUploadedBy(user == null ? null : user.getUserId());
            target.setProcessingAttempts(0);
            if (existing == null) documentMapper.insert(target);
            else documentMapper.updateUpload(target);
            if (oldStorageName != null && !oldStorageName.equals(storageName)) fileStoreMapper.deleteByName(oldStorageName);
            documentMapper.markProcessing(target.getDocumentId());
            processor.processAsync(target.getDocumentId());
            return documentMapper.selectById(target.getDocumentId());
        } catch (Exception e) {
            throw new IllegalStateException("文件上传失败，请稍后重试", e);
        }
    }

    @Override
    public KnowledgeDocument retry(Long documentId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) throw new IllegalArgumentException("知识库文件不存在");
        if (!"FAILED".equals(document.getStatus())) throw new IllegalArgumentException("只有处理失败的文件可以重试");
        documentMapper.markProcessing(documentId);
        processor.processAsync(documentId);
        return documentMapper.selectById(documentId);
    }

    @Override
    public boolean delete(Long documentId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        if (document == null) return false;
        boolean deleted = documentMapper.deleteById(documentId) > 0;
        if (deleted && document.getStorageFileName() != null) fileStoreMapper.deleteByName(document.getStorageFileName());
        return deleted;
    }

    @Override
    public List<KnowledgeDocument> searchCatalog(String query, int limit) {
        String normalized = query == null ? "" : query.trim();
        if (normalized.isBlank()) return List.of();
        int safeLimit = Math.min(10, Math.max(1, limit));
        for (String candidate : searchCandidates(normalized)) {
            if (candidate.length() < 2) continue;
            List<KnowledgeDocument> result = documentMapper.searchReady(candidate, safeLimit);
            if (!result.isEmpty()) return result;
        }
        return List.of();
    }

    @Override
    public KnowledgeDocument getReady(Long documentId) {
        KnowledgeDocument document = documentMapper.selectById(documentId);
        return document != null && "READY".equals(document.getStatus()) ? document : null;
    }

    @Override
    public Map<String, Object> readForAi(Long documentId, String query) {
        KnowledgeDocument document = getReady(documentId);
        if (document == null) return Map.of("success", false, "error", "文件不存在或尚未处理完成");
        String markdown = document.getMarkdownContent() == null ? "" : document.getMarkdownContent();
        String selected = selectRelevantSections(markdown, query);
        boolean truncated = selected.length() > 18000;
        if (truncated) selected = selected.substring(0, 18000) + "\n\n[正文片段已截断，请继续按章节查询]";
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("documentId", document.getDocumentId());
        result.put("fileName", document.getOriginalFileName());
        result.put("title", document.getTitle());
        result.put("sourceSections", extractHeadings(selected));
        result.put("content", selected);
        result.put("truncated", truncated);
        return result;
    }

    private String selectRelevantSections(String markdown, String query) {
        if (query == null || query.isBlank()) return markdown;
        String[] sections = markdown.split("(?m)(?=^#{1,6}\\s)");
        List<String> matched = new ArrayList<>();
        for (String section : sections) {
            if (containsQuery(section, query)) matched.add(section.trim());
        }
        if (!matched.isEmpty()) return String.join("\n\n", matched);
        return markdown;
    }

    private boolean containsQuery(String text, String query) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        for (String candidate : searchCandidates(query)) {
            String lowerQuery = candidate.toLowerCase(Locale.ROOT);
            if (lowerQuery.length() >= 2 && lowerText.contains(lowerQuery)) return true;
        }
        return false;
    }

    private List<String> searchCandidates(String query) {
        String compact = query == null ? "" : query.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[，。！？、；：,.!?;:]", " ")
                .replaceAll("\\s+", " ").trim();
        List<String> candidates = new ArrayList<>();
        if (!compact.isBlank()) candidates.add(compact);
        String shortened = compact.replaceFirst("^(请问|请帮我|帮我|我想了解|我想知道)\\s*", "")
                .replaceFirst("(是什么|有哪些|怎么做|如何做|如何|怎么|多少|吗|呢)$", "").trim();
        if (!shortened.isBlank() && !candidates.contains(shortened)) candidates.add(shortened);
        for (String token : shortened.split("\\s+")) {
            if (token.length() >= 2 && !candidates.contains(token)) candidates.add(token);
        }
        return candidates;
    }

    private List<String> extractHeadings(String markdown) {
        List<String> headings = new ArrayList<>();
        for (String line : markdown.split("\\R")) {
            if (line.matches("^#{1,6}\\s+.+")) headings.add(line.trim());
        }
        return headings;
    }

    private String safeOriginalName(String originalName) {
        String name = originalName == null ? "" : originalName.replace('\\', '/');
        name = Paths.get(name).getFileName().toString().trim();
        if (name.isBlank()) throw new IllegalArgumentException("文件名不能为空");
        return name.length() > 255 ? name.substring(name.length() - 255) : name;
    }
}
