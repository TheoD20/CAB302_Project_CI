package com.app.studysnap.services;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 Writes plain text to a simple multipage PDF.
*/
public final class PdfExporter {

    // in PdfExporter
    public void export(String content, File destination) throws Exception {
        // ensure parent dirs exist
        java.io.File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            final float margin = 48f, leading = 14f;
            String[] lines = content.replace("\r","").split("\n");
            var page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
            doc.addPage(page);

            var cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
            cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11f);

            float y = page.getMediaBox().getHeight() - margin;
            float usableWidth = page.getMediaBox().getWidth() - margin*2;

            for (String raw : lines) {
                for (String wrapped : wrap(raw, org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11f, usableWidth)) {
                    if (y <= margin) {
                        cs.close();
                        page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
                        doc.addPage(page);
                        cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
                        cs.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 11f);
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

    private static List<String> wrap(String text, PDType1Font font, float fontSize, float width) throws Exception {
        List<String> out = new ArrayList<>();
        if (text.isBlank()) { out.add(""); return out; }
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String candidate = line.isEmpty() ? w : line + " " + w;
            float tw = font.getStringWidth(candidate) / 1000f * fontSize;
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
}