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

public class DashboardController {

    @FXML private StackPane contentArea;
    private static String pendingCenterFxml;

    @FXML
    private void initialize() {
        String target = (pendingCenterFxml != null) ? pendingCenterFxml : "home.fxml";
        pendingCenterFxml = null;
        safeLoadCenter(target);
    }

    // Sidebar navigation
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

    // Call to set up an alternative window to open in initialization rather than home.fxml
    public static void openOn(String centerFxml) {
        pendingCenterFxml = centerFxml;
    }

    // Logout btn click
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Session.clear();
            Navigator.goTo((Node) event.getSource(), "login.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Update dashboard center content
    private void safeLoadCenter(String fxmlName) {
        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource(fxmlName)));
            contentArea.getChildren().setAll(view);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
