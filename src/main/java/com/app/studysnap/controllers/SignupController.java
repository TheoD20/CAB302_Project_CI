package com.app.studysnap.controllers;

import com.app.studysnap.Navigator;
import com.app.studysnap.Popup;
import com.app.studysnap.auth.AuthService;
import com.app.studysnap.auth.GoogleAuthService;
import com.app.studysnap.auth.Session;
import com.app.studysnap.model.SqliteUserDAO;

import com.app.studysnap.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Objects;

public class SignupController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button googleSignButton;
    @FXML private Button signButton;
    @FXML private Hyperlink toLoginLink;

    private final AuthService auth = new AuthService(new SqliteUserDAO());

    @FXML
    private void handleSignup() {
        try {
            String username = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!password.equals(confirmPassword)) {
                Popup.warn("Passwords do not match.");
                return; // stop signup
            }

            auth.register(username, email, password);
            var user = auth.loginWithEmail(email, password);
            Session.setCurrentUser(user);
            Navigator.goTo(signButton, "dashboard.fxml");

        } catch (IllegalArgumentException ex) {
            Popup.error(ex.getMessage());
        } catch (Exception ex) {
            Popup.error("Unexpected error. Please try again.");
        }
    }

    @FXML
    private void handleGoogleSignup() throws IOException {
        try {
            GoogleAuthService googleAuth = new GoogleAuthService();
            var userInfo = googleAuth.login();

            String name = userInfo.getName();
            if (name == null || name.isBlank()) name = userInfo.getEmail().split("@")[0];

            auth.registerGoogleUser(name, userInfo.getEmail(), userInfo.getId());

            User u = auth.loginWithGoogle(userInfo.getId(), userInfo.getEmail(), userInfo.getName());
            Session.setCurrentUser(u);
            Popup.info("Welcome, " + u.getUsername());
            Navigator.goTo(googleSignButton, "dashboard.fxml");

        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage();
            Popup.error(message);
            if (Objects.equals(message, "An account with this email uses a password. Use email login.")) {
                goToLogin();
            }
        } catch (Exception e) {
            Popup.error("Google signup failed: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() throws IOException {
        Navigator.goTo(toLoginLink, "login.fxml");
    }
}
