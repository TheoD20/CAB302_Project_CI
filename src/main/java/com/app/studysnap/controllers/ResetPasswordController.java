package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.auth.AuthService;
import com.app.studysnap.model.SqliteUserDAO;
import com.app.studysnap.services.Popup;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;

public class ResetPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final AuthService authService;

    public ResetPasswordController() {
        this.authService = new AuthService(new SqliteUserDAO());
    }

    @FXML
    private void initialize() {
        // If we have a session, hide the email input and use the session user's email
        var user = Session.getCurrentUser();
        if (user != null) {
            String email = user.getEmail() == null ? "" : user.getEmail();
            emailField.setText(email);
            hide(emailField);

            // If this is a Google account, no password changes
            String provider = user.getAuthProvider();
            if (provider != null && provider.equalsIgnoreCase("GOOGLE")) {
                disablePasswordChangeForGoogle();
            }
        }
    }

    private void hide(Node n) {
        if (n != null) {
            n.setVisible(false);
            n.setManaged(false);
        }
    }

    private void disablePasswordChangeForGoogle() {
        newPasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
        Popup.info("This account uses Google sign-in. Password changes are managed with Google.");
    }
    @FXML
    private void handleResetPassword() throws IOException {
        var user = Session.getCurrentUser();

        String email = (user != null && user.getEmail() != null && !user.getEmail().isBlank())
                ? user.getEmail()
                : emailField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!newPass.equals(confirmPass)) {
            Popup.error("Passwords do not match.");
            return;
        }

        if (user != null && "GOOGLE".equalsIgnoreCase(String.valueOf(user.getAuthProvider()))) {
            Popup.error("This account uses Google sign-in. Change your password via your Google account.");
            return;
        }

        try {
            authService.resetPassword(email, newPass);

            if (user != null) {
                Popup.info("Password reset successfully!");

                DashboardController.openOn("profile.fxml");
                Navigator.goTo(emailField, "dashboard.fxml");
            } else {
                Popup.info("Password reset successfully! Please log in with your new password.");

                // Redirect straight to login after successful reset
                Navigator.goTo(emailField, "login.fxml");
            }

        } catch (IllegalArgumentException e) {
            Popup.error(e.getMessage());
        }
    }

    @FXML
    private void handleBack() throws IOException {
        if (Session.getCurrentUser() != null) {
            DashboardController.openOn("profile.fxml");
            Navigator.goTo(emailField, "dashboard.fxml");
        }
        else {
            Navigator.goTo(emailField, "login.fxml");
        }
    }
}
