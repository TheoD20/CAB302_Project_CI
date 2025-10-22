package com.app.studysnap.controllers;

import com.app.studysnap.Main;
import com.app.studysnap.services.*;
import com.app.studysnap.auth.Session;
import com.app.studysnap.model.IQuizDAO;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.SqliteQuizDAO;
import com.app.studysnap.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

import static com.app.studysnap.services.PdfExporter.safeFileName;
import static com.app.studysnap.services.TextParser.*;

/**
 * Controller for the Home view fxml.
 * <p>
 * Displays a personalized welcome message, the user's quiz cards, an empty state when
 * no quizzes exist, and quick actions to play, edit, delete, or export a quiz to PDF.
 * Also loads and displays the user's avatar.
 * </p>
 */
public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private FlowPane deckContainer;
    @FXML private VBox emptyState;
    @FXML private ImageView avatarView;

    private final AvatarService avatars = new AvatarService();
    private static final double AVATAR_SIZE = 48.0;

    private final IQuizDAO dao = new SqliteQuizDAO();
    private final PdfExporter pdfExporter = new PdfExporter();
    private final QuizRenderer renderer = new QuizRenderer();

    /**
     * JavaFX initialization – sets welcome text, loads the user's quizzes into cards,
     * and applies the avatar image (user-specific or default).
     */
    @FXML
    public void initialize() {
        User u = Session.getCurrentUser();
        String name = (u != null && u.getUsername() != null && !u.getUsername().isBlank())
                ? u.getUsername() : "there";
        welcomeLabel.setText("Welcome, " + name);

        loadMyQuizzes();

        // Load avatar image
        Image defaultAvatar = avatars.loadDefaultAvatar(AVATAR_SIZE);
        avatars.applyUserAvatarOrDefault(avatarView, u, AVATAR_SIZE, defaultAvatar);
    }

    /**
     * Loads quizzes belonging to the current user and renders them as cards.
     * Shows the empty state when there are no quizzes or no active session.
     */
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

    /**
     * Shows or hides the empty state panel for when user have no quizzes yet.
     * @param show {@code true} to show the empty state; {@code false} to hide it
     */
    private void showEmpty(boolean show) {
        emptyState.setVisible(show);
        emptyState.setManaged(show);
    }

    /**
     * Builds a single quiz card node with title, subject, description, and action buttons.
     * @param q the quiz to render
     * @return a configured {@link Node} representing the quiz card
     */
    private Node buildCard(Quiz q) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefWidth(280);
        card.getStyleClass().add("card");

        Label title = new Label(trim(q.getTitle()));
        title.getStyleClass().add("card-title");

        HBox meta = new HBox(10);
        Label subject = new Label("📚 " + trim(q.getSubject()));
        subject.getStyleClass().add("card-meta");
        Label is_private = new Label(q.get_is_private() ? "🔒 Private" : "🌐 Public");
        is_private.getStyleClass().add("card-meta");
        meta.getChildren().addAll(subject, is_private);

        Text desc = new Text(trim(q.getDescription()));
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

        openBtn.setOnAction(e -> openQuizPlayPage(q));
        editBtn.setOnAction(e -> openEditQuiz(q));
        deleteBtn.setOnAction(e -> handleDeleteQuiz(q));
        exportBtn.setOnAction(e -> exportQuizPdf(q.getQuizId()));
        actions.getChildren().addAll(openBtn, editBtn, deleteBtn, exportBtn);

        card.getChildren().addAll(title, meta, desc, spacer, actions);
        return card;
    }

    /**
     * Handles empty-state button click: open the quiz generator within the dashboard content area.
     */
    @FXML
    private void onCreateQuiz() {
        // Load quizGen.fxml into the dashboard content area
        Navigator.showInDashboard(deckContainer, "quizGen.fxml");
    }

    /**
     * Confirms and deletes the given quiz, then reloads the list.
     * @param q the quiz to delete
     */
    private void handleDeleteQuiz(Quiz q) {
        boolean confirmed = Popup.confirm(
                "Delete quiz",
                "Delete \"" + trim(q.getTitle()) + "\"?\nThis will permanently remove the quiz and all its questions."
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

    /**
     * Exports a quiz (with or without answers) to a PDF chosen by the user.
     * @param quizId the ID of the quiz to export
     */
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
        if (isBlank(txt)) {
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

    /**
     * Navigates to the quiz play page (after confirmation) and injects the selected quiz.
     * @param quiz the quiz to play
     */
    private void openQuizPlayPage(Quiz quiz) {
        if(!Popup.confirm(
                "Play Quiz",
                "Are you ready to attempt: " + quiz.getTitle()
        )) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("playQuiz.fxml"));
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

    /**
     * Navigates to the quiz edit page (after confirmation) and injects the selected quiz.
     * @param quiz the quiz to edit
     */
    private void openEditQuiz(Quiz quiz) {
        if(!Popup.confirm(
                "Edit Quiz",
                "Do you want to make changes to: " + quiz.getTitle()
        )) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("editQuiz.fxml"));
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
}