package com.sdjzuxg.collegemanagesystem.knowledge.mapper;

import com.sdjzuxg.collegemanagesystem.knowledge.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper {
    KnowledgeDocument selectById(@Param("documentId") Long documentId);

    KnowledgeDocument selectByOriginalFileName(@Param("originalFileName") String originalFileName);

    List<KnowledgeDocument> selectAll();

    List<KnowledgeDocument> searchReady(@Param("query") String query, @Param("limit") int limit);

    int insert(KnowledgeDocument document);

    int updateUpload(KnowledgeDocument document);

    int markProcessing(@Param("documentId") Long documentId);

    int updateProcessed(KnowledgeDocument document);

    int markFailed(@Param("documentId") Long documentId, @Param("errorMessage") String errorMessage);

    int deleteById(@Param("documentId") Long documentId);
}
