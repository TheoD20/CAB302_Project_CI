package com.app.studysnap.model;

import java.sql.*;

public class SqliteBadgeProgressDAO implements IBadgeProgressDAO{

    private final Connection connection;

    public SqliteBadgeProgressDAO() {
        this.connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS BadgeProgress (
                user_id INTEGER NOT NULL,
                badge_id INTEGER NOT NULL,
                is_earned INTEGER NOT NULL DEFAULT 0,
                progress INTEGER NOT NULL DEFAULT 0,
                progress_goal INTEGER NOT NULL,
                PRIMARY KEY (user_id, badge_id),
                FOREIGN KEY (user_id)  REFERENCES Users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (badge_id) REFERENCES Badges(badge_id) ON DELETE CASCADE
            );
        """;

        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Increments badge progress by a specified value (positive or negative) and update is_earned
    @Override
    public void addProgress(int userId, int badgeId, int value) {
        String inc = "UPDATE BadgeProgress SET progress = MAX(0, progress + ?) WHERE user_id=? AND badge_id=?";
        String award = """
            UPDATE BadgeProgress
            SET is_earned = 1
            WHERE user_id=? AND badge_id=?
            AND progress >= progress_goal
        """;
        String unaward = """
            UPDATE BadgeProgress
            SET is_earned = 0
            WHERE user_id=? AND badge_id=?
            AND progress < progress_goal
        """;
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(inc)) {
                ps.setInt(1, value);
                ps.setInt(2, userId);
                ps.setInt(3, badgeId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(unaward)) {
                ps.setInt(1, userId);
                ps.setInt(2, badgeId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(award)) {
                ps.setInt(1, userId);
                ps.setInt(2, badgeId);
                ps.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignore) {}
            throw new RuntimeException(e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    // Sets the badge progress to an explicit value and update is_earned
    @Override
    public void setProgress(int userId, int badgeId, int value) {
        String set = "UPDATE BadgeProgress SET progress = MAX(0, ?) WHERE user_id=? AND badge_id=?";
        String award = """
            UPDATE BadgeProgress
            SET is_earned = 1
            WHERE user_id=? AND badge_id=?
            AND progress >= progress_goal
        """;
        String unaward = """
            UPDATE BadgeProgress
            SET is_earned = 0
            WHERE user_id=? AND badge_id=?
            AND progress < progress_goal
        """;
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(set)) {
                ps.setInt(1, value);
                ps.setInt(2, userId);
                ps.setInt(3, badgeId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(unaward)) {
                ps.setInt(1, userId);
                ps.setInt(2, badgeId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(award)) {
                ps.setInt(1, userId);
                ps.setInt(2, badgeId);
                ps.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignore) {}
            throw new RuntimeException(e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    // Resets progress for a specific badge
    @Override
    public void resetProgress(int userId, int badgeId) {
        String sql = "UPDATE BadgeProgress SET progress=0, is_earned=0 WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // Returns true if the badge has been earned by the user
    @Override
    public boolean isEarned(int userId, int badgeId) {
        String sql = "SELECT is_earned FROM BadgeProgress WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // Returns the current progress value for a specific user and badge
    @Override
    public int getProgress(int userId, int badgeId) {
        String sql = "SELECT progress FROM BadgeProgress WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    // Returns the progress goal for a badge, or null if not defined
    @Override
    public int getGoal(int userId, int badgeId) {
        String sql = "SELECT progress_goal FROM BadgeProgress WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
