package com.app.studysnap.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttemptTest {

    @Test
    void testConstructorWithoutAttemptId() {
        Attempt attempt = new Attempt(1, 2, "5/10", 120, "2025-10-23 12:00:00");

        assertEquals(1, attempt.getUserId());
        assertEquals(2, attempt.getQuizId());
        assertEquals("5/10", attempt.getScore());
        assertEquals(120, attempt.getTimeTaken());
        assertEquals("2025-10-23 12:00:00", attempt.getAttemptAt());
        assertEquals(0, attempt.getAttemptId(), "Default attemptId should be 0 before being set");
    }

    @Test
    void testConstructorWithAttemptId() {
        Attempt attempt = new Attempt(100, 1, 2, "10/10", 300, "2025-10-23 15:00:00");

        assertEquals(100, attempt.getAttemptId());
        assertEquals(1, attempt.getUserId());
        assertEquals(2, attempt.getQuizId());
        assertEquals("10/10", attempt.getScore());
        assertEquals(300, attempt.getTimeTaken());
        assertEquals("2025-10-23 15:00:00", attempt.getAttemptAt());
    }

    @Test
    void testSettersAndGetters() {
        Attempt attempt = new Attempt(0, 0, 0, "", 0, "");

        attempt.setAttemptId(5);
        attempt.setUserId(10);
        attempt.setQuizId(15);
        attempt.setScore("8/10");
        attempt.setTimeTaken(180);
        attempt.setAttemptAt("2025-10-23 13:30:00");

        assertEquals(5, attempt.getAttemptId());
        assertEquals(10, attempt.getUserId());
        assertEquals(15, attempt.getQuizId());
        assertEquals("8/10", attempt.getScore());
        assertEquals(180, attempt.getTimeTaken());
        assertEquals("2025-10-23 13:30:00", attempt.getAttemptAt());
    }

    @Test
    void testScoreFormatIsString() {
        Attempt attempt = new Attempt(1, 2, "7/10", 100, "2025-10-23 14:00:00");
        assertTrue(attempt.getScore().contains("/"), "Score format should include '/'");
    }
}
