package com.app.studysnap.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqliteQuizDAO implements IQuizDAO {

    private final Connection connection;
    private final SqliteQuestionDAO questionDAO;

    public SqliteQuizDAO() {
        connection = SqliteConnection.getInstance();
        questionDAO = new SqliteQuestionDAO();
        createTables();
    }

    private void createTables() {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("""
                CREATE TABLE IF NOT EXISTS Quizzes (
                    quiz_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    subject TEXT,
                    description TEXT,
                    is_private INTEGER NOT NULL DEFAULT 1,
                    created_by INTEGER NOT NULL,
                    FOREIGN KEY(created_by) REFERENCES Users(user_id)
                )
            """);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addQuiz(Quiz quiz) {
        final String insertQuiz = "INSERT INTO Quizzes(title,subject,description,is_private,created_by) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(insertQuiz, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getSubject());
            ps.setString(3, quiz.getDescription());
            ps.setInt(4, quiz.get_is_private() ? 1 : 0);
            ps.setInt(5, quiz.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("No quiz_id generated");
                int quizId = rs.getInt(1);
                quiz.setQuizId(quizId);

                // Delegate questions
                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    for (Question q : quiz.getQuestions()) q.setQuizId(quizId);
                    questionDAO.replaceForQuiz(quizId, quiz.getQuestions());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateQuiz(Quiz quiz) {
        final String updQuiz = "UPDATE Quizzes SET title=?, subject=?, description=?, is_private=?, created_by=? WHERE quiz_id=?";
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

        // Replace questions only if provided
        if (quiz.getQuestions() != null) {
            try {
                for (Question q : quiz.getQuestions()) q.setQuizId(quiz.getQuizId());
                questionDAO.replaceForQuiz(quiz.getQuizId(), quiz.getQuestions());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteQuiz(int quizId) {
        // ON DELETE CASCADE handles questions
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM Quizzes WHERE quiz_id=?")) {
            ps.setInt(1, quizId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Quiz> getAllQuizzes() {
        List<Quiz> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM Quizzes ORDER BY quiz_id DESC")) {
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

        // Delegate question fetching
        List<Question> questions = questionDAO.getQuestionsForQuiz(quizId);
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

    // Map all user quizzes by topic
    @Override
    public Map<String, Integer> getDeckCountsByTopic(int userId) {
        Map<String, Integer> out = new HashMap<>();
        String sql = "SELECT subject, COUNT(*) AS c " +
                "FROM Quizzes WHERE created_by = ? " +
                "GROUP BY subject " +
                "ORDER BY c DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String subject = rs.getString("subject");
                if (subject == null || subject.isBlank()) subject = "(No subject)";
                out.put(subject, rs.getInt("c"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public List<PublicListItem> findPublic(String query) {
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        final String sql = """
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

    //Reset the Quizzes table by deleting all the data in the table and reset the autoincrement at the same time
    public void resetQuizzesTable() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM Quizzes");

            //reset autoincrement
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='Quizzes'");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='QuizAttempts'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}