package com.app.studysnap.model;

import java.util.List;
import java.util.Map;

/**
 * A model representing a quiz with metadata and a list of {@link Question}s.
 * <p>
 * A quiz belongs to a creator (via {@code createdBy}), can be marked as private/public,
 * and can include a subject, title, and description.
 * </p>
 */
public class Quiz {
    private int quizId;
    private int createdBy; // user_id from Users table
    private String title;
    private String subject;
    private String description;
    private boolean is_private;
    private List<Question> questions; // questions for the quiz

    /**
     * Constructs a {@code Quiz} with all persisted fields populated.
     * @param quizId The unique identifier of the quiz (database primary key)
     * @param title The title of the quiz
     * @param subject The quiz subject or category
     * @param description A short description of the quiz (can be {@code null})
     * @param is_private Whether the quiz is private ({@code true}) or public ({@code false})
     * @param createdBy The user id of the quiz creator
     */
    public Quiz(int quizId, String title, String subject, String description, boolean is_private, int createdBy) {
        this.quizId = quizId;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.is_private = is_private;
        this.createdBy = createdBy;
    }

    /**
     * Constructs a {@code Quiz} without an assigned id (useful before persistence).
     *
     * @param title The title of the quiz
     * @param subject The quiz subject or category
     * @param description A short description of the quiz (can be {@code null})
     * @param is_private Whether the quiz is private ({@code true}) or public ({@code false})
     * @param createdBy The user id of the quiz creator
     */
    public Quiz(String title, String subject, String description, boolean is_private, int createdBy) {
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.is_private = is_private;
        this.createdBy = createdBy;
    }

    /**
     * Constructs a {@code Quiz} without a description.
     *
     * @param title The title of the quiz
     * @param subject The quiz subject or category
     * @param is_private Whether the quiz is private ({@code true}) or public ({@code false})
     * @param createdBy The user id of the quiz creator
     */
    public Quiz(String title, String subject, boolean is_private, int createdBy) {
        this.title = title;
        this.description = null;
        this.subject = subject;
        this.is_private = is_private;
        this.createdBy = createdBy;
    }

    /**
     * @return the unique identifier of the quiz
     */
    public int getQuizId() { return quizId; }

    /**
     * Sets the unique identifier of the quiz.
     * @param quizId quiz unique identifier
     */
    public void setQuizId(int quizId) { this.quizId = quizId; }

    /**
     * @return the title of the quiz
     */
    public String getTitle() { return title; }

    /**
     * Sets the quiz title.
     * @param title the title text
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * @return the subject or category of the quiz
     */
    public String getSubject() { return subject; }

    /**
     * Sets the quiz subject or category.
     * @param subject subject text
     */
    public void setSubject(String subject) { this.subject = subject; }

    /**
     * @return a short description of the quiz, or {@code null} if none
     */
    public String getDescription() { return description; }

    /**
     * Sets the quiz description.
     * @param description the description text (can be {@code null})
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * @return {@code true} if the quiz is private; {@code false} if public
     */
    public boolean get_is_private() { return is_private; }

    /**
     * Marks the quiz as private or public.
     * @param is_private {@code true} for private, {@code false} for public
     */
    public void set_is_private(boolean is_private) { this.is_private = is_private; }

    /**
     * @return the creator's user id
     */
    public int getCreatedBy() { return createdBy; }

    /**
     * Sets the creator's user id.
     * @param createdBy user id of the creator
     */
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    /**
     * @return the list of questions that belong to this quiz, or {@code null} if not loaded
     */
    public List<Question> getQuestions() { return questions; }

    /**
     * Sets the in-memory questions for this quiz (not persisted automatically).
     * @param questions the questions to associate
     */
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}