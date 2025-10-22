package com.app.studysnap.model;

/**
 * An object representing a single multiple-choice question that belongs to a quiz.
 * <p>
 * Each question contains the question text, up to five answer options, and the index
 * of the correct option (1–5).
 * </p>
 */

public class Question {
    private int questionId;
    private int quizId;
    private String question;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String option5;
    private Integer correctOption;

    /**
     * Creates an empty {@code Question}. Fields can be populated via setters.
     */
    public Question() {
    }

    /**
     * Creates a fully specified {@code Question} including its database id.
     *
     * @param questionId    Unique identifier of the question (database primary key)
     * @param quizId        Identifier of the quiz this question belongs to
     * @param question      The question text (stem)
     * @param option1       Text for option 1 (may be {@code null})
     * @param option2       Text for option 2 (may be {@code null})
     * @param option3       Text for option 3 (may be {@code null})
     * @param option4       Text for option 4 (may be {@code null})
     * @param option5       Text for option 5 (may be {@code null})
     * @param correctOption Index of the correct option (1–5)
     */
    public Question(int questionId, int quizId, String question,
                    String option1, String option2, String option3,
                    String option4, String option5, int correctOption) {
        this.questionId = questionId;
        this.quizId = quizId;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.option5 = option5;
        this.correctOption = correctOption;
    }

    /**
     * Creates a {@code Question} without a database id (useful before persistence).
     * @param quizId Identifier of the quiz this question belongs to
     * @param question The question text (stem)
     * @param option1 Text for option 1 (can be {@code null})
     * @param option2 Text for option 2 (can be {@code null})
     * @param option3 Text for option 3 (can be {@code null})
     * @param option4 Text for option 4 (can be {@code null})
     * @param option5 Text for option 5 (can be {@code null})
     * @param correctOption Index of the correct option (1-5)
     */
    public Question(int quizId, String question, String option1, String option2, String option3, String option4, String option5, int correctOption) {
        this.quizId = quizId;
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.option5 = option5;
        this.correctOption = correctOption;
    }

    /**
     * Getter for question ID
     * @return the unique identifier of the question
     */
    public int getQuestionId() {
        return questionId;
    }

    /**
     * Sets the unique identifier of the question.
     * @param questionId question unique identifier
     */
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    /**
     * Getter for ID of quiz this question belongs
     * @return the id of the quiz this question belongs to
     */
    public int getQuizId() {
        return quizId;
    }

    /**
     * Sets the id of the quiz this question belongs to.
     * @param quizId quiz identifier
     */
    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    /**
     * Getter for question stem text
     * @return the question text (stem)
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Sets the question text (stem).
     * @param question the question text
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Getter for option 1 text
     * @return text for option 1 (can be {@code null})
     */
    public String getOption1() {
        return option1;
    }

    /**
     * Sets text for option 1.
     * @param option1 option text
     */
    public void setOption1(String option1) {
        this.option1 = option1;
    }

    /**
     * Getter for option 2 text
     * @return text for option 2 (can be {@code null})
     */
    public String getOption2() {
        return option2;
    }

    /**
     * Sets text for option 2.
     * @param option2 option text
     */
    public void setOption2(String option2) {
        this.option2 = option2;
    }

    /**
     * Getter for option 3 text
     * @return text for option 3 (can be {@code null})
     */
    public String getOption3() {
        return option3;
    }

    /**
     * Sets text for option 3.
     * @param option3 option text
     */
    public void setOption3(String option3) {
        this.option3 = option3;
    }

    /**
     * Getter for option 4 text
     * @return text for option 4 (can be {@code null})
     */
    public String getOption4() {
        return option4;
    }

    /**
     * Sets text for option 4.
     * @param option4 option text
     */
    public void setOption4(String option4) {
        this.option4 = option4;
    }

    /**
     * Getter for option 5 text
     * @return text for option 5 (can be {@code null})
     */
    public String getOption5() {
        return option5;
    }

    /**
     * Sets text for option 5.
     * @param option5 option text
     */
    public void setOption5(String option5) {
        this.option5 = option5;
    }

    /**
     * Getter for correct option index
     * @return the index of the correct option (1-5), or {@code null} if unset
     */
    public Integer getCorrectOption() {
        return correctOption;
    }

    /**
     * Sets the index of the correct option.
     * @param correctOption the index (1-5), or {@code null} if unset
     */
    public void setCorrectOption(Integer correctOption) {
        this.correctOption = correctOption;
    }
}