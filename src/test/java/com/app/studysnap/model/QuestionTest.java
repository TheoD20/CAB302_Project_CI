package com.app.studysnap.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class QuestionTest {
    Question question;

    @BeforeEach
    void setUp() {
        try {
            question = new Question();
        } catch (Throwable t) {
            question = null;
        }
    }

    // Test if default constructor exists
    @Test
    void Question_DefaultConstructor_MethodExists() throws Exception {
        Class<?> MyClass = Question.class;
        assertNotNull(MyClass.getDeclaredConstructor());
    }

    // Test if constructor for no Question ID exists
    @Test
    void Question_NoIDConstructor_MethodExists() throws Exception {
        Class<?> MyClass = Question.class;
        assertNotNull(MyClass.getDeclaredConstructor(int.class, String.class, String.class, String.class, String.class, String.class, String.class, int.class));
    }

    // Test if parameterized constructor exists
    // int questionId, int quizId,
    // String question,
    // String option1, String option2, String option3, String option4, String option5,
    // int correctOption
    @Test
    void Question_FullConstructor_MethodExists() throws Exception {
        Class<?> MyClass = Question.class;
        assertNotNull(MyClass.getDeclaredConstructor(int.class, int.class, String.class, String.class, String.class, String.class, String.class, String.class, int.class));
    }

    // Test if getters exists
    @Test
    void getQuestionId_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getQuestionId"));
    }
    @Test
    void getQuizId_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getQuizId"));
    }
    @Test
    void getQuestion_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getQuestion"));
    }
    @Test
    void getOption1_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getOption1"));
    }
    @Test
    void getOption2_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getOption2"));
    }
    @Test
    void getOption3_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getOption3"));
    }
    @Test
    void getOption4_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getOption4"));
    }
    @Test
    void getOption5_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getOption5"));
    }
    @Test
    void getCorrectOption_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("getCorrectOption"));
    }

    // Test if setters exists
    @Test
    void setQuestionId_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setQuestionId", int.class));
    }
    @Test
    void setQuizId_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setQuizId", int.class));
    }
    @Test
    void setQuestion_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setQuestion", String.class));
    }
    @Test
    void setOption1_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setOption1", String.class));
    }
    @Test
    void setOption2_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setOption2", String.class));
    }
    @Test
    void setOption3_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setOption3", String.class));
    }
    @Test
    void setOption4_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setOption4", String.class));
    }
    @Test
    void setOption5_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setOption5", String.class));
    }
    @Test
    void setCorrectOption_MethodExists() throws Exception {
        Class<?> clazz = Question.class;
        assertNotNull(clazz.getDeclaredMethod("setCorrectOption", Integer.class));
    }

    // Validate setters behaviour when passing null/default arguments
    @Test
    void setQuestionId_DefaultArgs_DoesNotThrow() {
        try {
            question.setQuestionId(0);
            Assertions.assertTrue(true);
        } catch (Exception ex) {
            fail("Unexpected exception for default args");
        }
    }
    @Test
    void setQuizId_DefaultArgs_DoesNotThrow() {
        try {
            question.setQuizId(0);
            Assertions.assertTrue(true);
        } catch (Exception ex) {
            fail("Unexpected exception for default args");
        }
    }

    // Full constructor sets all fields correctly for valid, non-null inputs
    @Test
    void FullConstructor_SetsAllFields_Correctly() {
        Question q = new Question(
                11,
                22,
                "What is 2+2?",
                "1", "2", "3", "4", "5",
                4
        );

        assertEquals(11, q.getQuestionId());
        assertEquals(22, q.getQuizId());
        assertEquals("What is 2+2?", q.getQuestion());
        assertEquals("1", q.getOption1());
        assertEquals("2", q.getOption2());
        assertEquals("3", q.getOption3());
        assertEquals("4", q.getOption4());
        assertEquals("5", q.getOption5());
        assertEquals(4,  q.getCorrectOption());
    }

    // Default constructor produces default values (primitives zero, refs null)
    @Test
    void DefaultConstructor_InitialValues_AreDefaults() {
        assertEquals(0, question.getQuestionId());
        assertEquals(0, question.getQuizId());
        assertNull(question.getQuestion());
        assertNull(question.getOption1());
        assertNull(question.getOption2());
        assertNull(question.getOption3());
        assertNull(question.getOption4());
        assertNull(question.getOption5());
        assertNull(question.getCorrectOption());
    }

    // Getters should return recently setter assigned values
    @Test
    void Setters_Getters_RoundTrip_Values() {
        question.setQuestionId(7);
        question.setQuizId(9);
        question.setQuestion("Choose the correct answer");
        question.setOption1("A");
        question.setOption2("B");
        question.setOption3("C");
        question.setOption4("D");
        question.setOption5("E");
        question.setCorrectOption(2);

        assertEquals(7, question.getQuestionId());
        assertEquals(9, question.getQuizId());
        assertEquals("Choose the correct answer", question.getQuestion());
        assertEquals("A", question.getOption1());
        assertEquals("B", question.getOption2());
        assertEquals("C", question.getOption3());
        assertEquals("D", question.getOption4());
        assertEquals("E", question.getOption5());
        assertEquals(2, question.getCorrectOption());
    }

    // Setting large IDs and long strings should not break getters
    @Test
    void LargeValues_AndLongStrings_DoNotBreak() {
        String longText = "x".repeat(500);
        question.setQuestionId(Integer.MAX_VALUE);
        question.setQuizId(Integer.MAX_VALUE);
        question.setQuestion(longText);
        question.setOption1(longText);
        question.setOption2(longText);
        question.setOption3(longText);
        question.setOption4(longText);
        question.setOption5(longText);
        question.setCorrectOption(Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, question.getQuestionId());
        assertEquals(Integer.MAX_VALUE, question.getQuizId());
        assertEquals(longText, question.getQuestion());
        assertEquals(longText, question.getOption1());
        assertEquals(longText, question.getOption2());
        assertEquals(longText, question.getOption3());
        assertEquals(longText, question.getOption4());
        assertEquals(longText, question.getOption5());
        assertEquals(Integer.MAX_VALUE, question.getCorrectOption());
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(Question.class);
    }
}