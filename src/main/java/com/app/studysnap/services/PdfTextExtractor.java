package com.app.studysnap.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public final class PdfTextExtractor {

    // Handles file types, call extractor and returns raw text
    public String extract(File file) throws Exception {
        String name = file.getName().toLowerCase();
        String raw;
        if (name.endsWith(".pdf")) {
            raw = extractPdf(file);
        } else if (name.endsWith(".txt")) {
            raw = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + file.getName());
        }
        return normalize(raw); // <-- make PDF behave like paste
    }

    // Extract text from .pdf file
    private String extractPdf(File f) throws Exception {
        try (PDDocument doc = PDDocument.load(f)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setSuppressDuplicateOverlappingText(true);
            stripper.setWordSeparator(" ");
            stripper.setLineSeparator("\n");
            return stripper.getText(doc);
        }
    }

    // Normalize text
    private static String normalize(String raw) {
        // Return null input as empty string
        if (raw == null) return "";

        String s = raw;

        // New lines to LF
        s = s.replace("\r\n", "\n").replace("\r", "\n");

        // Invisible and layout artifacts
        s = s.replace('\u00A0', ' ');
        s = s.replace("\u200B", "");
        s = s.replace("\u00AD", "");

        // Handle hyphen wrappers
        s = s.replaceAll("(?<=\\p{L})-\\n(?=\\p{Ll})", "");

        // Handle horizontal whitespace
        s = s.replaceAll("[ \\t\\x0B\\f]+", " ");

        // Normalize tall gaps to blank line
        s = s.replaceAll("\\n{3,}", "\n\n");

        // Trim leading/trailing whitespace
        return s.trim();
    }
}