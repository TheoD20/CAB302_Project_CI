package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of {@link IBadgeProgressDAO}.
 * <p>
 * Uses a {@code BadgeProgress} table keyed by {@code (user_id, badge_id)} to track progress,
 * earned status, and goals.
 * </p>
 * @see IBadgeProgressDAO
 */
public class SqliteBadgeProgressDAO implements IBadgeProgressDAO{

    private final Connection connection;

    /**
     * Creates a DAO using the shared SQLite connection and ensures the schema exists.
     */
    public SqliteBadgeProgressDAO() {
        this.connection = SqliteConnection.getInstance();
        createTable();
    }

    /**
     * Creates the {@code BadgeProgress} table if it does not already exist.
     */
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
            throw new DataAccessException("Failed to create BadgeProgress table.", e);
        }
    }

    /** {@inheritDoc} */
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
            throw new DataAccessException("Failed to add badge progress (user " + userId + ", badge " + badgeId + ").", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    /** {@inheritDoc} */
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
            throw new DataAccessException("Failed to set badge progress (user " + userId + ", badge " + badgeId + ").", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    /** {@inheritDoc} */
    @Override
    public void resetProgress(int userId, int badgeId) {
        String sql = "UPDATE BadgeProgress SET progress=0, is_earned=0 WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reset badge progress (user " + userId + ", badge " + badgeId + ").", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Badge> getCompletedBadgesByUser(int userId) {
        List<Badge> list = new ArrayList<>();
        IBadgeDAO badgeDAO = new SqliteBadgeDAO();

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT badge_id FROM BadgeProgress WHERE user_id=? AND is_earned=1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(badgeDAO.getBadgeById(rs.getInt("badge_id")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch completed badges for user " + userId + ".", e);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEarned(int userId, int badgeId) {
        String sql = "SELECT is_earned FROM BadgeProgress WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to read is_earned (user " + userId + ", badge " + badgeId + ").", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getProgress(int userId, int badgeId) {
        String sql = "SELECT progress FROM BadgeProgress WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to read progress (user " + userId + ", badge " + badgeId + ").", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getGoal(int userId, int badgeId) {
        String sql = "SELECT progress_goal FROM BadgeProgress WHERE user_id=? AND badge_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, badgeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to read progress_goal (user " + userId + ", badge " + badgeId + ").", e);
        }
    }
}