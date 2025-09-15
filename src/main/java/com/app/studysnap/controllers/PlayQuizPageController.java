package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.SqliteQuestionDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayQuizPageController {
    private Quiz quiz;

    @FXML
    private VBox questionLayout;

    @FXML
    private Label quizTimer;// this is for displaying the time elapse

    List<QuestionController> questionControllers = new ArrayList<>();

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
                questionControllers.add(controller);

                questionNode.setUserData(controller);

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

    private void showResultScore(int score, int total) {
        Alert result = new Alert(Alert.AlertType.INFORMATION);
        result.setHeaderText("Your Score: " + score + "/" + total);
        result.showAndWait();
    }

    @FXML
    private void handleSubmit() {
        System.out.println("Submit button clicked!");
        boolean hasUnanswered = false;
        int score = 0;
        int total = questionControllers.size();

        for (Node node : questionLayout.getChildren()) {
            QuestionController controller = (QuestionController) node.getUserData();
            int selected = controller.getSelectedOptionIndex();
            int correct = controller.getQuestion().getCorrectOption(); // get the correct option(int) of the question

            if (selected == -1) {
                hasUnanswered = true;
            } else if (selected == correct) {
                score++;
            }
            controller.showResult(selected, correct); // highlight answers
        }

        if (hasUnanswered) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unanswered Questions");
            alert.setHeaderText("Some questions are unanswered");
            alert.setContentText("Are you sure you want to submit?");
            ButtonType response = alert.showAndWait().orElse(ButtonType.CANCEL);

            if (response != ButtonType.OK) return;
        }

        goToResultPage(score, total);
    }

    private void goToResultPage(int score, int total) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("quizResult.fxml"));
            Parent resultRoot = loader.load();

            // Pass data to result page
            ResultPageController controller = loader.getController();
            controller.setResult(score, total, questionControllers);

            // Replace current view with result page
            questionLayout.getScene().setRoot(resultRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
