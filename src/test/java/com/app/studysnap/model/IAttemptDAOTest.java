package com.app.studysnap.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IAttemptDAOTest {

    @Test
    void testInterfaceExists() {
        assertTrue(IAttemptDAO.class.isInterface(), "IAttemptDAO should be an interface");
    }

    @Test
    void testAllExpectedMethodsExist() throws NoSuchMethodException {
        // Verify method names and parameters exist in interface
        assertNotNull(IAttemptDAO.class.getMethod("addAttempt", Attempt.class));
        assertNotNull(IAttemptDAO.class.getMethod("getAttemptsByUser", int.class));
        assertNotNull(IAttemptDAO.class.getMethod("getAttemptsByQuiz", int.class));
        assertNotNull(IAttemptDAO.class.getMethod("getLastAttempt", int.class, int.class));
        assertNotNull(IAttemptDAO.class.getMethod("getCorrectAnswersByUser", int.class));
        assertNotNull(IAttemptDAO.class.getMethod("getCurrentStreakByUser", int.class));
        assertNotNull(IAttemptDAO.class.getMethod("getBestStreakByUser", int.class));
        assertNotNull(IAttemptDAO.class.getMethod("getStreakAsOf", int.class, LocalDate.class));
        assertNotNull(IAttemptDAO.class.getMethod("getAttemptsByDateRange", int.class, LocalDate.class, LocalDate.class));
    }

    @Test
    void testReturnTypes() throws NoSuchMethodException {
        assertEquals(void.class,
                IAttemptDAO.class.getMethod("addAttempt", Attempt.class).getReturnType());

        assertEquals(List.class,
                IAttemptDAO.class.getMethod("getAttemptsByUser", int.class).getReturnType());

        assertEquals(List.class,
                IAttemptDAO.class.getMethod("getAttemptsByQuiz", int.class).getReturnType());

        assertEquals(Attempt.class,
                IAttemptDAO.class.getMethod("getLastAttempt", int.class, int.class).getReturnType());

        assertEquals(int.class,
                IAttemptDAO.class.getMethod("getCorrectAnswersByUser", int.class).getReturnType());

        assertEquals(int.class,
                IAttemptDAO.class.getMethod("getCurrentStreakByUser", int.class).getReturnType());

        assertEquals(int.class,
                IAttemptDAO.class.getMethod("getBestStreakByUser", int.class).getReturnType());

        assertEquals(int.class,
                IAttemptDAO.class.getMethod("getStreakAsOf", int.class, LocalDate.class).getReturnType());

        assertEquals(Map.class,
                IAttemptDAO.class.getMethod("getAttemptsByDateRange", int.class, LocalDate.class, LocalDate.class).getReturnType());
    }

    @Test
    void testNoUnexpectedMethods() {
        Method[] methods = IAttemptDAO.class.getDeclaredMethods();
        assertEquals(9, methods.length, "IAttemptDAO should have exactly 9 methods defined.");
    }
}
