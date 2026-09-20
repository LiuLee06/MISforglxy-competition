package com.sdjzuxg.collegemanagesystem.knowledge.service;

import com.sdjzuxg.collegemanagesystem.common.auth.LoginUser;
import com.sdjzuxg.collegemanagesystem.knowledge.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface KnowledgeDocumentService {
    List<KnowledgeDocument> list();

    KnowledgeDocument upload(MultipartFile file, LoginUser user);

    KnowledgeDocument retry(Long documentId);

    boolean delete(Long documentId);

    List<KnowledgeDocument> searchCatalog(String query, int limit);

    KnowledgeDocument getReady(Long documentId);

    Map<String, Object> readForAi(Long documentId, String query);
}
