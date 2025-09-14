package com.app.studysnap;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Navigator {

    // Navigate to different fxml
    public static void goTo(Node source, String fxmlFile) throws IOException {
        Stage stage = (Stage) source.getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlFile));
        Scene scene = new Scene(fxmlLoader.load(), Main.WIDTH, Main.HEIGHT);
        stage.setScene(scene);
    }

    // Navigate to fxml into the Dashboard content area
    public static void showInDashboard(Node anyChildOnScene, String fxml) {
        try {
            if (anyChildOnScene == null || anyChildOnScene.getScene() == null) {
                throw new IllegalStateException("Node is not attached to a Scene.");
            }
            var root = anyChildOnScene.getScene().getRoot();
            if (!(root instanceof BorderPane dashRoot)) {
                throw new IllegalStateException("Root is not a BorderPane (dashboard).");
            }
            StackPane contentArea = (StackPane) dashRoot.lookup("#contentArea");
            if (contentArea == null) {
                throw new IllegalStateException("contentArea not found. Ensure id=\"contentArea\" in dashboard.fxml.");
            }
            Parent view = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource(fxml)));
            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to open " + fxml + ":\n" + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}