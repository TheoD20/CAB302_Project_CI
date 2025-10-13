package com.app.studysnap.model;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqliteAttemptDAO implements IAttemptDAO {
    private final Connection connection;
    public SqliteAttemptDAO() {
        this.connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS QuizAttempts (
                attempt_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER NOT NULL,
                quiz_id      INTEGER NOT NULL,
                score        TEXT,
                time_taken   INTEGER,
                attempt_at   TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (quiz_id) REFERENCES Quizzes(quiz_id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
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
        String sql = "INSERT INTO QuizAttempts(user_id, quiz_id, score, time_taken, attempt_at) VALUES (?, ?, ?, ?, ?)";

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
        String sql = "SELECT * FROM QuizAttempts WHERE user_id = ? ORDER BY attempt_at DESC";

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
            e.printStackTrace();
        }
        return attempts;
    }

    @Override
    public Attempt getLastAttempt(int userId, int quizId) {
        String sql = "SELECT * FROM QuizAttempts WHERE user_id = ? AND quiz_id = ? ORDER BY attempt_at DESC LIMIT 1";

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

    // Returns the amount of correct answers logged for a user (derives from score)
    @Override
    public int getCorrectAnswersByUser(int userId) {
        String sql = "SELECT score FROM QuizAttempts WHERE user_id = ?";
        int totalCorrect = 0;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String score = rs.getString("score");
                if (score != null && score.contains("/")) {
                    try {
                        String[] parts = score.split("/");
                        int correct = Integer.parseInt(parts[0].trim());
                        totalCorrect += correct;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format: " + score);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalCorrect;
    }

    // Return streak for user by analysing date entries and testing for consecutive days
    @Override
    public int getCurrentStreakByUser(int userId) {
        String sql = "SELECT attempt_at FROM QuizAttempts WHERE user_id = ? ORDER BY attempt_at DESC";
        List<LocalDate> days = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String ts = rs.getString("attempt_at");
                if (ts != null && ts.length() >= 10) {
                    LocalDate date = LocalDate.parse(ts.substring(0, 10));
                    if (!days.contains(date)) days.add(date); // keep only one per day
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (days.isEmpty()) return 0;

        LocalDate today = LocalDate.now();

        // If played today, start from today; else start from yesterday
        // they can still play today and maintain streak so it's not zero
        LocalDate anchor = days.contains(today) ? today : today.minusDays(1);

        int streak = 0;
        while (days.contains(anchor)) {
            streak++;
            anchor = anchor.minusDays(1);
        }

        return streak;
    }

    // Return best streak ever recorded for user
    @Override
    public int getBestStreakByUser(int userId) {
        String sql = "SELECT attempt_at FROM QuizAttempts WHERE user_id = ? ORDER BY attempt_at ASC";
        List<LocalDate> days = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String ts = rs.getString("attempt_at");
                if (ts != null && ts.length() >= 10) {
                    LocalDate date = LocalDate.parse(ts.substring(0, 10));
                    if (!days.contains(date)) days.add(date); // remove duplicates manually
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    // Get user streak on a specific date
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
                if (ts != null && ts.length() >= 10) {
                    days.add(LocalDate.parse(ts.substring(0, 10)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    // map all attempts for a user in each day throughout a specific date range
    @Override
    public Map<LocalDate, Integer> getAttemptsByDateRange(int userId, LocalDate start, LocalDate end) {
        Map<LocalDate, Integer> map = new HashMap<>();
        String sql = "SELECT attempt_at FROM QuizAttempts WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ts = rs.getString("attempt_at");
                if (ts != null && ts.length() >= 10) {
                    LocalDate d = LocalDate.parse(ts.substring(0, 10));
                    if ((d.isEqual(start) || d.isAfter(start)) && (d.isEqual(end) || d.isBefore(end))) {
                        map.put(d, map.getOrDefault(d, 0) + 1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }
}
