package com.app.studysnap.model;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QuizTest {
    Quiz quiz;

    @BeforeEach
    void setUp() {
        try {
            quiz = new Quiz("Title", "Subject", "Desc", false, 42);
        } catch (Throwable t) {
            quiz = null;
        }
    }

    // Test if full constructor exists
    @Test
    void Quiz_FullConstructor_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredConstructor(int.class, String.class, String.class, String.class, boolean.class, int.class));
    }

    // Full constructor: Test assigning for valid inputs
    @Test
    void FullConstructor_WithAllValues_SetsAllFields() {
        Quiz q = new Quiz(7, "T", "S", "D", true, 9);
        assertEquals(7, q.getQuizId());
        assertEquals("T", q.getTitle());
        assertEquals("S", q.getSubject());
        assertEquals("D", q.getDescription());
        assertTrue(q.get_is_private());
        assertEquals(9, q.getCreatedBy());
        assertNull(q.getQuestions());
    }

    // Full constructor: Test for null inputs and negative values -> should raise error
    @Test
    void FullConstructor_Negative_NullArgs_Throws() {
        Quiz q1 = new Quiz(-1, "T", "S", "D", false, -2);
        assertEquals(-1, q1.getQuizId());
        assertEquals(-2, q1.getCreatedBy());

        Quiz q2 = new Quiz(0, "T", "S", "D", true, 0);
        assertEquals(0, q2.getQuizId());
        assertEquals(0, q2.getCreatedBy());

        assertThrows(Exception.class, () -> new Quiz(0, null, null, null, false, 0));
    }

    // Test if constructor with no specified ID exists
    @Test
    void Quiz_NoID_Constructor_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredConstructor(String.class, String.class, String.class, boolean.class, int.class));
    }

    // No ID constructor: Test assigning for valid inputs
    @Test
    void Constructor_WithoutId_SetsFields_AndLeavesIdZero() {
        Quiz q = new Quiz("T", "S", "D", true, 9);
        assertEquals(0, q.getQuizId());
        assertEquals("T", q.getTitle());
        assertEquals("S", q.getSubject());
        assertEquals("D", q.getDescription());
        assertTrue(q.get_is_private());
        assertEquals(9, q.getCreatedBy());
        assertNull(q.getQuestions());
    }

    // No ID Constructor: Test for null inputs -> should raise error
    @Test
    void NoID_Constructor_NullArgs_Throws() {
        assertThrows(Exception.class, () -> new Quiz(null, null, null, false, 0));
    }

    // Test if constructor for no provided Description exists
    @Test
    void Quiz_NoDescription_Constructor_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredConstructor(String.class, String.class, boolean.class, int.class));
    }

    // No Description Constructor: Test assigning for valid input
    @Test
    void Constructor_WithoutDescription_SetsNullDescription() {
        Quiz q = new Quiz("T", "S", true, 9);
        assertEquals("T", q.getTitle());
        assertEquals("S", q.getSubject());
        assertNull(q.getDescription());
        assertTrue(q.get_is_private());
        assertEquals(9, q.getCreatedBy());
    }

    // No Description Constructor: Test for null inputs -> should raise error
    @Test
    void NoDescription_Constructor_NullArgs_Throws() {
        assertThrows(Exception.class, () -> new Quiz(null, null, false, 0));
    }

    // Test if all required getters exists
    @Test
    void getQuizId_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("getQuizId"));
    }
    @Test
    void getTitle_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("getTitle"));
    }
    @Test
    void getSubject_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("getSubject"));
    }
    @Test
    void getDescription_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("getDescription"));
    }
    @Test
    void get_is_private_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("get_is_private"));
    }
    @Test
    void getCreatedBy_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("getCreatedBy"));
    }
    @Test
    void getQuestions_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("getQuestions"));
    }

    // Test if all required setters exists
    @Test
    void setQuizId_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("setQuizId", int.class));
    }
    @Test
    void setTitle_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("setTitle", String.class));
    }
    @Test
    void setSubject_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("setSubject", String.class));
    }
    @Test
    void setDescription_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("setDescription", String.class));
    }
    @Test
    void set_is_private_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("set_is_private", boolean.class));
    }
    @Test
    void setCreatedBy_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("setCreatedBy", int.class));
    }
    @Test
    void setQuestions_MethodExists() throws Exception {
        Class<?> MyClass = Quiz.class;
        assertNotNull(MyClass.getDeclaredMethod("setQuestions", List.class));
    }

    // Validate setters behaviour:
    // Passing null/default and max arguments
    // If getters reflect setters.
    @Test
    void setQuizId_WithZeroAndMaxValue_SetsValues() {
        quiz.setQuizId(0);
        assertEquals(0, quiz.getQuizId());
        quiz.setQuizId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, quiz.getQuizId());
    }
    @Test
    void setTitle_WithNullAndValue_SetGet() {
        quiz.setTitle(null);
        assertNull(quiz.getTitle());
        quiz.setTitle("New Title");
        assertEquals("New Title", quiz.getTitle());
    }
    @Test
    void setSubject_WithNullAndValue_SetGet() {
        quiz.setSubject(null);
        assertNull(quiz.getSubject());
        quiz.setSubject("Math");
        assertEquals("Math", quiz.getSubject());
    }
    @Test
    void setDescription_WithNullAndValue_SetGet() {
        quiz.setDescription(null);
        assertNull(quiz.getDescription());
        quiz.setDescription("Algebra basics");
        assertEquals("Algebra basics", quiz.getDescription());
    }
    @Test
    void set_is_private_TogglesCorrectly() {
        quiz.set_is_private(false);
        assertFalse(quiz.get_is_private());
        quiz.set_is_private(true);
        assertTrue(quiz.get_is_private());
    }
    @Test
    void setCreatedBy_WithZeroAndPositive_SetsValues() {
        quiz.setCreatedBy(0);
        assertEquals(0, quiz.getCreatedBy());
        quiz.setCreatedBy(123);
        assertEquals(123, quiz.getCreatedBy());
    }
    @Test
    void setQuestions_WithNullAndNonEmptyList_GetSet() {
        quiz.setQuestions(null);
        assertNull(quiz.getQuestions());
        List<Question> list = new ArrayList<>();
        list.add(new Question());
        quiz.setQuestions(list);
        assertNotNull(quiz.getQuestions());
        assertEquals(1, quiz.getQuestions().size());
        assertSame(list, quiz.getQuestions());
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(Quiz.class);
    }
}