package com.app.studysnap.model;
import java.sql.*;
import java.util.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class SqliteQuestionProgressDAO implements IQuestionProgressDAO{

    private final Connection connection;

    public SqliteQuestionProgressDAO() {
        this.connection = SqliteConnection.getInstance();
//        createTables();
    }

    // Use this to add or update the date when the question is last seen. (keep track of when the users last attempted date).
    @Override
    public void addOrUpdateProgress(QuestionProgress progress) {
        String sql = """
            INSERT INTO QuestionProgress (user_id, question_id, last_attempted)
            VALUES (?, ?, ?)
            ON CONFLICT(user_id, question_id)
            DO UPDATE SET last_attempted = excluded.last_attempted
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, progress.getUserId());
            ps.setInt(2, progress.getQuestionId());
            ps.setString(3, progress.getLastAttempted());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get the last attempted date of the user for each question
    @Override
    public QuestionProgress getProgress(int userId, int questionId) {
        String sql = "SELECT * FROM QuestionProgress WHERE user_id = ? AND question_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, questionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new QuestionProgress(
                        rs.getInt("user_id"),
                        rs.getInt("question_id"),
                        rs.getString("last_attempted")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<QuestionProgress> getAllProgressForUser(int userId) {
        return List.of();
    }
}
