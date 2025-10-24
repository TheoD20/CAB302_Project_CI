package com.app.studysnap.controllers;
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

/**
 * Controller for the login view.
 * <p>
 * Handles both email/password login and Google Sign-In. On successful authentication,
 * sets the current session user and navigates to the dashboard.
 * </p>
 */
public class LoginController {

    // Fxml
    @FXML private Button googleLoginButton;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink toSignupLink;
    @FXML private Hyperlink forgotPassLink;

    /**
     * Auth service with SQLite DAO for user operations.
     */
    private final AuthService auth = new AuthService(new SqliteUserDAO());

    /**
     * Default constructor:
     * Creates a new {@code LoginController}.
     */
    public LoginController() {}

    /**
     * Attempts an email/password login. On success, navigates to {@code dashboard.fxml}.
     * @throws IOException if navigation fails
     */
    @FXML
    private void handleLogin() throws IOException {
        try {
            var user = auth.loginWithEmail(emailField.getText(), passwordField.getText());
            Session.setCurrentUser(user);
            Navigator.goTo(loginButton, "dashboard.fxml");
        } catch (ValidationException | AuthenticationException ex) {
            String message = ex.getMessage();
            Popup.error(message);
            if (Objects.equals(message, "This account uses Google Sign-In. Use 'Sign in with Google'.")) {
                handleGoogleLogin();
            }
        } catch (Exception ex) {
            Popup.error("Unexpected error. Please try again.");
        }
    }

    /**
     * Performs Google Sign-In and logs the user in.
     * On success, navigates to {@code dashboard.fxml}.
     * @throws IOException if navigation fails
     */
    @FXML
    private void handleGoogleLogin() throws IOException {
        try {
            GoogleAuthService googleAuth = new GoogleAuthService();
            var userInfo = googleAuth.login();
            User u = auth.loginWithGoogle(userInfo.getId(), userInfo.getEmail(), userInfo.getName());
            Session.setCurrentUser(u);
            Popup.info("Welcome, " + u.getUsername());
            Navigator.goTo(googleLoginButton, "dashboard.fxml");
        } catch (ValidationException | AuthenticationException ex) {
            String message = ex.getMessage();
            Popup.error(message);
            if (Objects.equals(message, "An account with this email uses a password. Use email login.")) {
                Navigator.goTo(googleLoginButton, "login.fxml");
            }
        } catch (Exception e) {
            Popup.error("Google login failed: " + e.getMessage());
        }
    }

    /**
     * Navigates to the signup view.
     * @throws IOException if navigation fails
     */
    @FXML
    private void goToSignup() throws IOException {
        Navigator.goTo(toSignupLink, "signup.fxml");
    }

    /**
     * Navigates to the password reset view.
     * @throws IOException if navigation fails
     */
    @FXML
    private void goToPassReset() throws IOException {
        Navigator.goTo(forgotPassLink, "resetPassword.fxml");
    }
}