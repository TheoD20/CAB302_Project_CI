package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.util.List;

/**
 * Data access for application badges.
 * <p>
 * Persist new badges, query existing generally, by id or type.
 * </p>
 */
public interface IBadgeDAO {
    
    /**
     * Inserts a new badge.
     * @param b the badge to add (non-null)
     * @throws DataAccessException on persistence failure
     */
    void addBadge(Badge b);

    /**
     * Returns all badges.
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<Badge> getAllBadges();

    /**
     * Looks up a badge by its identifier.
     * @param badge_id the badge id
     * @return the badge, or {@code null} if not found
     * @throws DataAccessException on query failure
     */
    Badge getBadgeById(int badge_id);

    /**
     * Returns badges filtered by type/category.
     * @param type badge type (case-sensitive unless implementation states otherwise)
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<Badge> getBadgesByType(String type);

    /**
     * Deletes a badge by id.
     * @param badgeId the badge id
     * @throws DataAccessException on delete failure
     */
    void deleteBadge(int badgeId);

    /**
     * Seeds a predefined set of badges if they do not already exist.
     * @throws DataAccessException on persistence failure
     */
    void initializeBadges();
}
