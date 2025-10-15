package com.app.studysnap.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteBadgeDAO implements IBadgeDAO {

    private final Connection connection;

    public SqliteBadgeDAO() {
        this.connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement statement = connection.createStatement();
            String query = "CREATE TABLE IF NOT EXISTS Badges ("
                    + "badge_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "description TEXT NOT NULL,"
                    + "icon_path TEXT NOT NULL UNIQUE,"
                    + "type TEXT NOT NULL,"
                    + "goal INTEGER NOT NULL"
                    + ")";
            statement.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBadge(Badge b) {
        final String insertBadge = """
            INSERT INTO Badges(name, description, icon_path, type, goal)
            VALUES (?, ?, ?, ?, ?)
        """;
        final String seedUserBadges = """
            INSERT OR IGNORE INTO BadgeProgress(user_id, badge_id, is_earned, progress, progress_goal)
            SELECT u.user_id, ?, 0, 0, ?
            FROM Users u
        """;

        try {
            connection.setAutoCommit(false);

            int newBadgeId;
            try (PreparedStatement ps = connection.prepareStatement(insertBadge, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, b.getBadgeName());
                ps.setString(2, b.getBadgeDescription());
                ps.setString(3, b.getBadgeIconPath());
                ps.setString(4, b.getBadgeType());
                ps.setInt(5, b.getBadgeGoal());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Failed to get badge_id");
                    newBadgeId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(seedUserBadges)) {
                ps.setInt(1, newBadgeId);
                ps.setInt(2, b.getBadgeGoal());
                ps.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e){
            try { connection.rollback(); } catch (SQLException ignore) {}
            e.printStackTrace();
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
        }
    }

    @Override
    public List<Badge> getAllBadges () {
        List<Badge> badges = new ArrayList<>();
        String sql = "SELECT * FROM Badges ORDER BY badge_id DESC";

        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                badges.add(new Badge(
                    rs.getInt("badge_id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("icon_path"),
                    rs.getString("type"),
                    rs.getInt("goal")
                ));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return badges;
    }

    @Override
    public Badge getBadgeById(int badge_id) {
        Badge badge = null;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM Badges WHERE badge_id=?")) {
            ps.setInt(1, badge_id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    badge = new Badge(
                        rs.getInt("badge_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("icon_path"),
                        rs.getString("type"),
                        rs.getInt("goal")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return badge;
    }

    @Override
    public List<Badge> getBadgesByType (String type) {
        List<Badge> badges = new ArrayList<>();
        String sql = "SELECT * FROM Badges WHERE type = ? ORDER BY badge_id DESC";

        try(PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                badges.add(new Badge(
                        rs.getInt("badge_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("icon_path"),
                        rs.getString("type"),
                        rs.getInt("goal")
                ));
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return badges;
    }

    @Override
    public void deleteBadge(int badgeId) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM Badges WHERE badge_id = ?"
            );
            statement.setInt(1, badgeId);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Seeds predefined badges
    @Override
    public void initializeBadges() {

        addBadge(new Badge("Flawless Five", "Score 100% on 5 quizzes", "src/main/resources/com/app/studysnap/images/badges/flawless3.png", "score", 5));

        addBadge(new Badge("Flawless Ten", "Score 100% on 10 quizzes", "src/main/resources/com/app/studysnap/images/badges/flawless2.png", "score", 10));

        addBadge(new Badge("Flawless Twenty", "Score 100% on 20 quizzes", "src/main/resources/com/app/studysnap/images/badges/flawless1.png", "score", 20));

        addBadge(new Badge("Big Brain", "Get 20 flashcards correct in a row without mistakes", "src/main/resources/com/app/studysnap/images/badges/bigbrain.png", "streak", 20));

        addBadge(new Badge("Speed Reader", "Complete a quiz in under 2 minutes with a score above 80%", "src/main/resources/com/app/studysnap/images/badges/speedreader.png", "speed", 1));

        addBadge(new Badge("Persistence Pays", "Retry the same quiz 3 or more times and achieve a passing score", "src/main/resources/com/app/studysnap/images/badges/persistencepays.png", "persistence", 3));

        addBadge(new Badge("Quiz Creator (Bronze)", "Create your first quiz", "src/main/resources/com/app/studysnap/images/badges/quizcreator3.png", "creation", 1));

        addBadge(new Badge("Quiz Creator (Silver)", "Create 5 quizzes", "src/main/resources/com/app/studysnap/images/badges/quizcreator2.png", "creation", 5));

        addBadge(new Badge("Quiz Creator (Gold)", "Create 10 quizzes", "src/main/resources/com/app/studysnap/images/badges/quizcreator1.png", "creation", 10));

        addBadge(new Badge("DecaGenius", "Complete 10 quizzes with at least 80% accuracy", "src/main/resources/com/app/studysnap/images/badges/genius.png", "score", 10));

        addBadge(new Badge("First Step", "Complete your first quiz or create your first deck", "src/main/resources/com/app/studysnap/images/badges/firststep.png", "progress", 1));

        addBadge(new Badge("Quick Learner", "Score 80% or higher on any quiz", "src/main/resources/com/app/studysnap/images/badges/quicklearner.png", "score", 1));

        addBadge(new Badge("Accuracy Hero", "Score 100% on a single quiz", "src/main/resources/com/app/studysnap/images/badges/accuracyhero.png", "score", 1));

        addBadge(new Badge("Comeback Kid", "Improve your score by 20% or more after retaking a quiz", "src/main/resources/com/app/studysnap/images/badges/comeback.png", "improvement", 1));

        addBadge(new Badge("Explorer", "Play quizzes from 5 different categories", "src/main/resources/com/app/studysnap/images/badges/explorer.png", "exploration", 5));

        addBadge(new Badge("Consistency Master", "Complete 10 quizzes in a row without skipping questions", "src/main/resources/com/app/studysnap/images/badges/consistencymaster.png", "streak", 10));

        addBadge(new Badge("Persistence", "Maintain a 7-day activity streak", "src/main/resources/com/app/studysnap/images/badges/persistence.png", "streak", 7));
    }
}