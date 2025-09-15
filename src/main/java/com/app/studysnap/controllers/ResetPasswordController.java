package com.app.studysnap.controllers;

import com.app.studysnap.services.Navigator;
import com.app.studysnap.auth.AuthService;
import com.app.studysnap.model.SqliteUserDAO;
import com.app.studysnap.services.Popup;
import javafx.fxml.FXML;
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
    private void handleResetPassword() throws IOException {
        String email = emailField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!newPass.equals(confirmPass)) {
            Popup.error("Passwords do not match.");
            return;
        }

        try {
            authService.resetPassword(email, newPass);

            Popup.info("Password reset successfully! Please log in with your new password.");

            // Redirect straight to login after successful reset
            Navigator.goTo(emailField, "login.fxml");

        } catch (IllegalArgumentException e) {
            Popup.error(e.getMessage());
        }
    }

    @FXML
    private void handleBack() throws IOException {
        Navigator.goTo(emailField, "login.fxml");
    }
}
