package com.app.studysnap.services;

import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuizRendererTest {

    private QuizRenderer renderer;

    @BeforeEach
    void setup() {
        renderer = new QuizRenderer();
    }

    @Test
    void testRenderEmptyQuiz() {
        Quiz quiz = new Quiz(null, null, null, true, 1);
        quiz.setQuestions(List.of());

        String result = renderer.renderAsText(quiz, false);
        assertEquals("", result);
    }

    @Test
    void testRenderTitleAndSubject() {
        Quiz quiz = new Quiz("My Quiz", "Math", "Description", true, 1);
        quiz.setQuestions(List.of());

        String result = renderer.renderAsText(quiz, false);
        assertTrue(result.startsWith("My Quiz — Math"));
    }

    @Test
    void testRenderSingleQuestionWithoutAnswer() {
        Quiz quiz = new Quiz("Title", "Subject", "", true, 1);

        Question q = new Question();
        q.setQuestion("What is 2+2?");
        q.setOption1("1");
        q.setOption2("2");
        q.setOption3("3");
        q.setOption4("4");
        q.setCorrectOption(4);

        quiz.setQuestions(List.of(q));

        String result = renderer.renderAsText(quiz, false);

        assertTrue(result.contains("1. What is 2+2?"));
        assertTrue(result.contains("A) 1"));
        assertTrue(result.contains("B) 2"));
        assertTrue(result.contains("C) 3"));
        assertTrue(result.contains("D) 4"));
        assertFalse(result.contains("Answer:"));
    }

    @Test
    void testRenderSingleQuestionWithAnswer() {
        Quiz quiz = new Quiz("Title", "Subject", "", true, 1);

        Question q = new Question();
        q.setQuestion("What is 2+2?");
        q.setOption1("1");
        q.setOption2("2");
        q.setOption3("3");
        q.setOption4("4");
        q.setCorrectOption(4);

        quiz.setQuestions(List.of(q));

        String result = renderer.renderAsText(quiz, true);

        assertTrue(result.contains("Answer: D"));
    }

    @Test
    void testRenderNullOptionsAndQuestion() {
        Quiz quiz = new Quiz("Title", "Subject", "", true, 1);

        Question q = new Question(); // all null
        quiz.setQuestions(List.of(q));

        String result = renderer.renderAsText(quiz, true);

        assertTrue(result.contains("1. "));
        assertFalse(result.contains("A)"));
        assertFalse(result.contains("Answer:"));
    }

    @Test
    void testRenderMultipleQuestions() {
        Quiz quiz = new Quiz("Title", "Subject", "", true, 1);

        Question q1 = new Question();
        q1.setQuestion("Q1?");
        q1.setOption1("A1");
        q1.setOption2("B1");
        q1.setCorrectOption(1);

        Question q2 = new Question();
        q2.setQuestion("Q2?");
        q2.setOption1("A2");
        q2.setOption2("B2");
        q2.setOption3("C2");
        q2.setCorrectOption(2);

        quiz.setQuestions(List.of(q1, q2));

        String result = renderer.renderAsText(quiz, true);

        assertTrue(result.contains("1. Q1?"));
        assertTrue(result.contains("2. Q2?"));
        assertTrue(result.contains("Answer: A"));
        assertTrue(result.contains("Answer: B"));
    }
}
