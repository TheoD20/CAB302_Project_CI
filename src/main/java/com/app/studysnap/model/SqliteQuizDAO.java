package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.app.studysnap.services.TextParser.isBlank;
import static com.app.studysnap.services.TextParser.trim;

/**
 * SQLite implementation of {@link IQuizDAO}.
 * <p>
 * Manages the {@code Quizzes} table (FK to {@code Users}) and delegates question
 * persistence to {@link SqliteQuestionDAO}.
 * </p>
 * @see IQuizDAO
 */
public class SqliteQuizDAO implements IQuizDAO {

    private final Connection connection;
    private final SqliteQuestionDAO questionDAO;

    /**
     * Creates a DAO bound to the shared SQLite connection and ensures the schema exists.
     */
    public SqliteQuizDAO() {
        try {
            connection = SqliteConnection.getInstance();
            questionDAO = new SqliteQuestionDAO();
            createTables();
        } catch (Exception e) {
            throw new DataAccessException("Failed to initialize SqliteQuizDAO.", e);
        }
    }

    /**
     * Creates the {@code Quizzes} table if it does not already exist.
     */
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
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create Quizzes table.", e);
        }
    }

    /** {@inheritDoc} */
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
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert quiz.", e);
        }
    }

    /** {@inheritDoc} */
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
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update quiz id " + quiz.getQuizId() + ".", e);
        }

        // Replace questions only if provided
        if (quiz.getQuestions() != null) {
            try {
                for (Question q : quiz.getQuestions()) q.setQuizId(quiz.getQuizId());
                questionDAO.replaceForQuiz(quiz.getQuizId(), quiz.getQuestions());
            } catch (Exception e) {
                throw new DataAccessException("Failed to replace questions for quiz " + quiz.getQuizId() + ".", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteQuiz(int quizId) {
        // ON DELETE CASCADE handles questions
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM Quizzes WHERE quiz_id=?")) {
            ps.setInt(1, quizId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete quiz id " + quizId + ".", e);
        }
    }

    /** {@inheritDoc} */
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
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch all quizzes.", e);
        }
        return list;
    }

    /** {@inheritDoc} */
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
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch quiz id " + quizId + ".", e);
        }
        if (quiz == null) return null;

        // Delegate question fetching
        List<Question> questions = questionDAO.getQuestionsForQuiz(quizId);
        quiz.setQuestions(questions);
        return quiz;
    }

    /** {@inheritDoc} */
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
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch quizzes for user " + userId + ".", e);
        }
        return list;
    }

    /** {@inheritDoc} */
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
                // rule #3: delegate blank check
                if (isBlank(subject)) subject = "(No subject)";
                out.put(subject, rs.getInt("c"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute deck counts by topic for user " + userId + ".", e);
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public List<PublicListItem> findPublic(String query) {
        String like = "%" + trim(query) + "%"; // rule #3: delegate trim/safe
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
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search public quizzes.", e);
        }
        return out;
    }

    /** {@inheritDoc} */
    @Override
    public void resetQuizzesTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM Quizzes");

            // reset autoincrement for related tables
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='Quizzes'");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='Questions'");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='QuizAttempts'");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reset Quizzes table.", e);
        }
    }
}