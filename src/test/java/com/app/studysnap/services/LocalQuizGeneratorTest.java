package com.app.studysnap.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalQuizGeneratorTest {

    private final LocalQuizGenerator generator = new LocalQuizGenerator();

    @Test
    void generateFromText_WithNormalText_ReturnsQuestions() {
        String text = "Java is a programming language. It is widely used for building applications. " +
                "The JVM allows Java code to run on multiple platforms.";
        String result = generator.generateFromText(text, false);

        assertNotNull(result);
        assertTrue(result.contains("_____") || result.contains("1."));
    }

    @Test
    void generateFromText_WithIncludeAnswers_IncludesAnswerLabel() {
        String text = "Python is popular for data science and web development. It supports multiple paradigms.";
        String result = generator.generateFromText(text, true);

        assertNotNull(result);
        assertTrue(result.contains("Answer:"));
    }

    @Test
    void generateFromText_WithEmptyOrShortText_ReturnsMessage() {
        String result = generator.generateFromText("Short text", false);
        assertEquals("Not enough content to generate questions.", result);
    }

    @Test
    void generateFromPrompt_ProducesOutput() {
        String prompt = "Kotlin is used for Android development.";
        String result = generator.generateFromPrompt(prompt, 5, false);

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }
}

