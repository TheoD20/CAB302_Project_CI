package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.model.*;

import com.app.studysnap.services.BadgeService;
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
import static com.app.studysnap.services.TextParser.isBlank;

/**
 * Controller for the quiz play page.
 * <p>
 * Loads quiz questions, tracks elapsed time, handles submission/cancel actions,
 * persists the attempt, and computes badge progress before routing to the result page.
 * </p>
 */
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

    /**
     * Timeline for updating the visible timer every second.
     */
    private Timeline timeline;// this is for displaying the time elapse
    private int elapsedSecond = 0;

    /**
     * Holds the controller for each rendered question card, used to collect answers/results.
     */
    List<QuestionController> questionControllers = new ArrayList<>();

    /**
     * JavaFX initialization: initializes the timer display.
     */
    @FXML
    private void initialize() {
        if (quizTimer != null) {
            quizTimer.setText("00:00:00");
        }
    }

    /**
     * Injects the quiz to be played, renders its questions, sets title/subtitle, and starts the timer.
     * @param quiz the {@link Quiz} to play
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        loadQuestions();  // display questions after quiz is injected

        if (quizTitle != null) {
            String t = (quiz != null && !isBlank(quiz.getTitle()))
                    ? quiz.getTitle() : "Untitled Quiz";
            quizTitle.setText(t);
        }
        if (quizSubtitle != null) {
            String t = (quiz != null && !isBlank(quiz.getSubject()))
                    ? quiz.getSubject() : "-";
            quizSubtitle.setText("Subject: " + t);
        }

        startTimer(); // start timer as soon as quiz is loaded.
    }

    /**
     * Loads questions for the current quiz and renders a question card for each.
     * Any load errors are caught and displayed via popup.
     */
    private void loadQuestions() {
        questionLayout.getChildren().clear();
        questionControllers.clear();
        SqliteQuestionDAO questionDAO = new SqliteQuestionDAO();

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

    /**
     * Validates unanswered questions, confirms submission, scores the attempt,
     * persists the result, computes badge progress, and navigates to the result page.
     */
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

            // Calculate badge progress
            try {
                // Get any unanswered questions
                int unansweredCount = (int) questionControllers.stream()
                        .filter(qc -> qc.getSelectedOptionIndex() == -1).count();

                BadgeService badgeSvc = new com.app.studysnap.services.BadgeService();
                badgeSvc.UpdateScoreTypeBadges(attempt, unansweredCount);
            } catch (Exception ignore) {
                // no blocking
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        goToResultPage(score, total, elapsedSecond);
    }

    /**
     * Loads the result view and injects the computed results and quiz metadata.
     * @param score number of correct answers
     * @param total total questions
     * @param elapsedSeconds time taken in seconds
     */
    private void goToResultPage(int score, int total, int elapsedSeconds) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("quizResult.fxml"));
            Parent resultRoot = loader.load();

            // Pass data to result page
            ResultPageController controller = loader.getController();
            if (controller != null) {
                controller.setResult(score, total, questionControllers, elapsedSeconds);
                controller.setQuiz(quiz);
            }

            // Replace current view with result page
            questionLayout.getScene().setRoot(resultRoot);

        } catch (IOException e) {
            Popup.error(e.getMessage());
        }
    }

    /**
     * Cancels the quiz (with confirmation), stops the timer, and navigates back to the dashboard.
     *
     * @throws IOException if navigation fails
     */
    @FXML
    private void handleCancel() throws IOException {
        if (!Popup.confirm("Cancel quiz", "Are you sure you want to cancel and return to Home?")) return;
        timeline.stop();
        Navigator.goTo(cancelButton, "dashboard.fxml");
    }

    /**
     * Starts the elapsed time counter and updates the UI every second.
     */
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

    /**
     * Formats seconds as {@code HH:mm:ss}.
     * @param seconds total seconds elapsed
     * @return formatted time string
     */
    private String formatTime(int seconds){
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
