package com.app.studysnap.services;

import com.app.studysnap.model.Quiz;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.app.studysnap.services.TextParser.isBlank;
import static com.app.studysnap.services.TextParser.trim;


// Writes plain text to a simple multipage PDF.
public final class PdfExporter {

    // Prepares text for import
    public static String buildExportText(boolean withAnswers, String currentPreviewText, String lastWithAnswers, List<com.app.studysnap.model.Question> lastQuestions, QuizRenderer renderer) {
        if (!withAnswers) {
            return currentPreviewText;
        }
        if (lastWithAnswers != null && !lastWithAnswers.isBlank()) {
            return lastWithAnswers;
        }
        if (lastQuestions != null && !lastQuestions.isEmpty()) {
            var q = new Quiz("Export", null, null, true, 0);
            q.setQuestions(lastQuestions);
            return renderer.renderAsText(q, true);
        }
        return currentPreviewText;
    }

    // Handles exporting
    public void export(String content, File destination) throws Exception {
        // ensure parent dirs exist
        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (PDDocument doc = new PDDocument()) {
            final float margin = 48f, leading = 14f;
            String[] lines = content.replace("\r","").split("\n");
            var page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            var cs = new PDPageContentStream(doc, page);
            cs.setFont(PDType1Font.HELVETICA, 11f);

            float y = page.getMediaBox().getHeight() - margin;
            float usableWidth = page.getMediaBox().getWidth() - margin*2;

            for (String raw : lines) {
                for (String wrapped : wrap(raw, usableWidth)) {
                    if (y <= margin) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        cs.setFont(PDType1Font.HELVETICA, 11f);
                        y = page.getMediaBox().getHeight() - margin;
                    }
                    cs.beginText();
                    cs.newLineAtOffset(margin, y);
                    cs.showText(wrapped);
                    cs.endText();
                    y -= leading;
                }
                y -= leading * 0.5f;
            }
            cs.close();
            doc.save(destination);
        }
    }

    private static List<String> wrap(String text, float width) throws Exception {
        List<String> out = new ArrayList<>();
        if (text.isBlank()) { out.add(""); return out; }
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String candidate = line.isEmpty() ? w : line + " " + w;
            float tw = PDType1Font.HELVETICA.getStringWidth(candidate) / 1000f * (float) 11.0;
            if (tw > width) {
                if (!line.isEmpty()) out.add(line.toString());
                line = new StringBuilder(w);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) out.add(line.toString());
        return out;
    }

    // format pdf file name for download
    public static String safeFileName(String s) {
        String base = (isBlank(s)) ? "quiz" : trim(s);
        return base.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}