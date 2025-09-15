package com.app.studysnap.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextExtractorTest {

    private PdfTextExtractor extractor;

    @BeforeEach
    void setup() {
        extractor = new PdfTextExtractor();
    }

    @Test
    void testExtractTxtFile() throws Exception {
        // Create temporary TXT file
        File temp = File.createTempFile("test", ".txt");
        try (PrintWriter out = new PrintWriter(temp)) {
            out.println("Hello World");
            out.println("Second line");
        }

        String result = extractor.extract(temp);

        assertTrue(result.contains("Hello World"));
        assertTrue(result.contains("Second line"));

        temp.delete();
    }

    @Test
    void testExtractUnsupportedFileType() {
        File fake = new File("unsupported.docx");

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> extractor.extract(fake));

        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }

    @Test
    void testNormalizeHandlesInvisibleChars() throws Exception {
        File temp = File.createTempFile("test2", ".txt");
        try (PrintWriter out = new PrintWriter(temp)) {
            out.print("Hello\u00A0World\u200B-\nnextline\u00AD");
        }

        String result = extractor.extract(temp);

        // Check normalization removed invisible chars and hyphen wrap
        assertEquals("Hello World nextline", result);

        temp.delete();
    }

    // Optional: test that extractPdf does not throw for empty PDF
    @Test
    void testExtractEmptyPdf() throws Exception {
        File tempPdf = File.createTempFile("empty", ".pdf");
        // Empty PDF
        try (var doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            doc.save(tempPdf);
        }

        String result = extractor.extract(tempPdf);
        assertEquals("", result);

        tempPdf.delete();
    }
}

