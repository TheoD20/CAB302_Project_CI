package com.app.studysnap.controllers;
import com.app.studysnap.Main;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.services.Popup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

import static com.app.studysnap.services.TextParser.formatTime;

/**
 * Controller for the quiz result page.
 * <p>
 * Displays score, time taken, summary charts, and a review list of the question cards with
 * correct/incorrect styling applied. Provides actions to finish (return to dashboard) or restart.
 * </p>
 */
public class ResultPageController {

    // Fxml
    @FXML private Label scoreLabel;
    @FXML private PieChart resultChart;
    @FXML private VBox reviewLayout;
    @FXML private Label timeTaken;
    @FXML private BarChart<String, Number> resultBar;

    /**
     * The quiz associated with this result view (used for restart).
     */
    Quiz quiz;

    /**
     * Default constructor:
     * Creates a new {@code ResultPageController}.
     */
    public ResultPageController() {}

    /**
     * JavaFX initialization: initializes default UI state for labels and charts.
     */
    @FXML
    private void initialize() {
        if (scoreLabel != null) scoreLabel.setText("--/--");
        if (timeTaken != null) timeTaken.setText("00:00");
        if (resultChart != null) {
            resultChart.setLegendVisible(false);
            resultChart.setLabelsVisible(true);
            resultChart.getData().clear();
        }
        if (reviewLayout != null) {
            reviewLayout.getChildren().clear();
        }
    }

    /**
     * Sets the quiz for this result page (primarily used when restarting).
     * @param quiz the {@link Quiz} instance for result displaying
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    /**
     * Populates the result view with the user's score, elapsed time, charts,
     * and a review list of question cards with result styling applied.
     * @param score number of correct answers
     * @param total total number of questions
     * @param questionControllers the rendered question controllers from the play page
     * @param elapsedSeconds total elapsed time in seconds
     */
    public void setResult(int score, int total, List<QuestionController> questionControllers, int elapsedSeconds) {
        // Show score
        scoreLabel.setText(score + "/" + total);
        timeTaken.setText(formatTime(elapsedSeconds));
        // Donut chart (correct vs wrong)
        resultChart.getData().clear();
        resultChart.getData().add(new PieChart.Data("Correct", score));
        resultChart.getData().add(new PieChart.Data("Wrong", total - score));

        if (resultBar != null) {
            resultBar.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Correct", score));
            series.getData().add(new XYChart.Data<>("Wrong", Math.max(0, total - score)));
            resultBar.getData().add(series);
        }

        // Build review list (ensure clean state)
        reviewLayout.getChildren().clear();

        // Show questions with correct/wrong highlights
        for (QuestionController qc : questionControllers) {
            int selectedIndex = (qc == null) ? -1 : qc.getSelectedOptionIndex();
            int correctIndex  = (qc == null || qc.getQuestion() == null) ? -1 : qc.getQuestion().getCorrectOption();

            // Style the existing question card according to result
            if (qc != null) {
                qc.showResult(selectedIndex, correctIndex);
                reviewLayout.getChildren().add(qc.getRootNode());
            }
        }
    }

    /**
     * Returns to the dashboard after user confirmation.
     */
    @FXML
    private void handleFinish() {
        if(!Popup.confirm(
                "Finish Quiz",
                "Are you sure you want to close your attempt?"
        )) {
            return;
        }
        try {
            // Load the main dashboard/home page
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("dashboard.fxml"));
            Parent homeRoot = loader.load();

            // Replace the current scene with dashboard
            Scene scene = reviewLayout.getScene();
            scene.setRoot(homeRoot);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not return to Home page.");
            alert.showAndWait();
        }
    }

    /**
     * Restarts the quiz: confirms, reloads the play page, and injects the same quiz.
     */
    @FXML
    private void handleRestart() {
        if(!Popup.confirm(
                "Play Quiz",
                "Are you ready to attempt: " + quiz.getTitle()
        )) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("playQuiz.fxml"));
            Parent view = loader.load();

            // Get controller from the loaded FXML and pass the quiz
            PlayQuizPageController controller = loader.getController();
            if (controller != null) {
                controller.setQuiz(quiz);
            }

            reviewLayout.getScene().setRoot(view);
        } catch (Exception ex) {
            Popup.error("Failed to open quiz play page:\n" + ex.getMessage());
        }
    }
}
