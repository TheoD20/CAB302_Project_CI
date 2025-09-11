package com.app.studysnap.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteQuizDAO implements IQuizDAO
{
    private final Connection connection;

    public SqliteQuizDAO() {
        connection = SqliteConnection.getInstance();
        createTables();
    }

    private void createTables() {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");

            st.execute("""
                CREATE TABLE IF NOT EXISTS Quizzes (
                    quiz_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                    title       TEXT NOT NULL,
                    subject     TEXT,
                    description TEXT,
                    is_private  INTEGER NOT NULL DEFAULT 1,
                    created_by  INTEGER NOT NULL,
                    FOREIGN KEY(created_by) REFERENCES Users(user_id)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Questions (
                    question_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    quiz_id INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    option1 TEXT,
                    option2 TEXT,
                    option3 TEXT,
                    option4 TEXT,
                    option5 TEXT,
                    correct_option INTEGER,
                    FOREIGN KEY(quiz_id) REFERENCES Quizzes(quiz_id) ON DELETE CASCADE
                )
            """);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addQuiz(Quiz quiz) {
        String insertQuiz = "INSERT INTO Quizzes(title,subject,description,is_private,created_by) VALUES(?,?,?,?,?)";
        String insertQ    = "INSERT INTO Questions(quiz_id,content,option1,option2,option3,option4,option5,correct_option) VALUES(?,?,?,?,?,?,?,?)";
        try {
            connection.setAutoCommit(false);

            int quizId;
            try (PreparedStatement ps = connection.prepareStatement(insertQuiz, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, quiz.getTitle());
                ps.setString(2, quiz.getSubject());
                ps.setString(3, quiz.getDescription());
                ps.setInt(4, quiz.get_is_private() ? 1 : 0);
                ps.setInt(5, quiz.getCreatedBy());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("No quiz_id generated");
                    quizId = rs.getInt(1);
                }
            }

            if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                try (PreparedStatement psQ = connection.prepareStatement(insertQ)) {
                    for (Question q : quiz.getQuestions()) {
                        psQ.setInt(1, quizId);
                        psQ.setString(2, q.getQuestion());
                        psQ.setString(3, q.getOption1());
                        psQ.setString(4, q.getOption2());
                        psQ.setString(5, q.getOption3());
                        psQ.setString(6, q.getOption4());
                        psQ.setString(7, q.getOption5());
                        if (q.getCorrectOption() == null) psQ.setNull(8, java.sql.Types.INTEGER);
                        else psQ.setInt(8, q.getCorrectOption());
                        psQ.addBatch();
                    }
                    psQ.executeBatch();
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
            try { connection.rollback(); } catch (Exception ignored) {}
            try { connection.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }

    @Override
    public void updateQuiz(Quiz quiz) {
        String updQuiz = "UPDATE Quizzes SET title=?, subject=?, description=?, is_private=?, created_by=? WHERE quiz_id=?";
        try (PreparedStatement ps = connection.prepareStatement(updQuiz)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getSubject());
            ps.setString(3, quiz.getDescription());
            ps.setInt(4, quiz.get_is_private() ? 1 : 0);
            ps.setInt(5, quiz.getCreatedBy());
            ps.setInt(6, quiz.getQuizId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Replace questions if a list was provided
        if (quiz.getQuestions() != null) {
            String delQs = "DELETE FROM Questions WHERE quiz_id=?";
            String insQ  = "INSERT INTO Questions(quiz_id,content,option1,option2,option3,option4,option5,correct_option) VALUES(?,?,?,?,?,?,?,?)";
            try {
                connection.setAutoCommit(false);
                try (PreparedStatement del = connection.prepareStatement(delQs)) {
                    del.setInt(1, quiz.getQuizId());
                    del.executeUpdate();
                }
                try (PreparedStatement ins = connection.prepareStatement(insQ)) {
                    for (Question q : quiz.getQuestions()) {
                        ins.setInt(1, quiz.getQuizId());
                        ins.setString(2, q.getQuestion());
                        ins.setString(3, q.getOption1());
                        ins.setString(4, q.getOption2());
                        ins.setString(5, q.getOption3());
                        ins.setString(6, q.getOption4());
                        ins.setString(7, q.getOption5());
                        if (q.getCorrectOption() == null) ins.setNull(8, java.sql.Types.INTEGER);
                        else ins.setInt(8, q.getCorrectOption());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
                try { connection.rollback(); } catch (Exception ignored) {}
            } finally {
                try { connection.setAutoCommit(true); } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void deleteQuiz(int quizId) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM Quizzes WHERE quiz_id=?")) {
            ps.setInt(1, quizId);
            ps.executeUpdate(); // cascades to Questions
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Quiz getQuizById(int quizId) {
        Quiz quiz = null;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT quiz_id,title,subject,description,is_private,created_by FROM Quizzes WHERE quiz_id=?")) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    quiz = new Quiz(
                            rs.getInt("quiz_id"),
                            rs.getString("title"),
                            rs.getString("subject"),
                            rs.getString("description"),
                            rs.getInt("is_private") == 1,
                            rs.getInt("created_by")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (quiz == null) return null;

        // Load questions
        List<Question> questions = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT question_id,content,option1,option2,option3,option4,option5,correct_option FROM Questions WHERE quiz_id=? ORDER BY question_id")) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionId(rs.getInt("question_id"));
                    q.setQuizId(quizId);
                    q.setQuestion(rs.getString("content"));
                    q.setOption1(rs.getString("option1"));
                    q.setOption2(rs.getString("option2"));
                    q.setOption3(rs.getString("option3"));
                    q.setOption4(rs.getString("option4"));
                    q.setOption5(rs.getString("option5"));
                    int val = rs.getInt("correct_option");
                    if (rs.wasNull()) q.setCorrectOption(null);
                    else q.setCorrectOption(val);
                    questions.add(q);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        quiz.setQuestions(questions);
        return quiz;
    }

    @Override
    public List<Quiz> getQuizzesByUser(int userId) {
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT quiz_id,title,subject,description,is_private,created_by FROM Quizzes WHERE created_by=? ORDER BY quiz_id DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Quiz(
                            rs.getInt("quiz_id"),
                            rs.getString("title"),
                            rs.getString("subject"),
                            rs.getString("description"),
                            rs.getInt("is_private") == 1,
                            rs.getInt("created_by")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<PublicListItem> findPublic(String query) {
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        String sql = """
            SELECT q.quiz_id, q.title, q.subject, q.description, IFNULL(u.username,'') AS author
            FROM Quizzes q
            LEFT JOIN Users u ON u.user_id = q.created_by
            WHERE q.is_private = 0
              AND (q.title LIKE ? OR q.subject LIKE ? OR q.description LIKE ? OR u.username LIKE ?)
            ORDER BY q.quiz_id DESC
            LIMIT 200
        """;
        List<PublicListItem> out = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new PublicListItem(
                            rs.getInt("quiz_id"),
                            rs.getString("title"),
                            rs.getString("subject"),
                            rs.getString("description"),
                            rs.getString("author")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return out;
    }
}
