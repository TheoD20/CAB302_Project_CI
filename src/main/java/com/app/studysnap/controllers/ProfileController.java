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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.app.studysnap.services.BadgeRenderer;
import static com.app.studysnap.auth.AuthService.validateEmail;
import static com.app.studysnap.auth.AuthService.validateUsername;
import static com.app.studysnap.services.TextParser.*;

/**
 * Controller for the user's profile page.
 * <p>
 * Loads and edits profile details (username/email), manages avatar, renders progress
 * charts, and displays earned badges. Persists changes via DAO services and uses
 * {@link Session} to access the signed-in user.
 * </p>
 */
public class ProfileController {

    // Fxml
    @FXML private Label displayName;
    @FXML private Label providerLabel, providerValue;
    @FXML private Label statusLabel;
    @FXML private TextField usernameField, emailField;
    @FXML private Button saveButton, deleteButton;
    @FXML private Label decksCount, quizzesCount, correctCount, streakCount, bestStreakCount;

    // Chart region and empty-state container
    @FXML private GridPane ChartsArea;
    @FXML private VBox chartsEmptyState;

    // Accuracy pie chart with overlay label placed in this container
    @FXML private PieChart accuracyChart;
    @FXML private StackPane accuracyChartContainer;
    @FXML private Label accuracyOverlay;

    // Activity, streak and topics charts
    @FXML private BarChart<String, Number> weeklyActivityChart;
    @FXML private LineChart<String, Number> streakLineChart;
    @FXML private StackedBarChart<String, Number> decksByTopicChart;

    // Badges flow grid.
    @FXML private FlowPane badgesGrid;

    // Password change, avatar image and delete button.
    @FXML private Button changePasswordButton;
    @FXML private ImageView avatarView;
    @FXML private Button deleteAvatarBtn;


    // DAOs
    private IUserDAO userDAO;
    private IQuizDAO quizDAO;
    private IAttemptDAO attemptDAO;
    private IBadgeProgressDAO badgeProgressDAO;

    // State
    private List<Quiz> myQuizzes;
    private List<Badge> CompletedBadges;
    private User currentUser;

    /** Service that loads and applies user avatars. */
    private final AvatarService avatars = new AvatarService();
    /** Avatar render size in pixels. */
    private static final double AVATAR_SIZE = 48.0;
    /** User default avatar image. */
    private Image defaultAvatar;

    /**
     * Default constructor:
     * Creates a new {@code ProfileController}.
     */
    public ProfileController() {}

    /**
     * JavaFX initialization: initializes DAOs, loads user/profile data, sets up charts and badges,
     * and applies the user's avatar.
     */
    @FXML
    private void initialize() {

        // Initialize DAOs
        try { userDAO = new SqliteUserDAO(); } catch (Throwable t) { userDAO = null; }
        try { quizDAO = new SqliteQuizDAO(); } catch (Throwable t) { quizDAO = null; }
        try { attemptDAO = new SqliteAttemptDAO(); } catch (Throwable t) { attemptDAO = null; }
        IBadgeDAO badgeDAO;
        try { badgeDAO = new SqliteBadgeDAO(); } catch (Throwable t) { badgeDAO = null; }
        try { badgeProgressDAO = new SqliteBadgeProgressDAO(); } catch (Throwable t) { badgeProgressDAO = null; }

        // Get current user
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            setStatus("No session found. Please log in again.");
            disableForm();
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
        usernameField.setText(trim(currentUser.getUsername()));
        emailField.setText(trim(currentUser.getEmail()));

        String provider = isBlank(trim(currentUser.getAuthProvider())) ? "LOCAL" : currentUser.getAuthProvider();
        providerValue.setText(provider);
        providerLabel.setText(provider.equalsIgnoreCase("GOOGLE") ? "Google account" : "Local account");
        displayName.setText(isBlank(trim(currentUser.getUsername())) ? "User" : currentUser.getUsername());

        // Block email change for Google account
        emailField.setEditable(!provider.equalsIgnoreCase("GOOGLE"));

        // Handle disable save btn
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

        if(attempts == 0) {
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
        List<Badge> badges;
        try {
            if (badgeDAO != null || badgeProgressDAO != null) {
                badges = badgeDAO.getAllBadges();
                CompletedBadges = badgeProgressDAO.getCompletedBadgesByUser(currentUser.getUserId());
            } else {
                badges = Collections.emptyList();
                CompletedBadges = Collections.emptyList();
                setStatus("Badges service unavailable. Some actions may be limited.");
            }
        } catch (Exception e) {
            badges = Collections.emptyList();
            CompletedBadges = Collections.emptyList();
            setStatus("Couldn’t load badges right now.");
        }
        renderBadges();
    }

    /**
     * Determines if changes in the profile form are valid.
     * Toggles save button and status accordingly.
     */
    private void validateDirty() {
        if (currentUser == null) {
            saveButton.setDisable(true);
            return;
        }

        String newUsername = trim(usernameField.getText());
        String newEmail = trim(emailField.getText());

        boolean changed = !newUsername.equals(trim(currentUser.getUsername()))
                || !newEmail.equals(trim(currentUser.getEmail()));

        boolean valid = validateUsername(newUsername)
                && (!emailField.isEditable() || validateEmail(newEmail));

        saveButton.setDisable(!(changed && valid));
        statusLabel.setText(changed && !valid ? "Fix validation errors to continue." : "");
    }

    /**
     * Saves profile changes (username/email) after validating, persists via DAO, and refreshes UI.
     */
    @FXML
    private void handleSave() {
        if (currentUser == null || userDAO == null) {
            setStatus("Internal error. Try again.");
            return;
        }

        String newUsername = trim(usernameField.getText());
        String newEmail = trim(emailField.getText());

        // Validations
        if (!validateUsername(newUsername)) {
            setStatus("Username must be 3–24 characters.");
            return;
        }
        if (emailField.isEditable() && !validateEmail(newEmail)) {
            setStatus("Enter a valid email.");
            return;
        }

        // If it's GOOGLE, don't allow email changes
        if (!emailField.isEditable()) {
            newEmail = trim(currentUser.getEmail());
        }

        // Update model
        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);

        // Persist
        boolean ok = true;
        try {
            userDAO.updateUser(currentUser);
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

    /**
     * Deletes the user profile and all associated content (after confirmation).
     * Clears the session and navigates back to login page.
     */
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
            // Delete quizzes
            if (myQuizzes != null) {
                for (Quiz q : myQuizzes) {
                    if (q != null) {
                        quizDAO.deleteQuiz(q.getQuizId());
                    }
                }
            }

            // Delete avatar files
            try { avatars.deleteAvatar(currentUser); } catch (Exception ignore) {}

            // Delete user
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

    /**
     * Opens the password reset view.
     * @throws IOException if navigation fails
     */
    @FXML
    private void handleChangePassword() throws IOException {
        Navigator.goTo(changePasswordButton, "resetPassword.fxml");
    }

    /**
     * Prompts for an image file, saves it as the user's avatar, and updates the view.
     */
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

    /**
     * Removes the user's custom avatar (after confirmation) and reverts to the default image.
     */
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

    /**
     * Updates visibility of avatar action buttons depending on whether a custom avatar exists.
     */
    @FXML
    private void updateAvatarButtons() {
        boolean hasCustom = avatars.findAvatarFile(currentUser).isPresent();
        if (deleteAvatarBtn != null) {
            deleteAvatarBtn.setVisible(hasCustom);
            deleteAvatarBtn.setManaged(hasCustom);
        }
    }

    /**
     * Opens a dialog with the full badges view.
     */
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

    /**
     * Populates the accuracy pie chart with counts for correct/incorrect answers.
     * Calculate accuracy and display on overlay text.
     * @param correct amount of correct answers.
     * @param incorrect amount of incorrect answers.
     */
    private void setupAccuracyChart(int correct, int incorrect) {
        int safeCorrect = Math.max(correct, 0);
        int safeIncorrect = Math.max(incorrect, 0);
        int total = safeCorrect + safeIncorrect;

        accuracyChart.getData().setAll(
                new PieChart.Data("Correct", safeCorrect),
                new PieChart.Data("Incorrect", safeIncorrect)
        );
        accuracyChart.setLegendVisible(false);
        accuracyChart.setLabelsVisible(false); // keep the pie clean

        // Create overlay label once
        if (accuracyOverlay == null) {
            accuracyOverlay = new Label();
            accuracyOverlay.getStyleClass().add("accuracy-overlay");
            accuracyOverlay.setMouseTransparent(true);
            accuracyChartContainer.getChildren().add(accuracyOverlay);
        }

        // Compute % and update text
        String text = (total == 0) ? "—" : String.format("%.0f%%", (safeCorrect * 100.0) / total);
        accuracyOverlay.setText(text);
    }

    /**
     * Builds a bar chart of attempts in the last 7 days (Sun–Sat labels).
     */
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

    /**
     * Renders a line chart showing weekly streak values for the past four weeks and now.
     */
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

    /**
     * Builds a stacked bar chart for quiz counts by topic (subject) for the current user.
     */
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

    /**
     * Populates the badges grid: shows earned badges or an empty label if none.
     */
    private void renderBadges() {
        badgesGrid.getChildren().clear();

        if (CompletedBadges.isEmpty()) {
            Label empty = new Label("No badges achieved yet");
            empty.getStyleClass().add("empty-label");
            badgesGrid.getChildren().add(empty);
        }
        else {
            for (Badge b : CompletedBadges) {
                badgesGrid.getChildren().add(BadgeRenderer.buildCard(b, currentUser, badgeProgressDAO, false));
            }
        }
    }

    // Helpers

    /**
     * Sets the status label (null-safe).
     * @param msg Message to set as status
     */
    private void setStatus(String msg) { statusLabel.setText(trim(msg)); }

    /**
     * Disables core form controls.
     */
    private void disableForm() {
        usernameField.setDisable(true);
        emailField.setDisable(true);
        saveButton.setDisable(true);
    }
}
