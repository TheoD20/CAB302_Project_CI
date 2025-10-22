package com.app.studysnap.services;

import com.app.studysnap.model.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextParserTest {

    private TextParser parser;

    @BeforeEach
    void setup() {
        parser = new TextParser();
    }

    @Test
    void testParseSingleQuestionWithLetterAnswer() {
        String text = """
                1. What is 2+2?
                   A) 1
                   B) 2
                   C) 3
                   D) 4
                   E) 5
                   Answer: D
                """;

        List<Question> questions = parser.parse(text);
        assertEquals(1, questions.size());

        Question q = questions.get(0);
        assertEquals("What is 2+2?", q.getQuestion());
        assertEquals("1", q.getOption1());
        assertEquals("2", q.getOption2());
        assertEquals("3", q.getOption3());
        assertEquals("4", q.getOption4());
        assertEquals("5", q.getOption5());
        assertEquals(4, q.getCorrectOption());
    }

    @Test
    void testParseQuestionWithoutEOption() {
        String text = """
                1. Capital of France?
                   A) Berlin
                   B) London
                   C) Paris
                   D) Madrid
                   Answer: C
                """;

        List<Question> questions = parser.parse(text);
        Question q = questions.get(0);

        assertEquals("Paris", q.getOption3());
        assertNull(q.getOption5());
        assertEquals(3, q.getCorrectOption());
    }

    @Test
    void testParseNumericAnswer() {
        String text = """
                1. 2+3=?
                   A) 4
                   B) 5
                   C) 6
                   D) 7
                   Answer: 2
                """;

        List<Question> questions = parser.parse(text);
        Question q = questions.get(0);
        assertEquals(2, q.getCorrectOption()); // B
    }

    @Test
    void testParseAnswerByText() {
        String text = """
                1. Color of sky?
                   A) Blue
                   B) Green
                   C) Red
                   D) Yellow
                   Answer: Blue
                """;

        List<Question> questions = parser.parse(text);
        Question q = questions.get(0);
        assertEquals(1, q.getCorrectOption()); // A
    }

    @Test
    void testParseMultipleQuestions() {
        String text = """
                1. Q1?
                   A) 1
                   B) 2
                   C) 3
                   D) 4
                   Answer: A

                2. Q2?
                   A) A
                   B) B
                   C) C
                   D) D
                   Answer: C
                """;

        List<Question> questions = parser.parse(text);
        assertEquals(2, questions.size());
        assertEquals(1, questions.get(0).getCorrectOption());
        assertEquals(3, questions.get(1).getCorrectOption());
    }

    @Test
    void testParseNullOrBlank() {
        assertTrue(parser.parse(null).isEmpty());
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse("   ").isEmpty());
    }
}

