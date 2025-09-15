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

    public void setResult(int score, int total, List<QuestionController> questionControllers) {
        // Show score
        scoreLabel.setText(score + "/" + total);

        // Donut chart (correct vs wrong)
        resultChart.getData().clear();
        resultChart.getData().add(new PieChart.Data("Correct", score));
        resultChart.getData().add(new PieChart.Data("Wrong", total - score));

        // Show questions with correct/wrong highlights
        for (QuestionController qc : questionControllers) {
            reviewLayout.getChildren().add(qc.getRootNode()); // qc.getRootNode() = UI of the question
        }
    }

    @FXML
    private void handleFinish() {
        try {
            // Load the main dashboard/home page
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("home.fxml"));
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
