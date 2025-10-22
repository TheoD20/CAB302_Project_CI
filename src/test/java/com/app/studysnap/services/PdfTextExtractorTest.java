package com.app.studysnap.services;

import com.app.studysnap.exceptions.ValidationException;
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

        Exception ex = assertThrows(ValidationException.class,
                () -> extractor.extract(fake));

        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }
}

