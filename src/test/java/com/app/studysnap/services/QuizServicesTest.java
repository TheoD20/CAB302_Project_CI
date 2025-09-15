package com.app.studysnap.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;

class QuizServiceTest {

    private QuizService service;

    @BeforeEach
    void setup() {
        service = new QuizService();
    }

    @Test
    void testGenerateFromPasteIncludesAnswers() {
        String pasted = "Question text\nA) 1\nB) 2\nC) 3\nD) 4\nE) 5\nAnswer: A";
        String result = service.generateFromPaste(pasted, true);

        assertNotNull(result);
        assertTrue(result.contains("Answer: A"));
    }

    @Test
    void testGenerateFromPasteExcludesAnswers() {
        String pasted = "Question text\nA) 1\nB) 2\nC) 3\nD) 4\nE) 5\nAnswer: A";
        String result = service.generateFromPaste(pasted, false);

        assertNotNull(result);
        assertFalse(result.contains("Answer:"));
    }

    @Test
    void testGenerateFromPrompt() {
        String prompt = "Generate 2 MCQs about Java.";
        String result = service.generateFromPrompt(prompt, 2, true);

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    void testGenerateFromUploadTxtFile() throws Exception {
        // Create temporary TXT file
        File temp = File.createTempFile("quiztest", ".txt");
        try (PrintWriter out = new PrintWriter(temp)) {
            out.println("Sample question?\nA) 1\nB) 2\nC) 3\nD) 4\nE) 5\nAnswer: A");
        }

        String result = service.generateFromUpload(temp, true);

        assertNotNull(result);
        assertTrue(result.contains("Sample question?"));
        assertTrue(result.contains("Answer: A"));

        temp.delete();
    }

    @Test
    void testGenerateFromUploadUnsupportedFile() throws Exception {
        File temp = File.createTempFile("quiztest", ".docx");

        Exception ex = assertThrows(Exception.class, () -> service.generateFromUpload(temp, true));
        assertTrue(ex.getMessage().contains("Unsupported file type"));

        temp.delete();
    }
}

