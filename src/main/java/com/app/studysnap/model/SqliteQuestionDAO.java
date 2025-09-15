package com.app.studysnap.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteQuestionDAO implements IQuestionDAO {

    private final Connection connection;

    public SqliteQuestionDAO() {
        this.connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
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
    public void addQuestion(Question q) {
        final String sql = """
            INSERT INTO Questions(quiz_id, content, option1, option2, option3, option4, option5, correct_option)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getQuizId());
            ps.setString(2, q.getQuestion());
            ps.setString(3, q.getOption1());
            ps.setString(4, q.getOption2());
            ps.setString(5, q.getOption3());
            ps.setString(6, q.getOption4());
            ps.setString(7, q.getOption5());
            if (q.getCorrectOption() == null) ps.setNull(8, Types.INTEGER); else ps.setInt(8, q.getCorrectOption());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) q.setQuestionId(rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Question getQuestionById(int questionId) {
        final String sql = """
            SELECT *
            FROM Questions WHERE question_id=?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Question> getQuestionsForQuiz(int quizId) {
        final String sql = """
            SELECT question_id, quiz_id, content, option1, option2, option3, option4, option5, correct_option
            FROM Questions WHERE quiz_id=? ORDER BY question_id
        """;
        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void updateQuestion(Question q) {
        final String sql = """
            UPDATE Questions
               SET content=?, option1=?, option2=?, option3=?, option4=?, option5=?, correct_option=?
             WHERE question_id=?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, q.getQuestion());
            ps.setString(2, q.getOption1());
            ps.setString(3, q.getOption2());
            ps.setString(4, q.getOption3());
            ps.setString(5, q.getOption4());
            ps.setString(6, q.getOption5());
            if (q.getCorrectOption() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, q.getCorrectOption());
            ps.setInt(8, q.getQuestionId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteQuestion(int questionId) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM Questions WHERE question_id=?")) {
            ps.setInt(1, questionId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replaceForQuiz(int quizId, List<Question> questions) throws SQLException {
        final String del = "DELETE FROM Questions WHERE quiz_id=?";
        final String ins = """
            INSERT INTO Questions(quiz_id, content, option1, option2, option3, option4, option5, correct_option)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        boolean oldAuto = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement d = connection.prepareStatement(del);
             PreparedStatement i = connection.prepareStatement(ins)) {

            d.setInt(1, quizId);
            d.executeUpdate();

            if (questions != null) {
                for (Question q : questions) {
                    i.setInt(1, quizId);
                    i.setString(2, q.getQuestion());
                    i.setString(3, q.getOption1());
                    i.setString(4, q.getOption2());
                    i.setString(5, q.getOption3());
                    i.setString(6, q.getOption4());
                    i.setString(7, q.getOption5());
                    if (q.getCorrectOption() == null) i.setNull(8, Types.INTEGER); else i.setInt(8, q.getCorrectOption());
                    i.addBatch();
                }
                i.executeBatch();
            }

            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }

    private Question map(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("question_id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setQuestion(rs.getString("content"));
        q.setOption1(rs.getString("option1"));
        q.setOption2(rs.getString("option2"));
        q.setOption3(rs.getString("option3"));
        q.setOption4(rs.getString("option4"));
        q.setOption5(rs.getString("option5"));
        int co = rs.getInt("correct_option");
        q.setCorrectOption(rs.wasNull() ? null : co);
        return q;
    }

}