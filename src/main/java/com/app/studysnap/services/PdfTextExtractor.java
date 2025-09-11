package com.app.studysnap.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public final class PdfTextExtractor {
    public String extract(File f) throws Exception {
        String name = f.getName().toLowerCase();
        if (name.endsWith(".pdf")) return extractPdf(f);
        if (name.endsWith(".txt")) return Files.readString(f.toPath(), StandardCharsets.UTF_8);
        throw new IllegalArgumentException("Unsupported file type: " + f.getName());
    }

    private String extractPdf(File f) throws Exception {
        try (PDDocument doc = PDDocument.load(f)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }
}