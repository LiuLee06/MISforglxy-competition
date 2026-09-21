package com.sdjzuxg.collegemanagesystem.knowledge.service;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 只负责从 Word 中可靠提取文字、标题和原生表格，输出大模型可校验的 Markdown 初稿。
 * 图片、文本框和扫描内容不在第一版解析范围内。
 */
@Component
public class WordDocumentParser {

    public String parse(String fileName, byte[] content) throws IOException {
        String lower = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".docx")) return parseDocx(content);
        if (lower.endsWith(".doc")) return parseDoc(content);
        throw new IllegalArgumentException("只支持 .doc 和 .docx 文件");
    }

    private String parseDocx(byte[] content) throws IOException {
        StringBuilder markdown = new StringBuilder();
        // 部分正常 Word 文件会内嵌高度压缩的字体，默认阈值会将其误判为 Zip bomb。
        // 上传本身仍受文件大小限制，这里只将 POI 的压缩比阈值调整到兼容常见 Word 文件的范围。
        ZipSecureFile.setMinInflateRatio(0.005d);
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content))) {
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph paragraph) {
                    appendParagraph(markdown, paragraph);
                } else if (element instanceof XWPFTable table) {
                    appendTable(markdown, table);
                }
            }
        }
        return clean(markdown.toString());
    }

    private String parseDoc(byte[] content) throws IOException {
        StringBuilder markdown = new StringBuilder();
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(content));
             WordExtractor extractor = new WordExtractor(document)) {
            for (String paragraph : extractor.getParagraphText()) {
                String text = paragraph == null ? "" : paragraph.replace('\r', ' ').replace('\n', ' ').trim();
                if (!text.isBlank()) markdown.append(text).append("\n\n");
            }
        }
        return clean(markdown.toString());
    }

    private void appendParagraph(StringBuilder markdown, XWPFParagraph paragraph) {
        String text = paragraph.getText() == null ? "" : paragraph.getText().trim();
        if (text.isBlank()) return;
        int headingLevel = headingLevel(paragraph);
        if (headingLevel > 0) {
            markdown.append("#".repeat(headingLevel)).append(' ').append(text).append("\n\n");
        } else if (paragraph.getNumID() != null) {
            markdown.append("- ").append(text).append("\n");
        } else {
            markdown.append(text).append("\n\n");
        }
    }

    private int headingLevel(XWPFParagraph paragraph) {
        String style = paragraph.getStyle();
        if (style != null && style.toLowerCase(Locale.ROOT).contains("heading")) {
            String digits = style.replaceAll("[^0-9]", "");
            if (!digits.isBlank()) return Math.min(6, Math.max(1, Integer.parseInt(digits)));
            return 1;
        }
        try {
            if (paragraph.getCTP().getPPr() != null && paragraph.getCTP().getPPr().isSetOutlineLvl()) {
                return Math.min(6, paragraph.getCTP().getPPr().getOutlineLvl().getVal().intValue() + 1);
            }
        } catch (Exception ignored) {
            // 某些 Word 文档的 outlineLvl 不完整，按普通段落处理。
        }
        return 0;
    }

    private void appendTable(StringBuilder markdown, XWPFTable table) {
        List<List<String>> rows = new ArrayList<>();
        for (XWPFTableRow row : table.getRows()) {
            List<String> cells = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                cells.add(cleanCell(cell.getText()));
            }
            if (!cells.isEmpty()) rows.add(cells);
        }
        if (rows.isEmpty()) return;

        int columns = rows.stream().mapToInt(List::size).max().orElse(0);
        markdown.append('|');
        for (int i = 0; i < columns; i++) markdown.append(' ').append(cellAt(rows.get(0), i)).append(" |");
        markdown.append('\n').append('|');
        for (int i = 0; i < columns; i++) markdown.append(" --- |");
        markdown.append('\n');
        for (int r = 1; r < rows.size(); r++) {
            markdown.append('|');
            for (int c = 0; c < columns; c++) markdown.append(' ').append(cellAt(rows.get(r), c)).append(" |");
            markdown.append('\n');
        }
        markdown.append('\n');
    }

    private String cellAt(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }

    private String cleanCell(String text) {
        return (text == null ? "" : text).replace('|', '｜').replace('\r', ' ').replace('\n', ' ').trim();
    }

    private String clean(String markdown) {
        return markdown.replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
