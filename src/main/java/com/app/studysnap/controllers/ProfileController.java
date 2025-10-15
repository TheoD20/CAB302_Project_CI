package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.auth.Session;
import com.app.studysnap.model.*;
import com.app.studysnap.services.AvatarService;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.services.Popup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.app.studysnap.services.BadgeRenderer;

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
    private Label decksCount, quizzesCount, correctCount, streakCount, bestStreakCount;
    @FXML
    private GridPane ChartsArea;
    @FXML
    private VBox chartsEmptyState;
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
    @FXML
    private ImageView avatarView;
    @FXML
    private Button deleteAvatarBtn;


    private IUserDAO userDAO;
    private IQuizDAO quizDAO;
    private IAttemptDAO attemptDAO;
    private IBadgeDAO badgeDAO;
    private IBadgeProgressDAO badgeProgressDAO;

    private List<Quiz> myQuizzes;
    private List<Badge> Badges;
    private List<Badge> CompletedBadges;
    private User currentUser;

    private final AvatarService avatars = new AvatarService();
    private static final double AVATAR_SIZE = 96.0;
    private Image defaultAvatar;

    @FXML
    private void initialize() {

        // Initialize DAOs
        try { userDAO = new SqliteUserDAO(); } catch (Throwable t) { userDAO = null; }
        try { quizDAO = new SqliteQuizDAO(); } catch (Throwable t) { quizDAO = null; }
        try { attemptDAO = new SqliteAttemptDAO(); } catch (Throwable t) { attemptDAO = null; }
        try { badgeDAO = new SqliteBadgeDAO(); } catch (Throwable t) { badgeDAO = null; }
        try { badgeProgressDAO = new SqliteBadgeProgressDAO(); } catch (Throwable t) { badgeProgressDAO = null; }

        // Get current user
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

        // Profile info
        usernameField.setText(safe(currentUser.getUsername()));
        emailField.setText(safe(currentUser.getEmail()));

        String provider = safe(currentUser.getAuthProvider()).isBlank() ? "LOCAL" : currentUser.getAuthProvider();
        providerValue.setText(provider);
        providerLabel.setText(provider.equalsIgnoreCase("GOOGLE") ? "Google account" : "Local account");
        displayName.setText(safe(currentUser.getUsername()).isBlank() ? "User" : currentUser.getUsername());

        // block email change for Google account
        emailField.setEditable(!provider.equalsIgnoreCase("GOOGLE"));

        // handle disable save btn
        saveButton.setDisable(true);
        usernameField.textProperty().addListener((obs, a, b) -> validateDirty());
        emailField.textProperty().addListener((obs, a, b) -> validateDirty());

        // Disable change-password for Google accounts
        boolean isGoogle = "GOOGLE".equalsIgnoreCase(providerValue.getText());
        changePasswordButton.setDisable(isGoogle);

        // Load avatar image
        defaultAvatar = avatars.loadDefaultAvatar(AVATAR_SIZE);
        avatars.applyUserAvatarOrDefault(avatarView, currentUser, AVATAR_SIZE, defaultAvatar);
        updateAvatarButtons();

        // Get all users attempts
        List<Attempt> userAttempts = attemptDAO.getAttemptsByUser(currentUser.getUserId());

        int decks = quizDAO.getQuizzesByUser(currentUser.getUserId()).size();
        int attempts = userAttempts.size();
        int correct = attemptDAO.getCorrectAnswersByUser(currentUser.getUserId());
        int streak = attemptDAO.getCurrentStreakByUser(currentUser.getUserId());
        int bestStreak = attemptDAO.getBestStreakByUser(currentUser.getUserId());
        decksCount.setText(String.valueOf(decks));
        quizzesCount.setText(String.valueOf(attempts));
        correctCount.setText(String.valueOf(correct));
        streakCount.setText(String.valueOf(streak));
        bestStreakCount.setText(String.valueOf(bestStreak));

        if(attempts == 0 && decks == 0) {
            ChartsArea.setVisible(false);
            ChartsArea.setManaged(false);

            // Show empty message
            chartsEmptyState.setVisible(true);
            chartsEmptyState.setManaged(true);
        } else {
            chartsEmptyState.setVisible(false);
            chartsEmptyState.setManaged(false);

            ChartsArea.setVisible(true);
            ChartsArea.setManaged(true);

            setupAccuracyChart(correct, Math.max(0, attempts * 10 - correct));
            setupWeeklyActivityChart();
            setupStreakLineChart();
            setupDecksByTopicChart();
        }

        // Load badges
        try {
            if (badgeDAO != null || badgeProgressDAO != null) {
                Badges = badgeDAO.getAllBadges();
                CompletedBadges = badgeProgressDAO.getCompletedBadgesByUser(currentUser.getUserId());
            } else {
                Badges = Collections.emptyList();
                CompletedBadges = Collections.emptyList();
                setStatus("Badges service unavailable. Some actions may be limited.");
            }
        } catch (Exception e) {
            Badges = Collections.emptyList();
            CompletedBadges = Collections.emptyList();
            setStatus("Couldn’t load badges right now.");
        }
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
                && (!emailField.isEditable() || validateEmail(newEmail));

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
            // delete quizzes
            if (myQuizzes != null) {
                for (Quiz q : myQuizzes) {
                    if (q != null) {
                        quizDAO.deleteQuiz(q.getQuizId());
                    }
                }
            }

            // delete avatar files
            try { avatars.deleteAvatar(currentUser); } catch (Exception ignore) {}

            // delete user
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
    private void handleUploadAvatar() {
        if (currentUser == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Avatar");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File chosen = fc.showOpenDialog(avatarView.getScene().getWindow());
        if (chosen == null) return;

        try {
            avatars.saveAvatar(currentUser, chosen);
            avatars.applyUserAvatarOrDefault(avatarView, currentUser, AVATAR_SIZE, defaultAvatar);
            setStatus("Avatar updated ✓");
            updateAvatarButtons();
        } catch (Exception e) {
            setStatus("Could not save avatar.");
        }
    }

    @FXML
    private void handleDeleteAvatar() {
        if (currentUser == null) return;
        if (!Popup.confirm("Remove avatar?", "Revert to the default icon.")) return;

        try {
            avatars.deleteAvatar(currentUser);
            avatars.applyUserAvatarOrDefault(avatarView, currentUser, AVATAR_SIZE, defaultAvatar);
            setStatus("Avatar removed ✓");
            updateAvatarButtons();
        } catch (Exception ex) {
            Popup.error("Couldn't remove avatar: " + ex.getMessage());
        }
    }

    // update buttons to add/delete avatar image
    @FXML
    private void updateAvatarButtons() {
        boolean hasCustom = avatars.findAvatarFile(currentUser).isPresent();
        if (deleteAvatarBtn != null) {
            deleteAvatarBtn.setVisible(hasCustom);
            deleteAvatarBtn.setManaged(hasCustom);
        }
    }


    @FXML
    private void handleSeeAllBadges() {
        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("badges.fxml")));

            Dialog<Void> dlg = new Dialog<>();
            dlg.setTitle("All Badges");
            dlg.getDialogPane().setContent(view);
            dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dlg.initOwner(displayName.getScene().getWindow());
            dlg.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback simple message
            new Alert(Alert.AlertType.ERROR, "Failed to open badges window.").showAndWait();
        }
    }

    // Progress Section:
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

        // Get date range
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        Map<LocalDate, Integer> counts = attemptDAO.getAttemptsByDateRange(currentUser.getUserId(), start, end);

        LocalDate d = start;
        while (!d.isAfter(end)) {
            String label = d.getDayOfWeek().name().substring(0, 3);
            s.getData().add(new XYChart.Data<>(label, counts.getOrDefault(d, 0)));
            d = d.plusDays(1);
        }

        weeklyActivityChart.getData().add(s);
    }

    private void setupStreakLineChart() {
        streakLineChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Streak");

        // User ID
        int userId = currentUser.getUserId();

        // Get date
        LocalDate today = LocalDate.now();

        // Define week endpoints (Sunday as end of the week)
        LocalDate thisWeekEnd = today.plusDays(7 - today.getDayOfWeek().getValue());
        if (today.getDayOfWeek().getValue() == 7) thisWeekEnd = today;

        // Labels + dates
        LocalDate w4 = thisWeekEnd.minusWeeks(4);
        LocalDate w3 = thisWeekEnd.minusWeeks(3);
        LocalDate w2 = thisWeekEnd.minusWeeks(2);
        LocalDate w1 = thisWeekEnd.minusWeeks(1);

        s.getData().add(new XYChart.Data<>("W-4", attemptDAO.getStreakAsOf(userId, w4)));
        s.getData().add(new XYChart.Data<>("W-3", attemptDAO.getStreakAsOf(userId, w3)));
        s.getData().add(new XYChart.Data<>("W-2", attemptDAO.getStreakAsOf(userId, w2)));
        s.getData().add(new XYChart.Data<>("W-1", attemptDAO.getStreakAsOf(userId, w1)));
        s.getData().add(new XYChart.Data<>("Now",  attemptDAO.getStreakAsOf(userId, today)));

        streakLineChart.getData().add(s);
    }

    private void setupDecksByTopicChart() {
        decksByTopicChart.getData().clear();

        int userId = currentUser.getUserId();
        Map<String, Integer> data = quizDAO.getDeckCountsByTopic(userId);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quizzes");

        for (Map.Entry<String, Integer> e : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        decksByTopicChart.getData().add(series);
    }

    private void renderBadges() {
        badgesGrid.getChildren().clear();

        if (CompletedBadges.isEmpty()) {
            Label empty = new Label("No badges achieved yet");
            empty.getStyleClass().add("empty-label");
            badgesGrid.getChildren().add(empty);
            return;
        }
        else {
            for (Badge b : CompletedBadges) {
                badgesGrid.getChildren().add(BadgeRenderer.buildCard(b, currentUser, badgeProgressDAO, false));
            }
        }
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
