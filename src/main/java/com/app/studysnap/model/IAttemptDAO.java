package com.app.studysnap.model;

import java.util.List;

public interface IAttemptDAO {
    void addAttempt(Attempt attempt);
    List<Attempt> getAttemptsByUser(int userId);
    List<Attempt> getAttemptsByQuiz(int quizId);
    Attempt getLastAttempt(int userId, int quizId);
    void deleteAttemptsByQuiz(int quizId);
}
