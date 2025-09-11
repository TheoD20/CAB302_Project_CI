package com.app.studysnap.model;

import java.util.List;
import java.util.Map;

public class Quiz {
    private int quizId;
    private int createdBy;   // user_id from Users table
    private String title;
    private String subject;
    private String description;
    private boolean is_private;
    private List<Question> questions; // questions for the quiz

    // Constructors
    public Quiz(int quizId, String title, String subject, String description,boolean is_private, int createdBy) {
        this.quizId = quizId;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.is_private = is_private;
        this.createdBy = createdBy;
    }

    public Quiz(String title, String subject, String description,boolean is_private, int createdBy) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.is_private = is_private;
        this.createdBy = createdBy;
    }

    public Quiz(String title, String subject, boolean is_private, int createdBy) {
        this.title = title;
        this.description = null;
        this.subject = subject;
        this.is_private = is_private;
        this.createdBy = createdBy;
    }

    // Getters and setters
    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) {this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) {this.description = description; }

    public boolean get_is_private() {return is_private; }
    public void set_is_private(boolean is_private){this.is_private = is_private; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
