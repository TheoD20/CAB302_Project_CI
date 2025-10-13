package com.app.studysnap.model;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class SqliteAttemptDAO implements IAttemptDAO {
    private final Connection connection;
    public SqliteAttemptDAO() {
        this.connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS quiz_attempts (
                attempt_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER NOT NULL,
                quiz_id      INTEGER NOT NULL,
                score        TEXT,
                time_taken   INTEGER,
                attempt_at   TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """;

        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addAttempt(Attempt attempt) {
        String sql = "INSERT INTO quiz_attempts(user_id, quiz_id, score, time_taken, attempt_at) VALUES (?, ?, ?, ?, ?)";

        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, attempt.getUserId());
            ps.setInt(2, attempt.getQuizId());
            ps.setString(3, attempt.getScore());
            ps.setInt(4, attempt.getTimeTaken());
            ps.setString(5, attempt.getAttemptAt());

            ps.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public List<Attempt> getAttemptsByUser(int userId) {
        List<Attempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM quiz_attempts WHERE user_id = ? ORDER BY attempt_at DESC";

        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                attempts.add(new Attempt(
                        rs.getInt("attempt_id"),
                        rs.getInt("user_id"),
                        rs.getString("score"),
                        rs.getInt("time_taken"),
                        rs.getString("attempt_at")
                ));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return attempts;
    }

    @Override
    public List<Attempt> getAttemptsByQuiz(int quizId) {
        List<Attempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM quiz_attempts WHERE quiz_id = ? ORDER BY attempt_at DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                attempts.add(new Attempt(
                        rs.getInt("attempt_id"),
                        rs.getInt("user_id"),
                        rs.getInt("quiz_id"),
                        rs.getString("score"),
                        rs.getInt("time_taken"),
                        rs.getString("attempt_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attempts;
    }

    @Override
    public Attempt getLastAttempt(int userId, int quizId) {
        String sql = "SELECT * FROM quiz_attempts WHERE user_id = ? AND quiz_id = ? ORDER BY attempt_at DESC LIMIT 1";

        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()){
                return new Attempt(
                        rs.getInt("attempt_id"),
                        rs.getInt("user_id"),
                        rs.getInt("quiz_id"),
                        rs.getString("score"),
                        rs.getInt("time_taken"),
                        rs.getString("attempt_at")
                );
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteAttemptsByQuiz(int quizId) {

    }
}
