package com.app.studysnap.model;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteQuestionDAOTest {

    SqliteQuestionDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        dao = new SqliteQuestionDAO();

        // Override private connection with an in-memory DB
        Connection inMemoryConn = DriverManager.getConnection("jdbc:sqlite::memory:");
        Field connField = SqliteQuestionDAO.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(dao, inMemoryConn);

        // Manually create the Questions table in memory
        inMemoryConn.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS Questions (
                question_id INTEGER PRIMARY KEY AUTOINCREMENT,
                quiz_id INTEGER NOT NULL,
                content TEXT NOT NULL,
                option1 TEXT,
                option2 TEXT,
                option3 TEXT,
                option4 TEXT,
                option5 TEXT,
                correct_option INTEGER
            )
        """);
    }

    @Test
    void classLoads() {
        assertNotNull(SqliteQuestionDAO.class);
    }

    @Test
    void constructor_Exists() throws Exception {
        assertNotNull(SqliteQuestionDAO.class.getDeclaredConstructor());
    }

    @Test
    void methodsExist() throws Exception {
        Class<?> clazz = SqliteQuestionDAO.class;
        assertNotNull(clazz.getDeclaredMethod("addQuestion", Question.class));
        assertNotNull(clazz.getDeclaredMethod("getQuestionById", int.class));
        assertNotNull(clazz.getDeclaredMethod("getQuestionsForQuiz", int.class));
        assertNotNull(clazz.getDeclaredMethod("updateQuestion", Question.class));
        assertNotNull(clazz.getDeclaredMethod("deleteQuestion", int.class));
        assertNotNull(clazz.getDeclaredMethod("replaceForQuiz", int.class, List.class));
    }

    @Test
    void addQuestion_DoesNotThrow() {
        Question q = new Question();
        q.setQuizId(1);
        q.setQuestion("Test Question");
        q.setOption1("A");
        q.setOption2("B");
        q.setCorrectOption(2);

        assertDoesNotThrow(() -> dao.addQuestion(q));
        assertTrue(q.getQuestionId() > 0);
    }

    @Test
    void getQuestionById_ReturnsInsertedQuestion() {
        Question q = new Question();
        q.setQuizId(1);
        q.setQuestion("Test Question");
        q.setOption1("A");
        q.setCorrectOption(1);
        dao.addQuestion(q);

        Question fetched = dao.getQuestionById(q.getQuestionId());
        assertNotNull(fetched);
        assertEquals("Test Question", fetched.getQuestion());
    }

    @Test
    void updateQuestion_DoesNotThrow() {
        Question q = new Question();
        q.setQuizId(1);
        q.setQuestion("Old Question");
        dao.addQuestion(q);

        q.setQuestion("Updated Question");
        assertDoesNotThrow(() -> dao.updateQuestion(q));

        Question updated = dao.getQuestionById(q.getQuestionId());
        assertEquals("Updated Question", updated.getQuestion());
    }

    @Test
    void deleteQuestion_DoesNotThrow() {
        Question q = new Question();
        q.setQuizId(1);
        q.setQuestion("Delete Me");
        dao.addQuestion(q);

        assertDoesNotThrow(() -> dao.deleteQuestion(q.getQuestionId()));
        assertNull(dao.getQuestionById(q.getQuestionId()));
    }

    @Test
    void replaceForQuiz_DoesNotThrow() {
        Question q1 = new Question();
        q1.setQuizId(1);
        q1.setQuestion("Q1");
        dao.addQuestion(q1);

        Question q2 = new Question();
        q2.setQuizId(1);
        q2.setQuestion("Q2");

        assertDoesNotThrow(() -> dao.replaceForQuiz(1, List.of(q2)));

        List<Question> questions = dao.getQuestionsForQuiz(1);
        assertEquals(1, questions.size());
        assertEquals("Q2", questions.get(0).getQuestion());
    }
}
