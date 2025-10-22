package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Data access for quiz attempts.
 * <p>
 * Persist new attempts, query existing by userId, quizID.
 * Provide streak calculation, access to all attempts relating
 * a User with a Quiz.
 * </p>
 */
public interface IAttemptDAO {

    /**
     * Persists a new attempt.
     * @param attempt attempt to insert (non-null)
     * @throws DataAccessException on persistence failure
     */
    void addAttempt(Attempt attempt);

    /**
     * Returns attempts for the given user, most recent first.
     * @param userId user identifier
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<Attempt> getAttemptsByUser(int userId);

    /**
     * Returns attempts for the given quiz, most recent first.
     * @param quizId quiz identifier
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<Attempt> getAttemptsByQuiz(int quizId);

    /**
     * Returns the most recent attempt by a user on a quiz.
     * @param userId user identifier
     * @param quizId quiz identifier
     * @return the last attempt, or {@code null} if none
     * @throws DataAccessException on query failure
     */
    Attempt getLastAttempt(int userId, int quizId);

    /**
     * Computes the total number of correct answers across all attempts by a user.
     * <p>Derives the count from the {@link Attempt#getScore()} string (e.g., {@code "8/10"}).</p>
     * @param userId user identifier
     * @return total correct answers (≥ 0)
     * @throws DataAccessException on query/parsing failure
     */
    int getCorrectAnswersByUser(int userId);

    /**
     * Computes the current daily streak (ending today if played, otherwise ending yesterday).
     * @param userId user identifier
     * @return current streak in days (≥ 0)
     * @throws DataAccessException on query failure
     */
    int getCurrentStreakByUser(int userId);

    /**
     * Returns the best (maximum) daily streak ever recorded for the user.
     * @param userId user identifier
     * @return max streak in days (≥ 0)
     * @throws DataAccessException on query failure
     */
    int getBestStreakByUser(int userId);

    /**
     * Computes the streak as of a specific date.
     * @param userId user identifier
     * @param asOfDate the anchor date (inclusive)
     * @return streak length in days (≥ 0)
     * @throws DataAccessException on query failure
     */
    int getStreakAsOf(int userId, LocalDate asOfDate);

    /**
     * Counts attempts per day in the inclusive date range.
     * @param userId user identifier
     * @param start inclusive start date
     * @param end inclusive end date
     * @return map of {@code LocalDate → count}; absent dates imply zero
     * @throws DataAccessException on query failure
     */
    Map<LocalDate, Integer> getAttemptsByDateRange(int userId, LocalDate start, LocalDate end);
}