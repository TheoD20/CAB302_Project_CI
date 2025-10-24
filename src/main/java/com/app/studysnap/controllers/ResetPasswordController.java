package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.exceptions.AuthenticationException;
import com.app.studysnap.exceptions.ResourceNotFoundException;
import com.app.studysnap.exceptions.ValidationException;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.auth.AuthService;
import com.app.studysnap.model.SqliteUserDAO;
import com.app.studysnap.services.Popup;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.IOException;

import static com.app.studysnap.services.TextParser.isBlank;

/**
 * Controller for the password reset view.
 * <p>
 * If a user is already signed in, the email field is hidden and their session email is used.
 * Google-linked accounts cannot reset passwords here and are directed to manage them via Google.
 * </p>
 */
public class ResetPasswordController {

    // Fxml
    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    /**
     * Authentication service backed by a SQLite DAO.
     */
    private final AuthService authService;

    /**
     * Creates a new {@code ResetPasswordController} and initializes dependencies.
     */
    public ResetPasswordController() {
        this.authService = new AuthService(new SqliteUserDAO());
    }

    /**
     * JavaFX initialization: fill or lock fields based on current user.
     * Hides the email input if a session user exists; disables password change for Google accounts.
     */
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

    /**
     * Helper to hide a node from layout/visibility.
     * @param n node to hide
     */
    private void hide(Node n) {
        if (n != null) {
            n.setVisible(false);
            n.setManaged(false);
        }
    }

    /**
     * Disables password fields and informs the user that Google-managed accounts
     * cannot change passwords here.
     */
    private void disablePasswordChangeForGoogle() {
        newPasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
        Popup.info("This account uses Google sign-in. Password changes are managed with Google.");
    }

    /**
     * Attempts to reset the user's password.
     * <ul>
     *   <li>When signed in, uses the session email; otherwise uses the email field.</li>
     *   <li>Validates password confirmation locally.</li>
     *   <li>Handles service errors and navigates appropriately on success.</li>
     * </ul>
     *
     * @throws IOException if navigation fails after a successful reset
     */
    @FXML
    private void handleResetPassword() throws IOException {
        var user = Session.getCurrentUser();

        String email = (user != null && !isBlank(user.getEmail()))
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

        } catch (ResourceNotFoundException | AuthenticationException | ValidationException e) {
            Popup.error(e.getMessage());
        }
    }

    /**
     * Navigates back to the previous logical screen:
     * dashboard (profile tab) if signed in, otherwise login.
     *
     * @throws IOException if navigation fails
     */
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
