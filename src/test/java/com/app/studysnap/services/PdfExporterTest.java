package com.app.studysnap.services;

import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfExporterTest {

    private PdfExporter exporter;
    private File tempFile;

    @BeforeEach
    void setUp() {
        exporter = new PdfExporter();
        tempFile = new File("test-export.pdf");
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testExportCreatesPdfFile() throws Exception {
        // Arrange: Create some sample questions
        Question q1 = new Question(
                1, 1, "What is 2+2?",
                "1", "2", "3", "4", "5",
                4
        );

        Question q2 = new Question(
                2, 1, "Capital of France?",
                "Berlin", "Madrid", "Paris", "Rome", "London",
                3
        );

        Quiz quiz = new Quiz("Math & Geo", "Tester", null, true, 0);
        quiz.setQuestions(List.of(q1, q2));

        QuizRenderer renderer = new QuizRenderer();
        String text = renderer.renderAsText(quiz, true);

        // Act: Export to PDF
        exporter.export(text, tempFile);

        // Assert: File exists and not empty
        assertTrue(tempFile.exists(), "PDF file should be created");
        assertTrue(tempFile.length() > 0, "PDF file should not be empty");
    }

    @Test
    void testBuildExportText_UsesExistingText() {
        String preview = "Preview text";
        String lastWithAnswers = "Last with answers";

        String result = PdfExporter.buildExportText(true, preview, lastWithAnswers, null, new QuizRenderer());

        assertEquals(lastWithAnswers, result, "Should reuse lastWithAnswers if available");
    }

    @Test
    void testBuildExportText_FallbackToPreview() {
        String preview = "Preview only";

        String result = PdfExporter.buildExportText(false, preview, null, null, new QuizRenderer());

        assertEquals(preview, result, "Should return current preview text if withAnswers=false");
    }
}
