package com.app.studysnap.model;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteQuizDAOTest {

    SqliteQuizDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        // Create in-memory SQLite DB
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");

        // Minimal Users table for foreign key
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE Users(user_id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT)");
        }

        // Override private connection field in DAO
        dao = new SqliteQuizDAO();
        Field connField = SqliteQuizDAO.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(dao, conn);

        // Override SqliteQuestionDAO's connection and create Questions table
        Field questionDAOField = SqliteQuizDAO.class.getDeclaredField("questionDAO");
        questionDAOField.setAccessible(true);
        questionDAOField.set(dao, new SqliteQuestionDAO() {
            {
                Field f = SqliteQuestionDAO.class.getDeclaredField("connection");
                f.setAccessible(true);
                f.set(this, conn);

                try (Statement st = conn.createStatement()) {
                    st.execute("""
                        CREATE TABLE IF NOT EXISTS Questions (
                            question_id INTEGER PRIMARY KEY AUTOINCREMENT,
                            quiz_id INTEGER NOT NULL,
                            content TEXT NOT NULL,
                            option1 TEXT,
                            option2 TEXT,
                            correct_option INTEGER
                        )
                    """);
                }
            }
        });
    }

    @Test
    void classLoads() {
        assertNotNull(SqliteQuizDAO.class);
    }

    @Test
    void constructor_Exists() throws Exception {
        assertNotNull(SqliteQuizDAO.class.getDeclaredConstructor());
    }

    @Test
    void methodsExist() throws Exception {
        Class<?> clazz = SqliteQuizDAO.class;
        assertNotNull(clazz.getDeclaredMethod("addQuiz", Quiz.class));
        assertNotNull(clazz.getDeclaredMethod("updateQuiz", Quiz.class));
        assertNotNull(clazz.getDeclaredMethod("deleteQuiz", int.class));
        assertNotNull(clazz.getDeclaredMethod("getAllQuizzes"));
        assertNotNull(clazz.getDeclaredMethod("getQuizById", int.class));
        assertNotNull(clazz.getDeclaredMethod("getQuizzesByUser", int.class));
        assertNotNull(clazz.getDeclaredMethod("findPublic", String.class));
    }

    @Test
    void deleteQuiz_DoesNotThrow() {
        Quiz quiz = new Quiz(0, "ToDelete", "Subj", "Desc", true, 1);
        dao.addQuiz(quiz);

        assertDoesNotThrow(() -> dao.deleteQuiz(quiz.getQuizId()));
        assertNull(dao.getQuizById(quiz.getQuizId()));
    }

    @Test
    void getAllQuizzes_DoesNotThrow() {
        assertDoesNotThrow(() -> dao.getAllQuizzes());
    }

    @Test
    void getQuizzesByUser_DoesNotThrow() {
        assertDoesNotThrow(() -> dao.getQuizzesByUser(1));
    }

    @Test
    void findPublic_DoesNotThrow() {
        assertDoesNotThrow(() -> dao.findPublic("Test"));
    }
}
