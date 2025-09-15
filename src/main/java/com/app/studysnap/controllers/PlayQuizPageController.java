package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.SqliteQuestionDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.util.Duration;
import java.time.LocalTime;

public class PlayQuizPageController {
    private Quiz quiz;

    @FXML
    private VBox questionLayout;

    @FXML
    private Label quizTimer;
    private Timeline timeline;// this is for displaying the time elapse
    private int elapsedSecond = 0;
    List<QuestionController> questionControllers = new ArrayList<>();

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        loadQuestions();  // display questions after quiz is injected
        startTimer(); // start timer as soon as quiz is loaded.
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

    @FXML
    private void handleSubmit() {
//        System.out.println("Submit button clicked!"); for testing if the button is clickable
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
        }

        if(hasUnanswered){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unanswered Questions");
            alert.setHeaderText("Some questions are unanswered");
            alert.setContentText("Are you sure you want to submit?");
            ButtonType response = alert.showAndWait().orElse(ButtonType.CANCEL);

            if(response != ButtonType.OK){
                return; // this means user canceled -> goes back to the quiz play
            }
        }

        for (Node node : questionLayout.getChildren()){
            QuestionController controller =(QuestionController) node.getUserData();
            int selected = controller.getSelectedOptionIndex();
            int correct = controller.getQuestion().getCorrectOption();
            controller.showResult(selected, correct);
        }


        timeline.stop();
        goToResultPage(score, total, elapsedSecond);
    }

    private void goToResultPage(int score, int total, int elapsedSeconds) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("quizResult.fxml"));
            Parent resultRoot = loader.load();

            // Pass data to result page
            ResultPageController controller = loader.getController();
            controller.setResult(score, total, questionControllers, elapsedSeconds);

            // Replace current view with result page
            questionLayout.getScene().setRoot(resultRoot);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTimer(){
        elapsedSecond = 0;
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    elapsedSecond ++;
                    quizTimer.setText(formatTime(elapsedSecond));
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private String formatTime(int seconds){
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
