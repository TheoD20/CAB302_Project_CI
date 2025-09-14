package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.SqliteQuestionDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import java.io.IOException;
import java.util.List;

public class PlayQuizPageController {
    private Quiz quiz;

    @FXML
    private VBox questionLayout;

    @FXML
    private Label quizTimer;// this is for displaying the time elapse

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        loadQuestions();  // display questions after quiz is injected
    }

    private void loadQuestions() {
        SqliteQuestionDAO questionDAO = new SqliteQuestionDAO();
        List<Question> questions = questionDAO.getQuestionsForQuiz(quiz.getQuizId());

        for (Question question : questions) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("question.fxml"));
                Node questionNode = fxmlLoader.load();

                // Pass each question into its controller
                QuestionController controller = fxmlLoader.getController();
                controller.setData(question);

                questionLayout.getChildren().add(questionNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void initialize() {
        // Leave empty, or do static UI setup (not dependent on quiz)
    }
}
