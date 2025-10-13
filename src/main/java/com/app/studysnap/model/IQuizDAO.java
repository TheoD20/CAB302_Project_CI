package com.app.studysnap.model;

import java.util.List;
import java.util.Map;

public interface IQuizDAO {
    void addQuiz(Quiz quiz);
    List<Quiz> getAllQuizzes();
    Quiz getQuizById(int quizId);
    List<Quiz> getQuizzesByUser(int userId);
    Map<String, Integer> getDeckCountsByTopic(int userId);
    List<PublicListItem> findPublic(String query);
    void updateQuiz(Quiz quiz);
    void deleteQuiz(int quizId);

    record PublicListItem(int quizId, String name, String subject, String description, String author) {}

}