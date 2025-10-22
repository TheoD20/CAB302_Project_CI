package com.app.studysnap.controllers;

import com.app.studysnap.exceptions.AlreadyExistsException;
import com.app.studysnap.exceptions.AuthenticationException;
import com.app.studysnap.exceptions.ExternalServiceException;
import com.app.studysnap.exceptions.ValidationException;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.services.Popup;
import com.app.studysnap.auth.AuthService;
import com.app.studysnap.auth.GoogleAuthService;
import com.app.studysnap.auth.Session;
import com.app.studysnap.model.SqliteUserDAO;

import com.app.studysnap.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.Objects;

import static com.app.studysnap.services.TextParser.isBlank;

/**
 * Controller for the signup page.
 * <p>
 * Supports both email/password sign-up and Google Sign-In. On success, the user is
 * authenticated and redirected to the dashboard.
 * </p>
 */
public class SignupController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button googleSignButton;
    @FXML private Button signButton;
    @FXML private Hyperlink toLoginLink;

    /**
     * Authentication service backed by a SQLite DAO.
     */
    private final AuthService auth = new AuthService(new SqliteUserDAO());

    /**
     * Handles email/password sign-up. Validates password confirmation, registers the user,
     * logs them in, and navigates to the dashboard.
     */
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

        } catch (ValidationException | AlreadyExistsException | AuthenticationException ex) {
            Popup.error(ex.getMessage());
        } catch (Exception ex) {
            Popup.error("Unexpected error. Please try again.");
        }
    }

    /**
     * Handles Google Sign-In flow. After Google auth, provisions the account if needed,
     * logs the user in, and navigates to the dashboard.
     * @throws IOException if navigation fails
     */
    @FXML
    private void handleGoogleSignup() throws IOException {
        try {
            GoogleAuthService googleAuth = new GoogleAuthService();
            var userInfo = googleAuth.login();

            String name = userInfo.getName();
            if (isBlank(name)) name = userInfo.getEmail().split("@")[0];

            auth.registerGoogleUser(name, userInfo.getEmail(), userInfo.getId());

            User u = auth.loginWithGoogle(userInfo.getId(), userInfo.getEmail(), userInfo.getName());
            Session.setCurrentUser(u);
            Popup.info("Welcome, " + u.getUsername());
            Navigator.goTo(googleSignButton, "dashboard.fxml");

        } catch (ValidationException | AlreadyExistsException | AuthenticationException ex) {
            String message = ex.getMessage();
            Popup.error(message);
            if (Objects.equals(message, "An account with this email uses a password. Use email login.")) {
                goToLogin();
            }
        } catch (ExternalServiceException e) {
            Popup.error("Google external service error: " + e.getMessage());
        } catch (Exception e) {
            Popup.error("Google signup failed: " + e.getMessage());
        }
    }

    /**
     * Navigates to the login screen.
     * @throws IOException if navigation fails
     */
    @FXML
    private void goToLogin() throws IOException {
        Navigator.goTo(toLoginLink, "login.fxml");
    }
}
