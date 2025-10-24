package com.app.studysnap.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BadgeTest {

    @Test
    void testConstructorWithId() {
        Badge badge = new Badge(1, "Quiz Master", "Awarded for perfect score",
                "icon1.png", "Score", 10);

        assertEquals(1, badge.getBadgeId());
        assertEquals("Quiz Master", badge.getBadgeName());
        assertEquals("Awarded for perfect score", badge.getBadgeDescription());
        assertEquals("icon1.png", badge.getBadgeIconPath());
        assertEquals("Score", badge.getBadgeType());
        assertEquals(10, badge.getBadgeGoal());
    }

    @Test
    void testConstructorWithoutId() {
        Badge badge = new Badge("Fast Learner", "Awarded for quick completion",
                "icon2.png", "Time", 5);

        assertEquals("Fast Learner", badge.getBadgeName());
        assertEquals("Awarded for quick completion", badge.getBadgeDescription());
        assertEquals("icon2.png", badge.getBadgeIconPath());
        assertEquals("Time", badge.getBadgeType());
        assertEquals(5, badge.getBadgeGoal());
        assertEquals(0, badge.getBadgeId(), "Badge ID should default to 0 when not provided");
    }

    @Test
    void testSettersAndGetters() {
        Badge badge = new Badge("Starter", "Initial achievement",
                "icon3.png", "Progress", 3);

        badge.setBadgeName("Pro Learner");
        badge.setBadgeDescription("Achieved advanced progress");
        badge.setBadgeIconPath("icon_pro.png");
        badge.setBadgeType("Skill");
        badge.setBadgeGoal(20);

        assertEquals("Pro Learner", badge.getBadgeName());
        assertEquals("Achieved advanced progress", badge.getBadgeDescription());
        assertEquals("icon_pro.png", badge.getBadgeIconPath());
        assertEquals("Skill", badge.getBadgeType());
        assertEquals(20, badge.getBadgeGoal());
    }

    @Test
    void testBadgeTypeCanBeUpdated() {
        Badge badge = new Badge("Dedicated", "Consistent study sessions",
                "icon4.png", "Streak", 7);

        badge.setBadgeType("Consistency");
        assertEquals("Consistency", badge.getBadgeType());
    }

    @Test
    void testGoalCanBeChanged() {
        Badge badge = new Badge("Focus Star", "Focused for 10 sessions",
                "icon5.png", "Focus", 10);

        badge.setBadgeGoal(15);
        assertEquals(15, badge.getBadgeGoal());
    }
}
