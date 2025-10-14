package com.app.studysnap.model;

import java.util.List;

public interface IBadgeDAO {
    void addBadge(Badge b);
    List<Badge> getAllBadges ();
    List<Badge> getBadgesByType (String type);
    void deleteBadge(int badgeId);
    void initializeBadges();
}
