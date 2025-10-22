package com.app.studysnap.model;

/**
 * A model class representing a user's progress toward earning a specific badge.
 * <p>
 * Tracks the user, the badge, current progress, the goal required to earn it, and whether
 * the badge has already been earned. A badge is considered earned when
 * {@code progress == progressGoal}.
 * </p>
 */
public class BadgeProgress {
    private int userId;
    private int badgeId;
    private boolean isEarned;
    private int progress;
    private int progressGoal;

    /**
     * Constructs a new {@code BadgeProgress} with an explicit progress value.
     * The {@code isEarned} flag will be computed as {@code (prog == prog_Goal)}.
     * @param user_Id The ID of the user
     * @param badge_Id The ID of the badge
     * @param prog The current progress value
     * @param prog_Goal The goal required to earn the badge
     */
    public BadgeProgress(int user_Id, int badge_Id, int prog, int prog_Goal) {
        userId = user_Id;
        badgeId = badge_Id;
        isEarned = prog == prog_Goal;
        progress = prog;
        progressGoal = prog_Goal;
    }

    /**
     * Constructs a new {@code BadgeProgress} starting at zero progress.
     * @param user_Id The ID of the user
     * @param badge_Id The ID of the badge
     * @param prog_Goal The goal required to earn the badge
     */
    public BadgeProgress(int user_Id, int badge_Id, int prog_Goal) {
        userId = user_Id;
        badgeId = badge_Id;
        isEarned = false;
        progress = 0;
        progressGoal = prog_Goal;
    }

    // Getters

    /**
     * @return The ID of the badge being tracked
     */
    public int getBadgeId() {
        return badgeId;
    }

    /**
     * @return The ID of the user whose progress is tracked
     */
    public int getUserId() {
        return userId;
    }

    /**
     * @return The current progress value toward the badge goal
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @return The required goal value to earn the badge
     */
    public int getProgressGoal() {
        return progressGoal;
    }

    /**
     * @return {@code true} if the badge is earned; otherwise {@code false}
     */
    public boolean getIsEarned() {
        return isEarned;
    }
}