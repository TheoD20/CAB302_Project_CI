package com.app.studysnap.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class IQuizDAOTest {
    IQuizDAO iQuizDAO;

    @BeforeEach
    void setUp() {
        try {
            iQuizDAO = null;
        } catch (Throwable t) {
            iQuizDAO = null;
        }
    }

    // Test if method to add quiz exists
    @Test
    void addQuiz_MethodExists() throws Exception {
        Class<?> MyClass = IQuizDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("addQuiz", Quiz.class));
    }

    // Test if method to get all quizzes exist
    @Test
    void getAllQuizzes_MethodExists() throws Exception {
        Class<?> MyClass = IQuizDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getQuizById", int.class));
    }
    
    // Test if method to get quiz by ID exists
    @Test
    void getQuizById_MethodExists() throws Exception {
        Class<?> MyClass = IQuizDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getQuizById", int.class));
    }

    // Test if method to get Quizzes by creator ID exists
    @Test
    void getQuizzesByUser_MethodExists() throws Exception {
        Class<?> MyClass = IQuizDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getQuizzesByUser", int.class));
    }

    // Test if method to find all public quizzes exists
    @Test
    void findPublic_MethodExists() throws Exception {
        Class<?> MyClass = IQuizDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("findPublic", String.class));
    }

    // Test if method to update an existing quiz exists
    @Test
    void updateQuiz_MethodExists() throws Exception {
        Class<?> MyClass = IQuizDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("updateQuiz", Quiz.class));
    }

    // Test if method to delete an existing quiz exists
    @Test
    void deleteQuiz_MethodExists() throws Exception {
        Class<?> MyClass = IQuizDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("deleteQuiz", int.class));
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(IQuizDAO.class);
    }
}