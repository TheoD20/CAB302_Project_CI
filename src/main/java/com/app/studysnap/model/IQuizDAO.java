package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.util.List;
import java.util.Map;

/**
 * Data access for quizzes and their metadata.
 * <p>
 * Persist new quizzes, query for existing using different filters.
 * Provide helpers for updating and deleting quizzes.
 * </p>
 */
public interface IQuizDAO {

    /**
     * Inserts a new quiz.
     * @param quiz the quiz to add (non-null)
     * @throws DataAccessException on persistence failure
     */
    void addQuiz(Quiz quiz);

    /**
     * Returns all quizzes.
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<Quiz> getAllQuizzes();

    /**
     * Loads a quiz by id.
     * @param quizId quiz identifier
     * @return the quiz, or {@code null} if not found
     * @throws DataAccessException on query failure
     */
    Quiz getQuizById(int quizId);

    /**
     * Returns all quizzes created by a specific user.
     * @param userId user identifier
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<Quiz> getQuizzesByUser(int userId);

    /**
     * Returns counts of a user's decks grouped by subject/topic.
     * @param userId user identifier
     * @return non-null map of subject → count
     * @throws DataAccessException on query failure
     */
    Map<String, Integer> getDeckCountsByTopic(int userId);

    /**
     * Finds public quizzes matching a text query across title, subject, description, and author.
     * @param query text filter (can be {@code null}/blank to match all)
     * @return non-null list
     * @throws DataAccessException on query failure
     */
    List<PublicListItem> findPublic(String query);

    /**
     * Updates an existing quiz (matched by id).
     * @param quiz quiz model with an id already set
     * @throws DataAccessException on update failure
     */
    void updateQuiz(Quiz quiz);

    /**
     * Deletes a quiz by id (related questions may be cascaded).
     * @param quizId quiz identifier
     * @throws DataAccessException on delete failure
     */
    void deleteQuiz(int quizId);

    /**
     * Destructive helper to clear {@code Quizzes} (and related sequences).
     * Intended for admin.
     * @throws DataAccessException on failure
     */
    void resetQuizzesTable();

    /**
     * Lightweight DTO for public listings.
     * @param quizId id
     * @param name title
     * @param subject subject/category
     * @param description summary
     * @param author creator username
     */
    record PublicListItem(int quizId, String name, String subject, String description, String author) {}
}