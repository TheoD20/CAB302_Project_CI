package com.app.studysnap.model;

import java.util.List;

public interface IBadgeProgressDAO {
    void addProgress(int userId, int badgeId, int value);
    void setProgress(int userId, int badgeId, int value);
    void resetProgress(int userId, int badgeId);
    boolean isEarned(int userId, int badgeId);
    int getProgress(int userId, int badgeId);
    int getGoal(int userId, int badgeId);
    List<Badge> getCompletedBadgesByUser(int userId);
}
