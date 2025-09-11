package com.app.studysnap.model;

import java.util.List;

public interface IQuizDAO {
    void addQuiz(Quiz quiz);
    Quiz getQuizById(int quizId);
    List<Quiz> getQuizzesByUser(int userId);
    List<PublicListItem> findPublic(String query);
    void updateQuiz(Quiz quiz);
    void deleteQuiz(int quizId);

    public record PublicListItem(int quizId, String name, String subject, String description, String author) {}

}
