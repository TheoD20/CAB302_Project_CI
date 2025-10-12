package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.model.*;

import com.app.studysnap.services.Navigator;
import com.app.studysnap.services.Popup;
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

import static com.app.studysnap.auth.Session.getCurrentUser;

public class PlayQuizPageController {
    private Quiz quiz;

    @FXML
    private VBox questionLayout;
    @FXML
    private Label quizTitle;
    @FXML
    private Label quizSubtitle;
    @FXML
    private Label quizTimer;
    @FXML
    private Button cancelButton;

    private Timeline timeline;// this is for displaying the time elapse
    private int elapsedSecond = 0;
    List<QuestionController> questionControllers = new ArrayList<>();

    @FXML
    private void initialize() {
        if (quizTimer != null) {
            quizTimer.setText("00:00:00");
        }
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        loadQuestions();  // display questions after quiz is injected

        if (quizTitle != null) {
            String t = (quiz != null && quiz.getTitle() != null && !quiz.getTitle().isBlank())
                    ? quiz.getTitle() : "Untitled Quiz";
            quizTitle.setText(t);
        }
        if (quizSubtitle != null) {
            String t = (quiz != null && quiz.getSubject() != null && !quiz.getSubject().isBlank())
                    ? quiz.getSubject() : "-";
            quizSubtitle.setText("Subject: " + t);
        }

        startTimer(); // start timer as soon as quiz is loaded.
    }

    private void loadQuestions() {
        questionLayout.getChildren().clear();
        questionControllers.clear();
        SqliteQuestionDAO questionDAO = new SqliteQuestionDAO();
        SqliteQuestionProgressDAO progressDAO = new SqliteQuestionProgressDAO(); // New for

        List<Question> questions = questionDAO.getQuestionsForQuiz(quiz.getQuizId());

        for (Question question : questions) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("question.fxml"));
                Parent card = fxmlLoader.load();

                // Pass each question into its controller
                QuestionController controller = fxmlLoader.getController();
                controller.setData(question);
                questionControllers.add(controller);
                card.setUserData(controller);

                questionLayout.getChildren().add(card);
            } catch (Exception e) {
                Popup.error("Could not load a question card: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSubmit() {
        long unanswered = questionControllers.stream()
                .filter(qc -> qc.getSelectedOptionIndex() == -1)
                .count();

        String prompt = (unanswered > 0)
                ? "You have " + unanswered + " unanswered question" + (unanswered > 1 ? "s" : "") + ". Submit anyway?"
                : "Submit your answers?";

        if (!Popup.confirm("Submit quiz", prompt)) {
            return;
        }

        timeline.stop();

        int score = 0;
        int total = questionControllers.size();

        for (Node node : questionLayout.getChildren()) {
            QuestionController controller = (QuestionController) node.getUserData();

            if (controller.isCorrect()) {
                score++;
            }
        }

        for (Node node : questionLayout.getChildren()){
            QuestionController controller = (QuestionController) node.getUserData();
            int selected = controller.getSelectedOptionIndex();
            int correct = controller.getQuestion().getCorrectOption();
            controller.showResult(selected, correct);
        }

        //stores the quiz result to attempt table in database.
        try {
            SqliteAttemptDAO attemptDAO = new SqliteAttemptDAO();
            String scoreText = score + "/" + total;
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Attempt attempt = new Attempt(
                    getCurrentUser().getUserId(),
                    quiz.getQuizId(),
                    scoreText,
                    elapsedSecond,
                    timestamp
            );

            attemptDAO.addAttempt(attempt);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            Popup.error(e.getMessage());
        }
    }

    @FXML
    private void handleCancel() throws IOException {
        if (!Popup.confirm("Cancel quiz", "Are you sure you want to cancel and return to Home?")) return;
        timeline.stop();
        Navigator.goTo(cancelButton, "dashboard.fxml");
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
