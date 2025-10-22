package com.app.studysnap.services;

import com.app.studysnap.exceptions.DataAccessException;
import com.app.studysnap.exceptions.ValidationException;
import com.app.studysnap.model.Quiz;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.app.studysnap.services.TextParser.*;

/**
 * Utility for exporting quiz text to a simple PDF using PDFBox.
 * <p>
 * This class does not depend on JavaFX and can be called from background
 * threads.
 * </p>
 */
public final class PdfExporter {

    private static final float MARGIN = 48f;
    private static final float LEADING = 14f;
    private static final float FONT_SIZE = 11f;

    private static final PDType1Font FONT = PDType1Font.HELVETICA;

    public PdfExporter() {}

    /**
     * Builds the text that will be exported.
     * @param withAnswers whether to include answers
     * @param currentPreviewText the text currently shown to the user (may omit answers)
     * @param lastWithAnswers last full text with answers, if available
     * @param lastQuestions questions model from which we can render text
     * @param renderer renderer used to produce the text representation
     * @return text ready to be exported
     * @throws ValidationException if {@code renderer} is null when it is needed
     */
    public static String buildExportText(boolean withAnswers, String currentPreviewText, String lastWithAnswers, List<com.app.studysnap.model.Question> lastQuestions, QuizRenderer renderer) {
        if (!withAnswers) {
            return currentPreviewText;
        }
        if (!isBlank(lastWithAnswers)) {
            return lastWithAnswers;
        }
        if (lastQuestions != null && !lastQuestions.isEmpty()) {
            if (renderer == null) {
                throw new ValidationException("Renderer is required to build export text with answers.");
            }
            var q = new Quiz("Export", null, null, true, 0);
            q.setQuestions(lastQuestions);
            return renderer.renderAsText(q, true);
        }
        return currentPreviewText;
    }

    /**
     * Writes the provided plain text content to a PDF file.
     * @param content the text content to write
     * @param destination target PDF file (parent directories will be created if missing)
     * @throws ValidationException if {@code destination} or {@code content} is null/blank
     * @throws DataAccessException if the PDF cannot be written.
     */
    public void export(String content, File destination) {
        if (destination == null) {
            throw new ValidationException("Destination file must not be null.");
        }
        if (isBlank(content)) {
            throw new ValidationException("Nothing to export.");
        }

        try {
            // ensure parent dirs exist
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
        } catch (Throwable t) {
            throw new DataAccessException("Unable to create parent directories for: " + destination, t);
        }

        try (PDDocument doc = new PDDocument()) {
            String[] lines = content.replace("\r", "").split("\n");

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.setFont(FONT, FONT_SIZE);

            float y = page.getMediaBox().getHeight() - MARGIN;
            float usableWidth = page.getMediaBox().getWidth() - MARGIN * 2;

            for (String raw : lines) {
                for (String wrapped : wrap(raw, usableWidth)) {
                    if (y <= MARGIN) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        cs.setFont(FONT, FONT_SIZE);
                        y = page.getMediaBox().getHeight() - MARGIN;
                    }
                    cs.beginText();
                    cs.newLineAtOffset(MARGIN, y);
                    cs.showText(wrapped);
                    cs.endText();
                    y -= LEADING;
                }
                y -= LEADING * 0.5f; // paragraph spacing
            }
            cs.close();
            doc.save(destination);
        } catch (Throwable t) {
            throw new DataAccessException("Failed to write PDF to: " + destination, t);
        }
    }

    /**
     * Soft word-wrap for a given width using the configured font/size.
     * @param text line of text to wrap
     * @param width usable width in points
     * @return wrapped lines
     */
    private static List<String> wrap(String text, float width) throws IOException {
        List<String> out = new ArrayList<>();
        if (isBlank(text)) { out.add(""); return out; }

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            String candidate = line.isEmpty() ? w : line + " " + w;
            float tw = FONT.getStringWidth(candidate) / 1000f * FONT_SIZE;
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