package com.app.studysnap.controllers;
import com.app.studysnap.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class ResultPageController {

    @FXML private Label scoreLabel;
    @FXML private PieChart resultChart;
    @FXML private VBox reviewLayout;
    @FXML private Label timeTaken;

    public void setResult(int score, int total, List<QuestionController> questionControllers, int elapsedSeconds) {
        // Show score
        scoreLabel.setText(score + "/" + total);
        timeTaken.setText("Time Taken" + formatTime(elapsedSeconds));
        // Donut chart (correct vs wrong)
        resultChart.getData().clear();
        resultChart.getData().add(new PieChart.Data("Correct", score));
        resultChart.getData().add(new PieChart.Data("Wrong", total - score));

        // Show questions with correct/wrong highlights
        for (QuestionController qc : questionControllers) {
            reviewLayout.getChildren().add(qc.getRootNode()); // qc.getRootNode() = UI of the question
        }
    }

    private String formatTime(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @FXML
    private void handleFinish() {
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
}
