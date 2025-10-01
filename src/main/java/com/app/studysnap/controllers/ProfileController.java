package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.model.*;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.services.Popup;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ProfileController {

    @FXML
    private Label displayName;
    @FXML
    private Label providerLabel;
    @FXML
    private Label providerValue;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label decksCount, quizzesCount, correctCount, streakCount;
    @FXML
    private PieChart accuracyChart;
    @FXML
    private BarChart<String, Number> weeklyActivityChart;
    @FXML
    private LineChart<String, Number> streakLineChart;
    @FXML
    private StackedBarChart<String, Number> decksByTopicChart;
    @FXML
    private FlowPane badgesGrid;
    @FXML
    private Button changePasswordButton;

    private IUserDAO userDAO;
    private IQuizDAO quizDAO;
    private List<Quiz> myQuizzes;
    private User currentUser;

    @FXML
    private void initialize() {
        try {
            userDAO = new SqliteUserDAO();
        } catch (Throwable t) {
            userDAO = null;
        }
        try {
            quizDAO = new SqliteQuizDAO();
        } catch (Throwable t) {
            quizDAO = null;
        }

        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            setStatus("No session found. Please log in again.");
            disableForm(true);
            return;
        }

        // Load user quizzes
        try {
            if (quizDAO != null) {
                myQuizzes = quizDAO.getQuizzesByUser(currentUser.getUserId());
            } else {
                myQuizzes = Collections.emptyList();
                setStatus("Quiz service unavailable. Some actions may be limited.");
            }
        } catch (Exception e) {
            myQuizzes = Collections.emptyList();
            setStatus("Couldn’t load your quizzes right now.");
        }

        // Prefill
        usernameField.setText(safe(currentUser.getUsername()));
        emailField.setText(safe(currentUser.getEmail()));

        String provider = safe(currentUser.getAuthProvider()).isBlank() ? "LOCAL" : currentUser.getAuthProvider();
        providerValue.setText(provider);
        providerLabel.setText(provider.equalsIgnoreCase("GOOGLE") ? "Google account" : "Local account");
        displayName.setText(safe(currentUser.getUsername()).isBlank() ? "User" : currentUser.getUsername());

        // Para GOOGLE, email sólo lectura
        emailField.setEditable(!provider.equalsIgnoreCase("GOOGLE"));

        // Habilitar guardar sólo si hay cambios válidos
        saveButton.setDisable(true);
        usernameField.textProperty().addListener((obs, a, b) -> validateDirty());
        emailField.textProperty().addListener((obs, a, b) -> validateDirty());

        // Disable change-password for Google accounts
        boolean isGoogle = "GOOGLE".equalsIgnoreCase(providerValue.getText());
        changePasswordButton.setDisable(isGoogle);

        //TODO: change default values for DB fetching
        int decks = 12;
        int quizzes = 31;
        int correct = 211;
        int streak = 5;
        decksCount.setText(String.valueOf(decks));
        quizzesCount.setText(String.valueOf(quizzes));
        correctCount.setText(String.valueOf(correct));
        streakCount.setText(String.valueOf(streak));
        setupAccuracyChart(correct, Math.max(0, quizzes * 10 - correct)); // placeholder total answers
        setupWeeklyActivityChart();
        setupStreakLineChart();
        setupDecksByTopicChart();
        renderBadges();
    }

    private void validateDirty() {
        if (currentUser == null) {
            saveButton.setDisable(true);
            return;
        }

        String newUsername = safe(usernameField.getText());
        String newEmail = safe(emailField.getText());

        boolean changed = !newUsername.equals(safe(currentUser.getUsername()))
                || !newEmail.equals(safe(currentUser.getEmail()));

        boolean valid = validateUsername(newUsername)
                && (emailField.isEditable() ? validateEmail(newEmail) : true);

        saveButton.setDisable(!(changed && valid));
        statusLabel.setText(changed && !valid ? "Fix validation errors to continue." : "");
    }

    @FXML
    private void handleSave() {
        if (currentUser == null || userDAO == null) {
            setStatus("Internal error. Try again.");
            return;
        }

        String newUsername = safe(usernameField.getText());
        String newEmail = safe(emailField.getText());

        // Validaciones
        if (!validateUsername(newUsername)) {
            setStatus("Username must be 3–24 characters.");
            return;
        }
        if (emailField.isEditable() && !validateEmail(newEmail)) {
            setStatus("Enter a valid email.");
            return;
        }

        // Si es GOOGLE, no permitir cambiar el email (ya está deshabilitado, de todas formas normalizamos)
        if (!emailField.isEditable()) {
            newEmail = safe(currentUser.getEmail());
        }

        // Actualizar modelo
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);

        // Persistir
        boolean ok = true;
        try {
            userDAO.updateUser(currentUser);   // <= sin asignación
        } catch (Exception ex) {
            ex.printStackTrace();
            ok = false;
        }

        if (ok) {
            Session.setCurrentUser(currentUser);
            displayName.setText(currentUser.getUsername());
            setStatus("Profile saved ✓");
            saveButton.setDisable(true);
        } else {
            setStatus("Could not save changes.");
        }
    }

    @FXML
    private void handleDeleteProfile() {
        if (currentUser == null || userDAO == null || quizDAO == null) {
            setStatus("Internal error. Try again.");
            return;
        }

        if (!Popup.confirm(
                "Delete Profile?",
                "All your quizzes and information will be deleted.\nThis cannot be undone.\n\nDo you want to proceed?")) {
            return;
        }

        if (deleteButton != null) deleteButton.setDisable(true);

        try {
            if (quizDAO != null) {
                for (Quiz q : myQuizzes) {
                    if (q != null) {
                        quizDAO.deleteQuiz(q.getQuizId());
                    }
                }
            }

            userDAO.deleteUser(currentUser.getUserId());
            if (myQuizzes != null) myQuizzes.clear();
            currentUser = null;
            Session.clear();

            Popup.info("Your profile has been deleted successfully.");
            Navigator.goTo(deleteButton, "login.fxml");

        } catch (Exception ex) {
            Popup.error("Failed to delete profile: " + ex.getMessage());
            setStatus("Could not delete profile.");
        } finally {
            if (deleteButton != null) deleteButton.setDisable(false);
        }
    }

    @FXML
    private void handleChangePassword() throws IOException {
        Navigator.goTo(changePasswordButton, "resetPassword.fxml");
    }

    @FXML
    private void handleSeeAllBadges() {
        // Open a simple window listing all possible badges
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("All Badges");
        dlg.setHeaderText("Browse all available badges");
        FlowPane grid = new FlowPane(12, 12);
        grid.setPrefWrapLength(480);

        //TODO: Inject badges
    }

    // Progress Section:
    // TODO: Wire up real data

    private void setupAccuracyChart(int correct, int incorrect) {
        accuracyChart.getData().clear();
        accuracyChart.getData().addAll(
                new PieChart.Data("Correct", correct),
                new PieChart.Data("Incorrect", Math.max(incorrect, 0))
        );
        accuracyChart.setLegendVisible(false);
    }

    private void setupWeeklyActivityChart() {
        weeklyActivityChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Quizzes");
        // TODO: Replace with last-7-days actual
        s.getData().add(new XYChart.Data<>("Mon", 2));
        s.getData().add(new XYChart.Data<>("Tue", 4));
        s.getData().add(new XYChart.Data<>("Wed", 1));
        s.getData().add(new XYChart.Data<>("Thu", 3));
        s.getData().add(new XYChart.Data<>("Fri", 5));
        s.getData().add(new XYChart.Data<>("Sat", 2));
        s.getData().add(new XYChart.Data<>("Sun", 6));
        weeklyActivityChart.getData().add(s);
    }

    private void setupStreakLineChart() {
        streakLineChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Streak");
        // TODO: Replace with real streak series
        s.getData().add(new XYChart.Data<>("W-4", 1));
        s.getData().add(new XYChart.Data<>("W-3", 2));
        s.getData().add(new XYChart.Data<>("W-2", 4));
        s.getData().add(new XYChart.Data<>("W-1", 3));
        s.getData().add(new XYChart.Data<>("Now", 5));
        streakLineChart.getData().add(s);
    }

    private void setupDecksByTopicChart() {
        decksByTopicChart.getData().clear();
        XYChart.Series<String, Number> math = new XYChart.Series<>();
        math.setName("Math");
        math.getData().add(new XYChart.Data<>("Algebra", 3));
        math.getData().add(new XYChart.Data<>("Calculus", 2));

        XYChart.Series<String, Number> cs = new XYChart.Series<>();
        cs.setName("CS");
        cs.getData().add(new XYChart.Data<>("OOP", 4));
        cs.getData().add(new XYChart.Data<>("Networks", 1));

        decksByTopicChart.getData().addAll(math, cs);
    }

    private void renderBadges() {
        badgesGrid.getChildren().clear();

        //TODO: get all badges from user and display
    }

    /* ------------ helpers ------------ */
    private void setStatus(String msg) { statusLabel.setText(msg == null ? "" : msg); }
    private void disableForm(boolean b) {
        usernameField.setDisable(b);
        emailField.setDisable(b);
        saveButton.setDisable(b);
    }
    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static boolean validateUsername(String s) { return s != null && s.trim().length() >= 3 && s.trim().length() <= 24; }
    private static boolean validateEmail(String s) {
        if (s == null) return false;
        String v = s.trim().toLowerCase();
        return v.contains("@") && v.indexOf('@') > 0 && v.indexOf('@') < v.length() - 3 && v.contains(".");
    }
}
