package com.app.studysnap.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class IQuestionDAOTest {
    IQuestionDAO iQuestionDAO;

    @BeforeEach
    void setUp() {
        try {
            iQuestionDAO = null;
        } catch (Throwable t) {
            iQuestionDAO = null;
        }
    }

    // Test if method to add questions exists
    @Test
    void addQuestion_MethodExists() throws Exception {
        Class<?> MyClass = IQuestionDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("addQuestion", Question.class));
    }

    // Test if method to get questions by ID exists
    @Test
    void getQuestionById_MethodExists() throws Exception {
        Class<?> MyClass = IQuestionDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getQuestionById", int.class));
    }

    // Test if method to get questions for quiz ID exists
    @Test
    void getQuestionsForQuiz_MethodExists() throws Exception {
        Class<?> MyClass = IQuestionDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getQuestionsForQuiz", int.class));
    }

    // Test if method to update a question exists
    @Test
    void updateQuestion_MethodExists() throws Exception {
        Class<?> MyClass = IQuestionDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("updateQuestion", Question.class));
    }

    // Test if method to delete a question exists
    @Test
    void deleteQuestion_MethodExists() throws Exception {
        Class<?> MyClass = IQuestionDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("deleteQuestion", int.class));
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(IQuestionDAO.class);
    }
}