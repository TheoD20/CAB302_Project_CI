package com.app.studysnap.model;

public class QuestionProgress {
    private int userId;
    private int questionId;
    private String lastAttempted; // store as String for SQLite compatibility

    public QuestionProgress(int userId, int questionId, String lastAttempted) {
        this.userId = userId;
        this.questionId = questionId;
        this.lastAttempted = lastAttempted;
    }

    public int getUserId() { return userId; }
    public int getQuestionId() { return questionId; }
    public String getLastAttempted() { return lastAttempted; }

    public void setLastAttempted(String lastAttempted) {
        this.lastAttempted = lastAttempted;
    }
}
