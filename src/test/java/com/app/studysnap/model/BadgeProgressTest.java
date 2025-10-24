package com.app.studysnap.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BadgeProgressTest {

    @Test
    void testConstructorWithProgressEqualGoal_ShouldBeEarned() {
        BadgeProgress bp = new BadgeProgress(1, 2, 10, 10);

        assertEquals(1, bp.getUserId());
        assertEquals(2, bp.getBadgeId());
        assertTrue(bp.getIsEarned(), "Badge should be earned when progress equals goal");
        assertEquals(10, bp.getProgress());
        assertEquals(10, bp.getProgressGoal());
    }

    @Test
    void testConstructorWithProgressNotEqualGoal_ShouldNotBeEarned() {
        BadgeProgress bp = new BadgeProgress(1, 2, 5, 10);

        assertFalse(bp.getIsEarned(), "Badge should not be earned when progress < goal");
        assertEquals(5, bp.getProgress());
        assertEquals(10, bp.getProgressGoal());
    }

    @Test
    void testConstructorWithoutProgress_DefaultValues() {
        BadgeProgress bp = new BadgeProgress(3, 4, 20);

        assertEquals(3, bp.getUserId());
        assertEquals(4, bp.getBadgeId());
        assertFalse(bp.getIsEarned(), "New badge progress should start as not earned");
        assertEquals(0, bp.getProgress(), "Progress should start at 0");
        assertEquals(20, bp.getProgressGoal());
    }

    @Test
    void testEarnedStatusChangesBasedOnProgressComparison() {
        BadgeProgress earned = new BadgeProgress(5, 6, 8, 8);
        BadgeProgress notEarned = new BadgeProgress(5, 6, 7, 8);

        assertTrue(earned.getIsEarned(), "Should be earned when progress == goal");
        assertFalse(notEarned.getIsEarned(), "Should not be earned when progress < goal");
    }
}
