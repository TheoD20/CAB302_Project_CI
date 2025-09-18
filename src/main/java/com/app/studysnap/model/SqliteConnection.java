package com.app.studysnap.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnection {
    private static Connection instance = null;

    private SqliteConnection() {
        String url = "jdbc:sqlite:StudySnap.db";
        try {
            instance = DriverManager.getConnection(url);
            if (instance == null) {
                throw new SQLException("DriverManager returned null for URL: " + url);
            }
        } catch (SQLException sqlEx) {
            throw new RuntimeException("Error opening SQLite connection to " + url, sqlEx);
        }
    }

    public static Connection getInstance() {
        if (instance == null) {
            new SqliteConnection();
            if (instance == null) {
                throw new IllegalStateException("SQLite connection is null.");
            }
        }
        return instance;
    }
}
