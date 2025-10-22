package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provider for a shared SQLite {@link Connection}.
 * <p>
 * Opens a JDBC connection to the {@code StudySnap.db} file and reuses it
 * for subsequent calls. Any failure to open or retrieve the connection results
 * in a {@link DataAccessException}.
 * </p>
 */
public class SqliteConnection {
    private static Connection instance = null;

    /**
     * Initializes by opening a JDBC connection to the local SQLite database.
     * @throws DataAccessException if the connection cannot be opened
     */
    private SqliteConnection() {
        String url = "jdbc:sqlite:StudySnap.db";
        try {
            instance = DriverManager.getConnection(url);
            if (instance == null) {
                throw new SQLException("DriverManager returned null for URL: " + url);
            }
        } catch (SQLException sqlEx) {
            throw new DataAccessException("Error opening SQLite connection to " + url, sqlEx);
        }
    }

    /**
     * Returns the shared SQLite connection, creating it on first use.
     * @return a live {@link Connection} to {@code StudySnap.db}
     * @throws DataAccessException if the connection could not be established
     */
    public static Connection getInstance() {
        if (instance == null) {
            new SqliteConnection();
            if (instance == null) {
                throw new DataAccessException("SQLite connection is null after initialization.");
            }
        }
        return instance;
    }
}