package com.sdjzuxg.collegemanagesystem.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdjzuxg.collegemanagesystem.agent.llm.MockLlmClient;
import com.sdjzuxg.collegemanagesystem.knowledge.service.KnowledgeAiService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeAiServiceTest {
    @Test
    void mockModelNormalizesMarkdownAndBuildsCatalogSummary() {
        KnowledgeAiService service = new KnowledgeAiService(new MockLlmClient(), new ObjectMapper(), 12000, 30000);
        String markdown = service.normalizeMarkdown("考核办法.docx", "# 考核办法\n\n教师按时完成考核。");
        KnowledgeAiService.KnowledgeSummary summary = service.summarize("考核办法.docx", markdown);
        assertTrue(markdown.contains("教师按时完成考核"));
        assertEquals("考核办法", summary.title());
        assertTrue(summary.summary().contains("教师按时完成考核"));
    }
}
