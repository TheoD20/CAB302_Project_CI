package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.util.List;

/**
 * Data access for tracking a user's progress toward badges.
 * <p>
 * Implementations maintain progress counters, earned flags, and goals for each badge.
 * </p>
 */
public interface IBadgeProgressDAO {
    /**
     * Increments a user's progress toward a badge by a value (can be negative) and updates
     * the {@code is_earned} flag based on {@code progress_goal}.
     * @param userId user identifier
     * @param badgeId badge identifier
     * @param value delta to add to current progress (negative values decrease progress but not below 0)
     * @throws DataAccessException on persistence failure
     */
    void addProgress(int userId, int badgeId, int value);

    /**
     * Sets a user's progress toward a badge to an explicit value and updates the
     * {@code is_earned} flag.
     * @param userId user identifier
     * @param badgeId badge identifier
     * @param value new absolute progress value (clamped to {@code >= 0})
     * @throws DataAccessException on persistence failure
     */
    void setProgress(int userId, int badgeId, int value);

    /**
     * Resets a user's progress for a given badge to zero and clears {@code is_earned}.
     * @param userId  user identifier
     * @param badgeId badge identifier
     * @throws DataAccessException on persistence failure
     */
    void resetProgress(int userId, int badgeId);

    /**
     * Returns whether the user has earned the specified badge.
     * @param userId  user identifier
     * @param badgeId badge identifier
     * @return {@code true} if earned, {@code false} otherwise
     * @throws DataAccessException on query failure
     */
    boolean isEarned(int userId, int badgeId);

    /**
     * Returns the user's current progress value for the specified badge.
     * @param userId  user identifier
     * @param badgeId badge identifier
     * @return progress value (≥ 0), or 0 if no row exists
     * @throws DataAccessException on query failure
     */
    int getProgress(int userId, int badgeId);

    /**
     * Returns the progress goal for the specified badge and user.
     * @param userId  user identifier
     * @param badgeId badge identifier
     * @return goal value (≥ 0), or 0 if no row exists
     * @throws DataAccessException on query failure
     */
    int getGoal(int userId, int badgeId);

    /**
     * Returns the list of badges the user has earned.
     * @param userId user identifier
     * @return non-null list of completed badges
     * @throws DataAccessException on query failure
     */
    List<Badge> getCompletedBadgesByUser(int userId);
}
