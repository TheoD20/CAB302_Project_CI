package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.auth.Session;
import com.app.studysnap.services.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;

import java.util.Objects;

/**
 * JavaFX controller for the main dashboard view.
 */
public class DashboardController {

    // Fxml
    @FXML private StackPane contentArea;

    /** Store fxml to inject on the {@code contentArea}. */
    private static String pendingCenterFxml;

    /**
     * Default constructor:
     * Creates a new {@code DashboardController}.
     */
    public DashboardController() {}

    /**
     * JavaFX initialize method.
     * <p>
     * Loads the requested center content into {@link #contentArea}. If no pending
     * content was specified, {@code home.fxml} is loaded by default.
     * </p>
     */
    @FXML
    private void initialize() {
        String target = (pendingCenterFxml != null) ? pendingCenterFxml : "home.fxml";
        pendingCenterFxml = null;
        safeLoadCenter(target);
    }

    /**
     * Handles sidebar navigation button clicks.
     * <p>
     * Expects each {@link Button} to have its target FXML filename set as {@code userData}.
     * When clicked, the corresponding view is loaded into {@link #contentArea}.
     * </p>
     * @param event the action event triggered by clicking on specific button
     */
    @FXML
    private void handleNav(ActionEvent event) {
        Object src = event.getSource();
        if (src instanceof Button btn) {
            Object ud = btn.getUserData();
            if (ud != null) {
                safeLoadCenter(ud.toString());
            }
        }
    }

    /**
     * set specific FXML to open on initialization.
     *
     * @param centerFxml the FXML resource name (e.g., {@code "profile.fxml"}) to open first
     */
    public static void openOn(String centerFxml) {
        pendingCenterFxml = centerFxml;
    }

    /**
     * Handles the logout action: clears the current session and navigates to {@code login.fxml}.
     * @param event the action event triggered by clicking the logout button
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Session.clear();
            Navigator.goTo((Node) event.getSource(), "login.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Loads an FXML view by name and replaces the contents of {@link #contentArea}.
     * Any load failure is caught and logged via stack trace.
     * @param fxmlName the resource name of the FXML to load (e.g., {@code "home.fxml"})
     */
    private void safeLoadCenter(String fxmlName) {
        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource(fxmlName)));
            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
