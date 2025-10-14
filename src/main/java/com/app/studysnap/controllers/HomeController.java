package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.services.Popup;
import com.app.studysnap.auth.Session;
import com.app.studysnap.model.IQuizDAO;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.SqliteQuizDAO;
import com.app.studysnap.model.User;
import com.app.studysnap.services.PdfExporter;
import com.app.studysnap.services.QuizRenderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private FlowPane deckContainer;
    @FXML private VBox emptyState;
    @FXML private ImageView avatarView;

    private static final int avatar_size = 48;
    private Image defaultAvatar;
    private static final String[] avatar_format = {".png", ".jpg", ".jpeg", ".gif"};
    private static final String DEFAULT_AVATAR_PATH = "src/main/resources/com/app/studysnap/images/default_avatar.png";

    private final IQuizDAO dao = new SqliteQuizDAO();
    private final PdfExporter pdfExporter = new PdfExporter();
    private final QuizRenderer renderer = new QuizRenderer();

    @FXML
    public void initialize() {
        User u = Session.getCurrentUser();
        String name = (u != null && u.getUsername() != null && !u.getUsername().isBlank())
                ? u.getUsername() : "there";
        welcomeLabel.setText("Welcome, " + name);

        loadMyQuizzes();

        // Load avatar image
        defaultAvatar = avatarView.getImage();
        if (defaultAvatar == null) {
            defaultAvatar = loadResourceImage(DEFAULT_AVATAR_PATH);
            if (defaultAvatar != null) applyCircularAvatar(avatarView, defaultAvatar, avatar_size);;
        }

        boolean hasCustom = loadAvatarFromDisk(u);
        if (!hasCustom && defaultAvatar != null) {
            applyCircularAvatar(avatarView, defaultAvatar, avatar_size);
        }
    }

    // Load user quizzes on fxml decks
    private void loadMyQuizzes() {
        deckContainer.getChildren().clear();

        User u = Session.getCurrentUser();
        if (u == null) {
            showEmpty(true);
            return;
        }

        List<Quiz> mine = dao.getQuizzesByUser(u.getUserId());
        if (mine.isEmpty()) {
            showEmpty(true);
            return;
        }

        showEmpty(false);
        for (Quiz q : mine) {
            deckContainer.getChildren().add(buildCard(q));
        }
    }

    // Handle display if user has no quizzes
    private void showEmpty(boolean show) {
        emptyState.setVisible(show);
        emptyState.setManaged(show);
    }

    // Create fxml deck
    private Node buildCard(Quiz q) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(280);
        card.getStyleClass().add("card");

        Label title = new Label(nz(q.getTitle()));
        title.getStyleClass().add("card-title");

        HBox meta = new HBox(10);
        Label subject = new Label("📚 " + nz(q.getSubject()));
        subject.getStyleClass().add("card-meta");
        Label is_private = new Label(q.get_is_private() ? "🔒 Private" : "🌐 Public");
        is_private.getStyleClass().add("card-meta");
        meta.getChildren().addAll(subject, is_private);

        Text desc = new Text(nz(q.getDescription()));
        desc.setWrappingWidth(256);
        desc.getStyleClass().add("card-desc");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.BOTTOM_LEFT);
        Button openBtn = new Button("Play");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button exportBtn = new Button("Export");

        openBtn.setOnAction(e -> openQuizPlayPage(q, "playQuiz.fxml"));
        editBtn.setOnAction(e -> openEditQuiz(q, "editQuiz.fxml"));
        deleteBtn.setOnAction(e -> handleDeleteQuiz(q));
        exportBtn.setOnAction(e -> exportQuizPdf(q.getQuizId()));
        actions.getChildren().addAll(openBtn, editBtn, deleteBtn, exportBtn);

        card.getChildren().addAll(title, meta, desc, spacer, actions);
        return card;
    }

    // Empty-state button -> go to quiz generator inside the dashboard
    @FXML
    private void onCreateQuiz() {
        // Load quizGen.fxml into the dashboard content area
        Navigator.showInDashboard(deckContainer, "quizGen.fxml");
    }

    // Handle btn to delete a quiz
    private void handleDeleteQuiz(Quiz q) {
        boolean confirmed = Popup.confirm(
                "Delete quiz",
                "Delete \"" + nz(q.getTitle()) + "\"?\nThis will permanently remove the quiz and all its questions."
        );
        if (!confirmed) return;

        try {
            dao.deleteQuiz(q.getQuizId());
            loadMyQuizzes();
            Popup.info("Quiz deleted.");
        } catch (Exception ex) {
            Popup.error("Could not delete quiz:\n" + ex.getMessage());
        }
    }

    // handle btn to export quiz as pdf
    private void exportQuizPdf(int quizId) {
        // Load full quiz with questions
        Quiz full = dao.getQuizById(quizId);
        if (full == null) {
            Popup.error("Could not load quiz details.");
            return;
        }

        // Ask user choice (include answers or not)
        Alert ask = new Alert(Alert.AlertType.CONFIRMATION);
        ask.setHeaderText("Export Quiz");
        ask.setContentText("Include answers in the PDF?");
        ButtonType withAns = new ButtonType("With answers");
        ButtonType withoutAns = new ButtonType("Without answers");
        ButtonType cancel = ButtonType.CANCEL;
        ask.getButtonTypes().setAll(withAns, withoutAns, cancel);
        ButtonType choice = ask.showAndWait().orElse(cancel);
        if (choice == cancel) return;

        boolean includeAnswers = (choice == withAns);

        // Render text
        String txt = renderer.renderAsText(full, includeAnswers);
        if (txt.isBlank()) {
            Popup.warn("Nothing to export.");
            return;
        }

        // Save file
        FileChooser fc = new FileChooser();
        fc.setTitle(includeAnswers ? "Export Quiz (with answers)" : "Export Quiz");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        String base = safeFileName(full.getTitle());
        fc.setInitialFileName(base + (includeAnswers ? "-with-answers" : "") + ".pdf");
        File pdf = fc.showSaveDialog(deckContainer.getScene().getWindow());
        if (pdf == null) return;

        try {
            pdfExporter.export(txt, pdf);
            Popup.info("PDF saved to:\n" + pdf.getAbsolutePath());
        } catch (Exception ex) {
            Popup.error("Export failed:\n" + ex.getMessage());
        }
    }

    //This opens quiz play page where you see all the questions belongs to the selected quiz
    private void openQuizPlayPage(Quiz quiz, String fxml) {
        if(!Popup.confirm(
                "Play Quiz",
                "Are you ready to attempt: " + quiz.getTitle()
        )) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent view = loader.load();

            // Get controller from the loaded FXML and pass the quiz
            PlayQuizPageController controller = loader.getController();
            if (controller != null) {
                controller.setQuiz(quiz);
            }

            emptyState.getScene().setRoot(view);
        } catch (Exception ex) {
            Popup.error("Failed to open quiz play page:\n" + ex.getMessage());
        }
    }

    private void openEditQuiz(Quiz quiz, String fxml) {
        if(!Popup.confirm(
                "Edit Quiz",
                "Do you want to make changes to: " + quiz.getTitle()
        )) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxml));
            Parent view = loader.load();

            // Get controller from the loaded FXML and pass the quiz
            EditQuizController controller = loader.getController();
            if (controller != null) {
                controller.setQuiz(quiz);
            }

            welcomeLabel.getScene().setRoot(view);
        } catch (Exception ex) {
            Popup.error("Failed to open quiz edit page:\n" + ex.getMessage());
        }
    }

    // format pdf file name for download
    private String safeFileName(String s) {
        String base = (s == null || s.isBlank()) ? "quiz" : s.trim();
        return base.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    // Helper to handle null strings
    private String nz(String s) { return s == null ? "" : s; }
    private static String safe(String s) { return s == null ? "" : s.trim(); }

    // Avatar image helpers
    private Image loadResourceImage(String path) {
        var url = ProfileController.class.getResource(path);
        if (url == null) {
            var cl = Thread.currentThread().getContextClassLoader();
            url = cl.getResource(path.startsWith("/") ? path.substring(1) : path);
        }
        return (url != null) ? new Image(url.toExternalForm(), 96, 96, true, true) : null;
    }
    private boolean loadAvatarFromDisk(User u) {
        Path p = findAvatarFile(u);
        if (p != null && Files.exists(p)) {
            String uri = p.toUri().toString();
            applyCircularAvatar(avatarView, new Image(uri), avatar_size);
            return true;
        }
        return false;
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
    private void applyCircularAvatar(ImageView iv, Image img, double sizePx) {
        iv.setImage(img);

        double w = img.getWidth();
        double h = img.getHeight();
        double s = Math.min(w, h);
        double x = (w - s) / 2.0;
        double y = (h - s) / 2.0;
        iv.setViewport(new Rectangle2D(x, y, s, s));

        iv.setFitWidth(sizePx);
        iv.setFitHeight(sizePx);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);

        Circle clip = new Circle(sizePx / 2.0, sizePx / 2.0, sizePx / 2.0);
        iv.setClip(clip);

        iv.layoutBoundsProperty().addListener((obs, oldB, newB) -> {
            double cx = newB.getWidth()  / 2.0;
            double cy = newB.getHeight() / 2.0;
            double r  = Math.min(newB.getWidth(), newB.getHeight()) / 2.0;
            clip.setCenterX(cx);
            clip.setCenterY(cy);
            clip.setRadius(r);
        });
    }
}