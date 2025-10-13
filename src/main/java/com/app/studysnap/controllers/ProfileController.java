package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.model.*;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.services.Popup;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private List<Quiz> myQuizzes;
    private User currentUser;
    private static final String[] avatar_format = {".png", ".jpg", ".jpeg", ".gif"};
    private Image defaultAvatar;
    private static final String DEFAULT_AVATAR_PATH = "/images/default_avatar.png";

    @FXML
    private void initialize() {
        try {
            userDAO = new SqliteUserDAO();
            quizDAO = new SqliteQuizDAO();
            attemptDAO = new SqliteAttemptDAO();

        } catch (Throwable t) {
            userDAO = null;
            quizDAO = null;
            attemptDAO = null;
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
        defaultAvatar = avatarView.getImage();
        if (defaultAvatar == null) {
            defaultAvatar = loadResourceImage(DEFAULT_AVATAR_PATH);
            if (defaultAvatar != null) avatarView.setImage(defaultAvatar);
        }

        boolean hasCustom = loadAvatarFromDisk();
        if (!hasCustom && defaultAvatar != null) {
            avatarView.setImage(defaultAvatar);
        }
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
        setupAccuracyChart(correct, Math.max(0, attempts * 10 - correct));
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
            Path saved = saveAvatarForUser(currentUser, chosen);
            avatarView.setImage(new Image(saved.toUri().toString(), 96, 96, true, true));
            setStatus("Avatar updated ✓");
            updateAvatarButtons(); // <— show delete
        } catch (Exception e) {
            setStatus("Could not save avatar.");
        }
    }

    @FXML
    private void handleDeleteAvatar() {
        if (currentUser == null) return;
        if (!Popup.confirm("Remove avatar?", "Revert to the default icon.")) return;

        try {
            deleteAvatarFilesForUser(currentUser);
            resetAvatarToDefault();
            setStatus("Avatar removed ✓");
            updateAvatarButtons();
        } catch (Exception ex) {
            Popup.error("Couldn't remove avatar: " + ex.getMessage());
        }
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
        List<String> userBadges = List.of(); // TODO: fetch from DB

        if (userBadges.isEmpty()) {
            Label empty = new Label("No badges achieved yet");
            empty.getStyleClass().add("empty-label");
            badgesGrid.getChildren().add(empty);
            return;
        }

        //TODO: otherwise render badges
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

    /* ------------ avatar helpers ------------ */
    private void updateAvatarButtons() {
        boolean hasCustom = findAvatarFile(currentUser) != null;
        if (deleteAvatarBtn != null) {
            deleteAvatarBtn.setVisible(hasCustom);
            deleteAvatarBtn.setManaged(hasCustom);
        }
    }
    private boolean loadAvatarFromDisk() {
        Path p = findAvatarFile(currentUser);
        if (p != null && Files.exists(p)) {
            String uri = p.toUri().toString();
            avatarView.setImage(new Image(uri, 96, 96, true, true));
            return true;
        }
        return false;
    }
    private Path saveAvatarForUser(User u, File chosen) throws Exception {
        Path dir = getAvatarsDir();
        Files.createDirectories(dir);

        String base = baseAvatarName(u);
        String ext = extLower(chosen.getName());
        if (!ext.matches("\\.(png|jpg|jpeg|gif)")) ext = ".png";
        for (String e : avatar_format) Files.deleteIfExists(dir.resolve(base + e));

        Path target = dir.resolve(base + ext);
        Files.copy(chosen.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }
    private Path findAvatarFile(User u) {
        Path dir = getAvatarsDir();
        String base = baseAvatarName(u);
        for (String e : avatar_format) {
            Path p = dir.resolve(base + e);
            if (Files.exists(p)) return p;
        }
        return null;
    }
    private Path getAvatarsDir() {
        return Paths.get(System.getProperty("user.home"), ".studysnap", "avatars");
    }
    private String baseAvatarName(User u) {
        if (u != null && u.getUserId() > 0) return "u" + u.getUserId();
        String email = safe(u == null ? null : u.getEmail());
        return email.isBlank() ? "anonymous" : email.replaceAll("[^a-zA-Z0-9]", "_");
    }
    private static String extLower(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i).toLowerCase() : "";
    }
    private void resetAvatarToDefault() {
        if (defaultAvatar == null) defaultAvatar = loadResourceImage(DEFAULT_AVATAR_PATH);
        if (defaultAvatar != null) avatarView.setImage(defaultAvatar);
    }
    private void deleteAvatarFilesForUser(User u) throws Exception {
        Path dir = getAvatarsDir();
        String base = baseAvatarName(u);
        for (String e : avatar_format) {
            Files.deleteIfExists(dir.resolve(base + e));
        }
    }
    private Image loadResourceImage(String path) {
        var url = ProfileController.class.getResource(path);
        if (url == null) {
            var cl = Thread.currentThread().getContextClassLoader();
            url = cl.getResource(path.startsWith("/") ? path.substring(1) : path);
        }
        return (url != null) ? new Image(url.toExternalForm(), 96, 96, true, true) : null;
    }
}
