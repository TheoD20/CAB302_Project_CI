package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.util.List;

/**
 * Data access for quiz questions.
 * <p>
 * Persist new questions, query existing by questionId or quizID.
 * Provide helpers for updating and deleting questions.
 * </p>
 */
public interface IQuestionDAO {

    /**
     * Inserts a new question.
     * @param question the question to add (non-null)
     * @throws DataAccessException on persistence failure
     */
    void addQuestion(Question question);

    /**
     * Looks up a question by its identifier.
     * @param questionId the question id
     * @return the question object, or {@code null} if not found
     * @throws DataAccessException on query failure
     */
    Question getQuestionById(int questionId);

    /**
     * Returns questions for a quiz, ordered by {@code question_id}.
     * @param quizId the quiz id
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<Question> getQuestionsForQuiz(int quizId);

    /**
     * Updates the given question (matching by {@code question_id}).
     * @param question the updated question model (non-null; must have an id)
     * @throws DataAccessException on update failure
     */
    void updateQuestion(Question question);

    /**
     * Deletes a question by id.
     * @param questionId the question id
     * @throws DataAccessException on delete failure
     */
    void deleteQuestion(int questionId);

    /**
     * Replaces all questions for a quiz.
     * Deletes existing rows for the quiz and inserts the provided list.
     * @param quizId the quiz id
     * @param questions the new set of questions
     * @throws DataAccessException on persistence failure
     */
    void replaceForQuiz(int quizId, List<Question> questions);
}