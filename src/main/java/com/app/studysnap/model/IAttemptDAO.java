package com.app.studysnap.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IAttemptDAO {
    void addAttempt(Attempt attempt);
    List<Attempt> getAttemptsByUser(int userId);
    List<Attempt> getAttemptsByQuiz(int quizId);
    Attempt getLastAttempt(int userId, int quizId);
    int getCorrectAnswersByUser(int userId);
    int getCurrentStreakByUser(int userId);
    int getBestStreakByUser(int userId);
    int getStreakAsOf(int userId, LocalDate asOfDate);
    Map<LocalDate, Integer> getAttemptsByDateRange(int userId, LocalDate start, LocalDate end);
}
