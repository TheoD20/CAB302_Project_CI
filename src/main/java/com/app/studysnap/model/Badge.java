package com.app.studysnap.model;

public class Badge {
    private int badgeId;
    private String badgeName;
    private String badgeDescription;
    private String iconPath;
    private String badgeType;
    private int completionGoal;

    public Badge(int id, String name, String description, String path, String type, int goal) {
        badgeId = id;
        badgeName = name;
        badgeDescription = description;
        iconPath = path;
        badgeType = type;
        completionGoal = goal;
    }

    public Badge(String name, String description, String path, String type, int goal) {
        badgeName = name;
        badgeDescription = description;
        iconPath = path;
        badgeType = type;
        completionGoal = goal;
    }

    // Getters
    public int getBadgeId() {
        return badgeId;
    }
    public String getBadgeName() {
        return badgeName;
    }
    public String getBadgeDescription() {
        return badgeDescription;
    }
    public String getBadgeIconPath() {
        return iconPath;
    }
    public String getBadgeType() {
        return badgeType;
    }
    public int getBadgeGoal() {
        return completionGoal;
    }

    // Setters
    public void setBadgeName(String name) {
        badgeName = name;
    }
    public void setBadgeDescription(String desc) {
        badgeDescription = desc;
    }
    public void setBadgeIconPath(String path) {
        iconPath = path;
    }
    public void setBadgeType(String type) {
        badgeType = type;
    }
    public void setBadgeGoal(int goal) {
        completionGoal = goal;
    }
}
