package com.app.studysnap.controllers;

import com.app.studysnap.services.Navigator;
import com.app.studysnap.auth.AuthService;
import com.app.studysnap.model.SqliteUserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class ResetPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

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
            messageLabel.setText("Passwords do not match.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            authService.resetPassword(email, newPass);

            // Redirect straight to login after successful reset
            Navigator.goTo(messageLabel, "login.fxml");

        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
