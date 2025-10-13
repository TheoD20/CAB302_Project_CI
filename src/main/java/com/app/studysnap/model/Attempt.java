package com.app.studysnap.model;

public class Attempt {
    private int attemptId;
    private int userId;
    private int quizId;
    private String score;
    private int timeTaken;
    private String attemptAt;

    public Attempt(int userId, int quizId, String score, int timeTaken, String attemptAt){
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.timeTaken = timeTaken;
        this.attemptAt = attemptAt;
    }

    public Attempt(int attemptId, int userId, int quizId, String score, int timeTaken, String attemptAt) {
        this.attemptId = attemptId;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.timeTaken = timeTaken;
        this.attemptAt = attemptAt;
    }

    // Getters
    public int getAttemptId() { return attemptId; }
    public int getUserId() { return userId; }
    public int getQuizId() { return quizId; }
    public String getScore() { return score; }
    public int getTimeTaken() { return timeTaken; }
    public String getAttemptAt() { return attemptAt; }

    // Setters
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public void setScore(String score) { this.score = score; }
    public void setTimeTaken(int timeTaken) { this.timeTaken = timeTaken; }
    public void setAttemptAt(String attemptAt) { this.attemptAt = attemptAt; }

}
