package com.app.studysnap.model;

public class BadgeProgress {
    private int userId;
    private int badgeId;
    private boolean isEarned;
    private int progress;
    private int progressGoal;

    public BadgeProgress(int user_Id, int badge_Id, int prog, int prog_Goal) {
        userId = user_Id;
        badgeId = badge_Id;
        isEarned = prog == prog_Goal;
        progress = prog;
        progressGoal = prog_Goal;
    }

    public BadgeProgress(int user_Id, int badge_Id, int prog_Goal) {
        userId = user_Id;
        badgeId = badge_Id;
        isEarned = false;
        progress = 0;
        progressGoal = prog_Goal;
    }

    // Getters
    public int getBadgeId() {
        return badgeId;
    }
    public int getUserId() {
        return userId;
    }
    public int getProgress() {
        return progress;
    }
    public int getProgressGoal() {
        return progressGoal;
    }
    public boolean getIsEarned() {
        return isEarned;
    }
}