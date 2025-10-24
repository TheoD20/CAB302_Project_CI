package com.app.studysnap.model;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SqliteAttemptDAOTest {

    private Connection conn;
    private SqliteAttemptDAO dao;

    @BeforeEach
    void setup() throws SQLException {

        conn = DriverManager.getConnection("jdbc:sqlite::memory:");


        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE QuizAttempts (
                    attempt_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    quiz_id INTEGER NOT NULL,
                    score TEXT,
                    time_taken INTEGER,
                    attempt_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }


        dao = new SqliteAttemptDAO() {
            {
                try {
                    java.lang.reflect.Field field = SqliteAttemptDAO.class.getDeclaredField("connection");
                    field.setAccessible(true);
                    field.set(this, conn);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @AfterEach
    void teardown() throws SQLException {
        conn.close();
    }

    @Test
    void testAddAndGetAttemptsByUser() {
        dao.addAttempt(new Attempt(1, 10, "3/5", 120, "2025-10-20 10:00:00"));
        dao.addAttempt(new Attempt(1, 11, "4/5", 100, "2025-10-21 12:00:00"));

        List<Attempt> results = dao.getAttemptsByUser(1);
        assertEquals(2, results.size());
        assertEquals("4/5", results.get(0).getScore());
    }

    @Test
    void testGetAttemptsByQuiz() {
        dao.addAttempt(new Attempt(1, 101, "5/5", 60, "2025-10-21 11:00:00"));
        dao.addAttempt(new Attempt(2, 101, "2/5", 80, "2025-10-21 12:00:00"));

        List<Attempt> results = dao.getAttemptsByQuiz(101);
        assertEquals(2, results.size());
        assertEquals("2/5", results.get(0).getScore());
    }

    @Test
    void testGetLastAttempt() {
        dao.addAttempt(new Attempt(1, 200, "3/5", 90, "2025-10-20 10:00:00"));
        dao.addAttempt(new Attempt(1, 200, "5/5", 100, "2025-10-21 10:00:00"));

        Attempt last = dao.getLastAttempt(1, 200);
        assertNotNull(last);
        assertEquals("5/5", last.getScore());
    }

    @Test
    void testGetCorrectAnswersByUser() {
        dao.addAttempt(new Attempt(1, 1, "3/5", 60, "2025-10-21 09:00:00"));
        dao.addAttempt(new Attempt(1, 2, "2/5", 70, "2025-10-21 10:00:00"));
        assertEquals(5, dao.getCorrectAnswersByUser(1));
    }

    @Test
    void testGetCurrentStreakByUser() {
        LocalDate today = LocalDate.now();
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, today.toString() + " 09:00:00"));
        dao.addAttempt(new Attempt(1, 2, "1/5", 10, today.minusDays(1).toString() + " 09:00:00"));
        dao.addAttempt(new Attempt(1, 3, "1/5", 10, today.minusDays(2).toString() + " 09:00:00"));

        int streak = dao.getCurrentStreakByUser(1);
        assertTrue(streak >= 2, "Streak should count consecutive days");
    }

    @Test
    void testGetBestStreakByUser() {
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, "2025-10-19 09:00:00"));
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, "2025-10-20 09:00:00"));
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, "2025-10-22 09:00:00"));
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, "2025-10-23 09:00:00"));

        assertEquals(2, dao.getBestStreakByUser(1));
    }

    @Test
    void testGetStreakAsOf() {
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, "2025-10-18 09:00:00"));
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, "2025-10-19 09:00:00"));
        dao.addAttempt(new Attempt(1, 1, "1/5", 10, "2025-10-20 09:00:00"));

        LocalDate check = LocalDate.parse("2025-10-20");
        assertEquals(3, dao.getStreakAsOf(1, check));
    }

    @Test
    void testGetAttemptsByDateRange() {
        dao.addAttempt(new Attempt(1, 1, "2/5", 10, "2025-10-18 09:00:00"));
        dao.addAttempt(new Attempt(1, 1, "2/5", 10, "2025-10-19 09:00:00"));
        dao.addAttempt(new Attempt(1, 1, "2/5", 10, "2025-10-21 09:00:00"));

        Map<LocalDate, Integer> map = dao.getAttemptsByDateRange(1,
                LocalDate.parse("2025-10-18"), LocalDate.parse("2025-10-20"));

        assertEquals(2, map.size());
        assertTrue(map.containsKey(LocalDate.parse("2025-10-18")));
        assertTrue(map.containsKey(LocalDate.parse("2025-10-19")));
    }
}
