package com.app.studysnap.services;

import com.app.studysnap.Main;
import com.app.studysnap.exceptions.AppException;
import com.app.studysnap.exceptions.ResourceNotFoundException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

/**
 * Simple navigation helper for switching scenes or replacing the
 * dashboard content area with another FXML view.
 * <p>
 * This utility centralizes the usual JavaFX navigation boilerplate and
 * reports failures through app popups.
 * </p>
 */
public class Navigator {

    /**
     * Not instantiable.
     */
    private Navigator() {
        throw new AssertionError("No instances");
    }

    /**
     * Replaces the root of the current window with the view from the given FXML.
     * If the {@link Stage} has no scene yet, a new {@link Scene} is created.
     * @param source any node on the target window (used to resolve its stage)
     * @param fxmlFile FXML file name/path relative to {@link Main} resources
     */
    public static void goTo(Node source, String fxmlFile) {
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

    /**
     * Loads an FXML view relative to {@link Main}’s resource root.
     * @param fxml relative resource path (e.g. {@code "home.fxml"})
     * @return the loaded {@link Parent}
     * @throws ResourceNotFoundException if the FXML resource cannot be located
     * @throws AppException if the FXML fails to load
     */
    private static Parent loadView(String fxml) {
        try {
            URL url = Main.class.getResource(fxml);
            if (url == null) {
                throw new ResourceNotFoundException("FXML not found: " + fxml);
            }
            return FXMLLoader.load(url);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException("Error loading FXML '" + fxml + "': " + e.getMessage(), e);
        }
    }

    /**
     * Replaces the dashboard center content (a {@link StackPane} with fx:id {@code contentArea})
     * with a view loaded from an FXML file.
     * @param anyChildOnScene any node that lives in the dashboard scene
     * @param fxml relative FXML path to load
     */
    public static void showInDashboard(Node anyChildOnScene, String fxml) {
        try {
            if (anyChildOnScene == null || anyChildOnScene.getScene() == null) {
                throw new AppException("Node is not attached to a Scene.");
            }
            var root = anyChildOnScene.getScene().getRoot();
            if (!(root instanceof BorderPane dashRoot)) {
                throw new AppException("Root is not a BorderPane (dashboard).");
            }
            StackPane contentArea = (StackPane) dashRoot.lookup("#contentArea");
            if (contentArea == null) {
                throw new ResourceNotFoundException("contentArea not found. Ensure id=\"contentArea\" in dashboard.fxml.");
            }
            Parent view = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource(fxml),
                    "FXML not found: " + fxml));
            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to open " + fxml + ":\n" + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    /**
     * Replaces the dashboard center content (a {@link StackPane} with fx:id {@code contentArea})
     * with an already loaded {@link Parent}.
     * @param anyChildOnScene any node that lives in the dashboard scene
     * @param view a preloaded view to inject
     * @throws AppException if the scene/root type is unexpected
     * @throws ResourceNotFoundException if the {@code contentArea} node is missing
     */
    public static void showInDashboard(Node anyChildOnScene, Parent view) {
        if (anyChildOnScene == null || anyChildOnScene.getScene() == null) {
            throw new AppException("Node is not attached to a Scene.");
        }
        var root = anyChildOnScene.getScene().getRoot();
        if (!(root instanceof BorderPane dashRoot)) {
            throw new AppException("Root is not a BorderPane (dashboard).");
        }
        StackPane contentArea = (StackPane) dashRoot.lookup("#contentArea");
        if (contentArea == null) {
            throw new ResourceNotFoundException("contentArea not found. Ensure id=\"contentArea\" in dashboard.fxml.");
        }
        contentArea.getChildren().setAll(view);
    }
}