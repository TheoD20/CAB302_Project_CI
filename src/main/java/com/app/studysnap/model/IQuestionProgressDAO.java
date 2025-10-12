package com.app.studysnap.model;

import java.util.List;

public interface IQuestionProgressDAO {
    void addOrUpdateProgress(QuestionProgress progress);
    QuestionProgress getProgress(int userId, int questionId);
    List<QuestionProgress> getAllProgressForUser(int userId);
}