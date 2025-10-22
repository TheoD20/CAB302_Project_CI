package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.app.studysnap.services.TextParser.*;

/**
 * SQLite implementation of {@link IUserDAO}.
 * <p>
 * Manages the {@code Users} table and provides lookups by id, username, email, and Google sub.
 * </p>
 * @see IUserDAO
 */
public class SqliteUserDAO implements IUserDAO {
    private final Connection connection;

    /**
     * Creates a DAO using the shared SQLite connection and ensures the schema exists.
     */
    public SqliteUserDAO() {
        try {
            connection = SqliteConnection.getInstance();
            createTable();
        } catch (Exception e) {
            throw new DataAccessException("Failed to initialize SqliteUserDAO.", e);
        }
    }

    /**
     * Creates the {@code Users} table if it does not already exist.
     */
    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            String query = "CREATE TABLE IF NOT EXISTS Users ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL,"
                + "email TEXT NOT NULL UNIQUE,"
                + "password TEXT,"
                + "auth_provider TEXT NOT NULL DEFAULT 'LOCAL',"
                + "google_sub TEXT UNIQUE"
                + ")";
            statement.execute(query);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create Users table.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int addUser(User user) {
        String provider = isBlank(user.getAuthProvider()) ? "LOCAL" : user.getAuthProvider();

        String insertUser = "INSERT INTO Users(username, email, password, auth_provider, google_sub) VALUES (?,?,?,?,?)";
        final String seedUserBadges = """
            INSERT OR IGNORE INTO BadgeProgress(user_id, badge_id, is_earned, progress, progress_goal)
            SELECT ?, b.badge_id, 0, 0, b.goal
            FROM Badges b
        """;

        boolean oldAuto;
        try {
            oldAuto = connection.getAutoCommit();
            connection.setAutoCommit(false);

            int newUserId;
            try (PreparedStatement ps = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, trim(user.getUsername()));
                ps.setString(2, trim(user.getEmail()).toLowerCase());
                ps.setString(3, user.getPassword());
                ps.setString(4, provider);
                ps.setString(5, user.getGoogleSub());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Failed to get user_id");
                    newUserId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(seedUserBadges)) {
                ps.setInt(1, newUserId);
                ps.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(oldAuto);
            return newUserId;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignore) {}
            throw new DataAccessException("Failed to insert user.", e);
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    /** {@inheritDoc} */
    @Override
    public int addGoogleUser(String username, String email, String googleSub) {
        return addUser(new User(username, email, "google_oauth", "GOOGLE", googleSub));
    }

    /** {@inheritDoc} */
    @Override
    public void updateUser(User user) {
        String sql = "UPDATE Users SET username=?, email=?, password=?, auth_provider=?, google_sub=? WHERE user_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trim(user.getUsername()));
            statement.setString(2, trim(user.getEmail()).toLowerCase());
            statement.setString(3, user.getPassword());
            statement.setString(4, isBlank(user.getAuthProvider()) ? "LOCAL" : user.getAuthProvider());
            statement.setString(5, user.getGoogleSub());
            statement.setInt(6, user.getUserId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update user id " + user.getUserId() + ".", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deleteUser(int userId) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM Users WHERE user_id = ?"
            );
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user id " + userId + ".", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM Users");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch all users.", e);
        }
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public User getUserById(int userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM Users WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch user id " + userId + ".", e);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public User getUserByUsername(String username) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM Users WHERE username=?")) {
            ps.setString(1, trim(username));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch by username.", e);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public User getUserByEmail(String email) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM Users WHERE email=?")) {
            ps.setString(1, trim(email).toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch by email.", e);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean usernameExists(String username) {
        return getUserByUsername(username) != null;
    }

    /** {@inheritDoc} */
    @Override
    public User getUserByGoogleSub(String googleSub) {
        if (isBlank(googleSub)) return null;
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM Users WHERE google_sub=?")) {
            ps.setString(1, trim(googleSub));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch by Google sub.", e);
        }
        return null;
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link User}.
     * @param rs result set positioned at a row
     * @return populated user
     * @throws SQLException if column access fails
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setAuthProvider(rs.getString("auth_provider"));
        u.setGoogleSub(rs.getString("google_sub"));
        return u;
    }

    // Debug/Testing helpers

    /** Seeds three mock users (LOCAL) with hashed passwords. */
    public void seedMockUsers() {
        addUser(new User("test1", "email1@gmail.com", BCrypt.hashpw("password1", BCrypt.gensalt()), "LOCAL", null));
        addUser(new User("test2", "email2@gmail.com", BCrypt.hashpw("password2", BCrypt.gensalt()), "LOCAL", null));
        addUser(new User("test3", "email3@gmail.com", BCrypt.hashpw("password3", BCrypt.gensalt()), "LOCAL", null));
    }

    /** Clears {@code Users} table and resets related sequences. Destructive; intended for tests. */
    public void resetUsersTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM Users");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='Users'");
            statement.executeUpdate("DELETE FROM sqlite_sequence WHERE name='QuizAttempts'");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reset Users table.", e);
        }
    }
}