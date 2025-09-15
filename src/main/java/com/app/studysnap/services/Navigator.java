package com.app.studysnap.services;

import com.app.studysnap.Main;
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
import java.net.URL;
import java.util.Objects;

public class Navigator {
    private Navigator() {}

    // Navigate to different fxml
    public static void goTo(Node source, String fxmlFile) throws IOException {
        try {
            Stage stage = (Stage) source.getScene().getWindow();
            Parent view = loadView(fxmlFile);

            Scene scene = stage.getScene();
            if (scene == null) {
                stage.setScene(new Scene(view, Main.WIDTH, Main.HEIGHT));
            } else {
                scene.setRoot(view);
            }
        } catch (Exception ex) {
            Popup.error("Failed to open " + fxmlFile + ":\n" + ex.getMessage());
        }
    }

    private static Parent loadView(String fxml) throws Exception {
        URL url = Objects.requireNonNull(Main.class.getResource(fxml), "FXML not found: " + fxml);
        return FXMLLoader.load(url);
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

    // Overload to load already loaded scene in dashboard
    public static void showInDashboard(Node anyChildOnScene, Parent view) {
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
        contentArea.getChildren().setAll(view);
    }
}