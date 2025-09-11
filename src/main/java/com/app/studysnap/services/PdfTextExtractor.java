package com.app.studysnap.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public final class PdfTextExtractor {
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

    private static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw;

        s = s.replace("\r\n", "\n").replace("\r", "\n");
        s = s.replace('\u00A0', ' ').replace("\u200B", "");
        s = s.replace("\u00AD", "");
        s = s.replaceAll("(?<=\\p{L})-\\n(?=\\p{Ll})", "");
        s = s.replaceAll("(?<!\\n)\\n(?!\\n)", " ");
        s = s.replaceAll("[ \\t\\x0B\\f]+", " ");
        s = s.replaceAll("\\n{3,}", "\n\n");
        s = s.replace('“', '"').replace('”', '"').replace('’', '\'');

        return s.trim();
    }
}