package com.sdjzuxg.collegemanagesystem.knowledge;

import com.sdjzuxg.collegemanagesystem.knowledge.service.WordDocumentParser;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WordDocumentParserTest {
    @Test
    void extractsDocxHeadingParagraphAndTableAsMarkdown() throws Exception {
        XWPFDocument document = new XWPFDocument();
        document.createParagraph().setStyle("Heading1");
        document.getParagraphs().get(0).createRun().setText("教师管理办法");
        document.createParagraph().createRun().setText("教师应按规定完成年度考核。");
        var table = document.createTable(2, 2);
        table.getRow(0).getCell(0).setText("项目");
        table.getRow(0).getCell(1).setText("要求");
        table.getRow(1).getCell(0).setText("考核");
        table.getRow(1).getCell(1).setText("按时完成");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        document.write(output);
        document.close();

        String markdown = new WordDocumentParser().parse("制度.docx", output.toByteArray());
        assertTrue(markdown.contains("# 教师管理办法"));
        assertTrue(markdown.contains("教师应按规定完成年度考核。"));
        assertTrue(markdown.contains("| 项目 | 要求 |"));
        assertTrue(markdown.contains("| 考核 | 按时完成 |"));
    }
}
