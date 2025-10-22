package com.app.studysnap.model;
import com.app.studysnap.exceptions.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.app.studysnap.services.TextParser.*;

/**
 * SQLite implementation of {@link IAttemptDAO}.
 * <p>
 * Creates and reads rows from the {@code QuizAttempts} table.
 * Timestamps are stored in a text column ({@code attempt_at}) and parsed as dates
 * using the first 10 chars ({@code yyyy-MM-dd}) for streak computations.
 * </p>
 * @see IAttemptDAO
 */
public class SqliteAttemptDAO implements IAttemptDAO {
    private final Connection connection;

    /**
     * Constructs a DAO using the shared {@link SqliteConnection} and ensures the schema exists.
     */
    public SqliteAttemptDAO() {
        this.connection = SqliteConnection.getInstance();
        createTable();
    }

    /**
     * Create {@code QuizAttempts} table if non-existing
     */
    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS QuizAttempts (
                attempt_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                quiz_id INTEGER NOT NULL,
                score TEXT,
                time_taken INTEGER,
                attempt_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (quiz_id) REFERENCES Quizzes(quiz_id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
            );
        """;

        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute(sql);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create QuizAttempts table.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addAttempt(Attempt attempt) {
        String sql = "INSERT INTO QuizAttempts(user_id, quiz_id, score, time_taken, attempt_at) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, attempt.getUserId());
            ps.setInt(2, attempt.getQuizId());
            ps.setString(3, attempt.getScore());
            ps.setInt(4, attempt.getTimeTaken());
            ps.setString(5, attempt.getAttemptAt());

            ps.executeUpdate();
        } catch (SQLException e){
            throw new DataAccessException("Failed to insert quiz attempt.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Attempt> getAttemptsByUser(int userId) {
        List<Attempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM QuizAttempts WHERE user_id = ? ORDER BY attempt_at DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
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
        } catch(SQLException e) {
            throw new DataAccessException("Failed to query attempts by user.", e);
        }
        return attempts;
    }

    /** {@inheritDoc} */
    @Override
    public List<Attempt> getAttemptsByQuiz(int quizId) {
        List<Attempt> attempts = new ArrayList<>();
        String sql = "SELECT * FROM QuizAttempts WHERE quiz_id = ? ORDER BY attempt_at DESC";

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
            throw new DataAccessException("Failed to query attempts by quiz.", e);
        }
        return attempts;
    }

    /** {@inheritDoc} */
    @Override
    public Attempt getLastAttempt(int userId, int quizId) {
        String sql = "SELECT * FROM QuizAttempts WHERE user_id = ? AND quiz_id = ? ORDER BY attempt_at DESC LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
        } catch (SQLException e){
            throw new DataAccessException("Failed to fetch last attempt for user/quiz.", e);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getCorrectAnswersByUser(int userId) {
        String sql = "SELECT score FROM QuizAttempts WHERE user_id = ?";
        int totalCorrect = 0;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String score = rs.getString("score");
                if (!isBlank(score) && score.contains("/")) {
                    try {
                        String[] parts = score.split("/");
                        int correct = Integer.parseInt(trim(parts[0]));
                        totalCorrect += correct;
                    } catch (NumberFormatException e) {
                        throw new DataAccessException("Invalid score format: " + score);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute total correct answers for user.", e);
        }

        return totalCorrect;
    }

    /** {@inheritDoc} */
    @Override
    public int getCurrentStreakByUser(int userId) {
        String sql = "SELECT attempt_at FROM QuizAttempts WHERE user_id = ? ORDER BY attempt_at DESC";
        List<LocalDate> days = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String ts = rs.getString("attempt_at");
                if (!isBlank(ts) && ts.length() >= 10) {
                    LocalDate date = LocalDate.parse(ts.substring(0, 10));
                    if (!days.contains(date)) days.add(date); // keep only one per day
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute current streak for user.", e);
        }

        if (days.isEmpty()) return 0;

        LocalDate today = LocalDate.now();

        // If played today, start from today; else start from yesterday
        LocalDate anchor = days.contains(today) ? today : today.minusDays(1);

        int streak = 0;
        while (days.contains(anchor)) {
            streak++;
            anchor = anchor.minusDays(1);
        }

        return streak;
    }

    /** {@inheritDoc} */
    @Override
    public int getBestStreakByUser(int userId) {
        String sql = "SELECT attempt_at FROM QuizAttempts WHERE user_id = ? ORDER BY attempt_at ASC";
        List<LocalDate> days = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String ts = rs.getString("attempt_at");
                if (!isBlank(ts) && ts.length() >= 10) {
                    LocalDate date = LocalDate.parse(ts.substring(0, 10));
                    if (!days.contains(date)) days.add(date); // remove duplicates manually
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute best streak for user.", e);
        }

        if (days.isEmpty()) return 0;

        int best = 1;
        int current = 1;

        for (int i = 1; i < days.size(); i++) {
            if (days.get(i).equals(days.get(i - 1).plusDays(1))) {
                current++;
                if (current > best) best = current;
            } else {
                current = 1;
            }
        }

        return best;
    }

    /** {@inheritDoc} */
    @Override
    public int getStreakAsOf(int userId, LocalDate asOfDate) {
        // collect unique attempt days
        List<LocalDate> days = new ArrayList<>();
        String sql = "SELECT attempt_at FROM QuizAttempts WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ts = rs.getString("attempt_at");
                if (!isBlank(ts) && ts.length() >= 10) {
                    days.add(LocalDate.parse(ts.substring(0, 10)));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to compute streak as of date for user.", e);
        }

        if (days.isEmpty()) return 0;

        // If played on start date, start there; else start from the previous day
        LocalDate anchor = days.contains(asOfDate) ? asOfDate : asOfDate.minusDays(1);

        int streak = 0;
        while (days.contains(anchor)) {
            streak++;
            anchor = anchor.minusDays(1);
        }
        return streak;
    }

    /** {@inheritDoc} */
    @Override
    public Map<LocalDate, Integer> getAttemptsByDateRange(int userId, LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> map = new HashMap<>();
        String sql = "SELECT attempt_at FROM QuizAttempts WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ts = rs.getString("attempt_at");
                if (!isBlank(ts) && ts.length() >= 10) {
                    LocalDate d = LocalDate.parse(ts.substring(0, 10));
                    if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) {
                        map.put(d, map.getOrDefault(d, 0) + 1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch attempts by date range for user.", e);
        }
        return map;
    }
}