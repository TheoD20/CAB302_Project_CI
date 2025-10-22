package com.app.studysnap.model;

/**
 * A model class representing an earnable badge in the application.
 * <p>
 * Each badge has a unique identifier, display name, description, icon path, a type/category,
 * and a numeric completion goal (e.g., number of actions required to earn it).
 * </p>
 */
public class Badge {
    private int badgeId;
    private String badgeName;
    private String badgeDescription;
    private String iconPath;
    private String badgeType;
    private int completionGoal;

    /**
     * Constructs a new {@code Badge} with an explicit ID (e.g., when feeding from storage).
     * @param id The unique identifier of the badge
     * @param name The display name of the badge
     * @param description A short description explaining how or why the badge is earned
     * @param path The icon path or resource identifier for the badge image
     * @param type The logical badge category/type (e.g., {@code "SCORE"}, {@code "STREAK"})
     * @param goal The numeric completion goal required to earn the badge
     */
    public Badge(int id, String name, String description, String path, String type, int goal) {
        badgeId = id;
        badgeName = name;
        badgeDescription = description;
        iconPath = path;
        badgeType = type;
        completionGoal = goal;
    }

    /**
     * Constructs a new {@code Badge} without an explicit ID (e.g., prior to persistence).
     * @param name The display name of the badge
     * @param description A short description explaining how or why the badge is earned
     * @param path The icon path or resource identifier for the badge image
     * @param type The logical badge category/type (e.g., {@code "SCORE"}, {@code "STREAK"})
     * @param goal The numeric completion goal required to earn the badge
     */
    public Badge(String name, String description, String path, String type, int goal) {
        badgeName = name;
        badgeDescription = description;
        iconPath = path;
        badgeType = type;
        completionGoal = goal;
    }

    // Getters

    /**
     * @return The unique identifier of the badge
     */
    public int getBadgeId() {
        return badgeId;
    }

    /**
     * @return The display name of the badge
     */
    public String getBadgeName() {
        return badgeName;
    }

    /**
     * @return The description of the badge
     */
    public String getBadgeDescription() {
        return badgeDescription;
    }

    /**
     * @return The icon path or resource identifier for the badge image
     */
    public String getBadgeIconPath() {
        return iconPath;
    }

    /**
     * @return The badge type/category (e.g., {@code "score"}, {@code "creation"})
     */
    public String getBadgeType() {
        return badgeType;
    }

    /**
     * @return The numeric completion goal required to earn the badge
     */
    public int getBadgeGoal() {
        return completionGoal;
    }

    // Setters

    /**
     * Sets the display name of the badge.
     * @param name The new badge name
     */
    public void setBadgeName(String name) {
        badgeName = name;
    }

    /**
     * Sets the description of the badge.
     * @param desc The new badge description
     */
    public void setBadgeDescription(String desc) {
        badgeDescription = desc;
    }

    /**
     * Sets the icon path for the badge image.
     * @param path The new icon path or resource identifier
     */
    public void setBadgeIconPath(String path) {
        iconPath = path;
    }

    /**
     * Sets the type/category of the badge.
     * @param type The new badge type (e.g., {@code "score"})
     */
    public void setBadgeType(String type) {
        badgeType = type;
    }

    /**
     * Sets the numeric completion goal required to earn the badge.
     * @param goal The new completion goal value
     */
    public void setBadgeGoal(int goal) {
        completionGoal = goal;
    }
}