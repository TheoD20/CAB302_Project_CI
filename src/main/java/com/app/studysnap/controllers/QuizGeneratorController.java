package com.app.studysnap.controllers;

import com.app.studysnap.model.*;
import com.app.studysnap.services.*;
import com.app.studysnap.auth.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.app.studysnap.services.TextParser.*;

/**
 * Controller for the Quiz Generator workspace.
 * <p>
 * Supports generating quizzes from uploaded files, pasted text, or prompts; browsing public quizzes;
 * previewing questions (with/without answers); saving to the database; and exporting to PDF.
 * </p>
 */
public class QuizGeneratorController {

    // Common UI
    @FXML private TabPane tabPane;
    @FXML private TextArea previewArea;
    @FXML private ProgressIndicator progress;

    // Upload tab
    @FXML private StackPane fileDropZone;
    @FXML private Button chooseFileBtn;
    @FXML private Label chosenFileLabel;
    @FXML private CheckBox includeAnswersUpload;
    private File selectedFile;

    // Paste tab
    @FXML private TextArea pastedTextArea;
    @FXML private CheckBox includeAnswersPaste;

    // Prompt tab
    @FXML private TextArea promptTextArea;
    @FXML private CheckBox includeAnswersPrompt;

    // Public tab
    @FXML private TextField searchField;
    @FXML private TableView<PublicQuizRow> publicTable;
    @FXML private TableColumn<PublicQuizRow, String> colName, colSubject, colDescription, colAuthor;
    @FXML private CheckBox publicIncludeAnswersCheck;

    // Display and Actions tab
    @FXML private CheckBox exportWithAnswersCheck;

    // Services
    private final QuizService genGateway = new QuizService();
    private final TextParser parser = new TextParser();
    private final QuizRenderer renderer = new QuizRenderer();
    private final PdfExporter pdfExporter = new PdfExporter();
    private final IQuizDAO quizDao = new SqliteQuizDAO();

    // Display state
    private List<com.app.studysnap.model.Question> lastGeneratedQuestions = List.of();
    private String lastGeneratedWithAnswers = null;

    /**
     * Default constructor:
     * Creates a new {@code QuizGeneratorController}.
     */
    public QuizGeneratorController() {}

    /**
     * JavaFX initialization: wires drag and drop handlers and configures the public-table columns/sizing.
     */
    @FXML
    public void initialize() {
        // Drag and drop setup for file area
        if (fileDropZone != null) {
            fileDropZone.setOnDragOver(this::onDragOver);
            fileDropZone.setOnDragDropped(this::onDragDropped);
        }

        // Public table setup (empty for now, but columns wired)
        if (publicTable != null) {
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
            colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
            colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));

            // make sure columns are scaled properly with screen
            publicTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            var tableWidth = publicTable.widthProperty().subtract(18);

            colName.prefWidthProperty().bind(tableWidth.multiply(0.25));
            colSubject.prefWidthProperty().bind(tableWidth.multiply(0.25));
            colDescription.prefWidthProperty().bind(tableWidth.multiply(0.35));
            colAuthor.prefWidthProperty().bind(tableWidth.multiply(0.15));

            // minimum size so they don’t collapse
            colName.setMinWidth(120);
            colSubject.setMinWidth(100);
            colDescription.setMinWidth(160);
            colAuthor.setMinWidth(100);
        }
    }

    // Upload

    /**
     * Opens a file chooser for PDF/TXT and stores the selected file.
     */
    @FXML private void onChooseFile() {
        var owner = chooseFileBtn.getScene().getWindow();
        File f = FileDialogs.chooseOpenDoc(owner);
        if (f != null) {
            selectedFile = f;
            chosenFileLabel.setText(f.getName());
        }
    }

    /**
     * Accepts copy transfer mode when files are dragged over the drop zone.
     */
    private void onDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasFiles()) e.acceptTransferModes(TransferMode.COPY);
        e.consume();
    }

    /**
     * Handles files dropped into the drop zone and updates UI.
     */
    private void onDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasFiles()) {
            selectedFile = db.getFiles().get(0);
            chosenFileLabel.setText(selectedFile.getName());
            e.setDropCompleted(true);
        } else {
            e.setDropCompleted(false);
        }
        e.consume();
    }

    /**
     * Generates quiz from the uploaded document (always requests answers internally),
     * parses them, and updates the preview (respecting the "include answers" toggle).
     */
    @FXML private void onGenerateFromUpload() {
        if (selectedFile == null) {
            Popup.warn("No file selected. Choose a PDF/TXT first.");
            return;
        }
        Async.run(
            () -> genGateway.generateFromUpload(selectedFile, true), // always with answers; hide later
            txt -> {
                if (isBlank(txt)) {
                    Popup.error("Nothing was generated from the file. Try a different file or reduce size.");
                    return;
                }
                lastGeneratedWithAnswers = txt;
                lastGeneratedQuestions = parser.parse(txt);

                boolean showAns = includeAnswersUpload != null && includeAnswersUpload.isSelected();
                String display = showAns ? lastGeneratedWithAnswers : stripAnswers(lastGeneratedWithAnswers);
                previewArea.setText(display);
            },
            progress, tabPane
        );
    }

    // Paste

    /**
     * Generates questions from pasted text, parses them, and updates the preview.
     */
    @FXML private void onGenerateFromPaste() {
        String text = pastedTextArea.getText();
        if (isBlank(text)) {
            Popup.warn("Nothing to generate. Paste some content first.");
            return;
        }
        Async.run(
            () -> genGateway.generateFromPaste(text, true),
            txt -> {
                if (isBlank(txt)) {
                    Popup.error("Nothing was generated from the pasted text. Add more detail and try again.");
                    return;
                }
                lastGeneratedWithAnswers = txt;
                lastGeneratedQuestions = parser.parse(txt);


                boolean showAns = includeAnswersPaste != null && includeAnswersPaste.isSelected();
                String display = showAns ? lastGeneratedWithAnswers : stripAnswers(lastGeneratedWithAnswers);
                previewArea.setText(display);
            },
            progress, tabPane
        );
    }

    /**
     * Handle clearing paste area
     */
    @FXML private void onResetPaste() {
        pastedTextArea.clear();
    }

    // Prompt

    /**
     * Generates questions from a short prompt, parses them, and updates the preview.
     */
    @FXML private void onGenerateFromPrompt() {
        String prompt = promptTextArea.getText();
        if (isBlank(prompt)) {
            Popup.warn("Empty prompt. Write a short prompt.");
            return;
        }
        Async.run(() -> genGateway.generateFromPrompt(prompt, 10, true),
            txt -> {
                if (isBlank(prompt)) {
                    Popup.error("Nothing was generated. Try a different prompt or include more context.");
                    return;
                }
                lastGeneratedWithAnswers = txt;
                lastGeneratedQuestions = parser.parse(txt);

                boolean showAns = includeAnswersPrompt != null && includeAnswersPrompt.isSelected();
                String display = showAns ? lastGeneratedWithAnswers : stripAnswers(lastGeneratedWithAnswers);
                previewArea.setText(display);
            },
            progress, tabPane
        );
    }

    /**
     * Handles clearing the prompt area.
     */
    @FXML private void onResetPrompt() {
        promptTextArea.clear();
    }

    // Public

    /**
     * Refreshes the list of public quizzes based on the search field.
     */
    @FXML private void onRefreshPublic() {
        String q = trim(searchField.getText());
        Async.run(
            () -> quizDao.findPublic(q),
            items -> {
                var rows = new ArrayList<PublicQuizRow>();
                for (var it : items) rows.add(new PublicQuizRow(
                        it.quizId(), it.name(), it.subject(), it.description(), it.author()
                ));
                publicTable.getItems().setAll(rows);
            },
            progress, tabPane
        );
    }

    /**
     * Downloads the selected public quiz, renders it to text, and updates the preview.
     */
    @FXML private void onDownloadSelected() {
        PublicQuizRow sel = publicTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Popup.info("No selection. Pick a quiz row to download.");
            return;
        }
        Async.run(
            () -> quizDao.getQuizById(sel.quizId()),
            quiz -> {
                lastGeneratedQuestions = quiz.getQuestions();
                lastGeneratedWithAnswers = renderer.renderAsText(quiz, true);

                boolean showAns = publicIncludeAnswersCheck != null && publicIncludeAnswersCheck.isSelected();
                String display = showAns ? lastGeneratedWithAnswers : stripAnswers(lastGeneratedWithAnswers);
                previewArea.setText(display);
            },
            progress, tabPane
        );
    }

    /**
     * Toggles whether answers are included in the preview and syncs all include-answer checkboxes.
     */
    @FXML private void onIncludeAnswersToggle(ActionEvent e) {
        if (isBlank(lastGeneratedWithAnswers)) return;

        CheckBox src = (CheckBox) e.getSource();
        boolean showAns = src != null && src.isSelected();

        syncIncludeAnswerChecks(showAns);

        String display = showAns ? lastGeneratedWithAnswers : stripAnswers(lastGeneratedWithAnswers);
        previewArea.setText(display);
    }

    /** Keeps all include-answer toggles in sync across tabs. */
    private void syncIncludeAnswerChecks(boolean selected) {
        if (includeAnswersUpload != null) includeAnswersUpload.setSelected(selected);
        if (includeAnswersPaste  != null) includeAnswersPaste.setSelected(selected);
        if (includeAnswersPrompt != null) includeAnswersPrompt.setSelected(selected);
        if (publicIncludeAnswersCheck != null) publicIncludeAnswersCheck.setSelected(selected);
    }

    // Save and Export

    /**
     * Prompts for quiz metadata, validates inputs, persists a new quiz, and updates UI/badges.
     */
    @FXML private void onSave() {

        // Handles dialog for quiz name, subject, description and is_public
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Save Quiz");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField subjectField = new TextField();
        TextArea descArea = new TextArea(); descArea.setPrefRowCount(3);
        CheckBox publicCheck = new CheckBox("Make it public?");

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(8); gp.setPadding(new Insets(10));
        gp.addRow(0, new Label("Quiz name:"), nameField);
        gp.addRow(1, new Label("Subject:"), subjectField);
        gp.addRow(2, new Label("Description:"), descArea);
        gp.add(publicCheck, 1, 3);
        dlg.getDialogPane().setContent(gp);

        if (dlg.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String nm = nameField.getText();
        String subj = subjectField.getText();
        String desc = descArea.getText();
        boolean is_private = !publicCheck.isSelected();

        if (isBlank(nm)) {
            Popup.warn("Name is required.");
            return;
        }

        if (lastGeneratedQuestions == null || lastGeneratedQuestions.isEmpty()) {
            // Fallback: if user typed/edited manually, try parse the displayed text
            var parsed = parser.parse(previewArea.getText());
            if (parsed.isEmpty()) {
                Popup.warn("No questions found. Generate a quiz first or use the expected MCQ format (A–E).");
                return;
            }
            lastGeneratedQuestions = parsed;
        }

        var user = Session.getCurrentUser();
        int createdBy = (user != null) ? user.getUserId() : 0;

        var quiz = new Quiz(nm, subj, desc, is_private, createdBy);
        quiz.setQuestions(lastGeneratedQuestions);

        Async.run(
            () -> {
                // Add quiz
                quizDao.addQuiz(quiz);

                return null; },
            ignored -> {
                Popup.info("Quiz saved successfully.");
                onRefreshPublic();

                // Update creation type badges
                new BadgeService().UpdateCreationTypeBadges(Session.getCurrentUser().getUserId());
            },
            progress, tabPane
        );
    }

    /**
     * Exports the currently previewed quiz text to PDF (with or without answers).
     */
    @FXML private void onExportPdf() {
        boolean withAnswers = exportWithAnswersCheck != null && exportWithAnswersCheck.isSelected();
        String txt = PdfExporter.buildExportText(
                withAnswers,
                previewArea.getText(),
                lastGeneratedWithAnswers,
                lastGeneratedQuestions,
                renderer
        );

        if (isBlank(txt)) {
            Popup.warn("Nothing to export. Generate or load a quiz first.");
            return;
        }

        // Save As dialog defaulting to Downloads
        var owner = (tabPane != null && tabPane.getScene() != null) ? tabPane.getScene().getWindow() : null;
        File dest = FileDialogs.chooseSavePdf(owner, withAnswers ? "quiz-with-answers" : "quiz");
        if (dest == null) return;

        Async.run(
            () -> { pdfExporter.export(txt, dest); return dest; },
            out -> Popup.info("PDF saved to:\n" + out.getAbsolutePath()),
            progress, tabPane
        );
    }

    // Helpers

    /**
     * Hides answer lines from generated text.
     * Lines beginning with "Answer:" (case-insensitive) are removed.
     * @param text full generated text including answers
     * @return text with answer lines stripped
     */
    private String stripAnswers(String text) {
        if (text == null) return null;
        // Remove lines starting with "Answer:" (case-insensitive, tolerant spacing)
        return text.replaceAll("(?im)^\\s*Answer:\\s*.*\\R?", "");
    }

    /**
     * Row model for the public quizzes table.
     * @param quizId Quiz unique identifier.
     * @param name Quiz title.
     * @param subject Quiz subject/topic.
     * @param description Quiz description.
     * @param author Author name.
     */
    public record PublicQuizRow(int quizId, String name, String subject, String description, String author) {

        /**
         * Getter for quiz name
         * @return the quiz name as displayed in the public list
         */
        public String getName() { return name; }

        /**
         * Getter for quiz subject
         * @return the quiz subject as displayed in the public list
         */
        public String getSubject() { return subject; }

        /**
         * Getter for quiz description
         * @return the quiz description as displayed in the public list
         */
        public String getDescription() { return description; }

        /**
         * Getter for quiz author
         * @return the quiz author as displayed in the public list
         */
        public String getAuthor() { return author; }
    }
}