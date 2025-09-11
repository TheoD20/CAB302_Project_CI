package com.app.studysnap.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

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
    @FXML private final com.app.studysnap.services.QuizService genGateway = new com.app.studysnap.services.QuizService();
    @FXML private final com.app.studysnap.services.QuizTextParser parser = new com.app.studysnap.services.QuizTextParser();
    @FXML private final com.app.studysnap.services.QuizRenderer renderer = new com.app.studysnap.services.QuizRenderer();
    @FXML private final com.app.studysnap.services.PdfExporter pdfExporter = new com.app.studysnap.services.PdfExporter();
    @FXML private final com.app.studysnap.model.IQuizDAO quizDao = new com.app.studysnap.model.SqliteQuizDAO();

    // Display
    @FXML private java.util.List<com.app.studysnap.model.Question> lastGeneratedQuestions = java.util.Collections.emptyList();
    @FXML private String lastGeneratedWithAnswers = null;


    @FXML
    public void initialize() {
        // Drag & drop setup for file area
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

    // ---------------- Upload ----------------
    @FXML private void onChooseFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", List.of("*.pdf", "*.txt"))
        );
        File f = fc.showOpenDialog(chooseFileBtn.getScene().getWindow());
        if (f != null) {
            selectedFile = f;
            chosenFileLabel.setText(f.getName());
        }
    }

    private void onDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasFiles()) e.acceptTransferModes(TransferMode.COPY);
        e.consume();
    }

    private void onDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasFiles()) {
            selectedFile = db.getFiles().getFirst();
            chosenFileLabel.setText(selectedFile.getName());
            e.setDropCompleted(true);
        } else {
            e.setDropCompleted(false);
        }
        e.consume();
    }

    @FXML private void onGenerateFromUpload() {
        if (selectedFile == null) {
            alert(Alert.AlertType.WARNING, "No file selected", "Choose a PDF/TXT first.");
            return;
        }
        disableAll(true);
        runAsync(
                () -> genGateway.generateFromUpload(selectedFile, true), // force includeAnswers=true
                txt -> {
                    lastGeneratedWithAnswers = txt;
                    lastGeneratedQuestions = parser.parse(txt); // has correct_option set
                    // Show or hide answers purely in the preview
                    String display = includeAnswersUpload.isSelected() ? txt : stripAnswers(txt);
                    previewArea.setText(display);
                    disableAll(false);
                }
        );
    }

    // ---------------- Paste ----------------
    @FXML private void onGenerateFromPaste() {
        String text = pastedTextArea.getText();
        if (text == null || text.isBlank()) {
            alert(Alert.AlertType.WARNING, "Nothing to generate", "Paste some content first.");
            return;
        }
        disableAll(true);
        runAsync(
                () -> genGateway.generateFromPaste(text, true), // force includeAnswers=true
                txt -> {
                    lastGeneratedWithAnswers = txt;
                    lastGeneratedQuestions = parser.parse(txt);
                    String display = includeAnswersPaste.isSelected() ? txt : stripAnswers(txt);
                    previewArea.setText(display);
                    disableAll(false);
                }
        );
    }

    @FXML private void onResetPaste() {
        pastedTextArea.clear();
    }

    // ---------------- Prompt ----------------
    @FXML private void onGenerateFromPrompt() {
        String prompt = promptTextArea.getText();
        if (prompt == null || prompt.isBlank()) {
            alert(Alert.AlertType.WARNING, "Empty prompt", "Write a short prompt.");
            return;
        }
        disableAll(true);
        runAsync(
                () -> genGateway.generateFromPrompt(prompt, 15, true), // force includeAnswers=true
                txt -> {
                    lastGeneratedWithAnswers = txt;
                    lastGeneratedQuestions = parser.parse(txt);
                    String display = includeAnswersPrompt.isSelected() ? txt : stripAnswers(txt);
                    previewArea.setText(display);
                    disableAll(false);
                }
        );
    }

    @FXML private void onResetPrompt() {
        promptTextArea.clear();
    }

    // ---------------- Public ----------------
    @FXML private void onRefreshPublic() {
        String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        disableAll(true);
        runAsync(
                () -> quizDao.findPublic(q),
                items -> {
                    var rows = new java.util.ArrayList<PublicQuizRow>();
                    for (var it : items) rows.add(new PublicQuizRow(
                            it.quizId(), it.name(), it.subject(), it.description(), it.author()
                    ));
                    publicTable.getItems().setAll(rows);
                    disableAll(false);
                }
        );
    }

    @FXML private void onDownloadSelected() {
        PublicQuizRow sel = publicTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert(Alert.AlertType.INFORMATION, "No selection", "Pick a quiz row to download.");
            return;
        }
        disableAll(true);
        runAsync(
                () -> quizDao.getQuizById(sel.quizId()),
                quiz -> {
                    // Keep structured questions for saving
                    lastGeneratedQuestions = quiz.getQuestions();

                    // Keep a full text with answers (even if we hide them in preview)
                    lastGeneratedWithAnswers = renderer.renderAsText(quiz, true);

                    boolean showAns = publicIncludeAnswersCheck != null && publicIncludeAnswersCheck.isSelected();
                    String display = showAns ? lastGeneratedWithAnswers : stripAnswers(lastGeneratedWithAnswers);
                    previewArea.setText(display);

                    disableAll(false);
                }
        );
    }

    @FXML private void onPublicIncludeAnswersToggle() {
        if (lastGeneratedWithAnswers == null || lastGeneratedWithAnswers.isBlank()) return;
        boolean showAns = publicIncludeAnswersCheck != null && publicIncludeAnswersCheck.isSelected();
        String display = showAns ? lastGeneratedWithAnswers : stripAnswers(lastGeneratedWithAnswers);
        previewArea.setText(display);
    }

    // ---------------- Save & Export ----------------
    @FXML private void onSave() {
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

        if (nm == null || nm.isBlank()) {
            alert(Alert.AlertType.WARNING, "Missing fields", "Name is required.");
            return;
        }

        if (lastGeneratedQuestions == null || lastGeneratedQuestions.isEmpty()) {
            // Fallback: if user typed/edited manually, try parse the displayed text
            var parsed = parser.parse(previewArea.getText());
            if (parsed.isEmpty()) {
                alert(Alert.AlertType.WARNING, "No questions found",
                        "Generate a quiz first or use the expected MCQ format (A–E).");
                return;
            }
            lastGeneratedQuestions = parsed;
        }

        var user = com.app.studysnap.auth.Session.getCurrentUser();
        int createdBy = (user != null) ? user.getUserId() : 0;

        var quiz = new com.app.studysnap.model.Quiz(nm, subj, desc, is_private, createdBy);
        quiz.setQuestions(lastGeneratedQuestions);

        disableAll(true);
        runAsync(
                () -> { quizDao.addQuiz(quiz); return null; },
                ignored -> {
                    disableAll(false);
                    alert(Alert.AlertType.INFORMATION, "Saved", "Quiz saved successfully.");
                    onRefreshPublic();
                }
        );
    }

    @FXML private void onExportPdf() {
        boolean withAnswers = exportWithAnswersCheck != null && exportWithAnswersCheck.isSelected();
        exportPdfInternal(withAnswers);
    }

    private void exportPdfInternal(boolean withAnswers) {
        String txt = buildExportText(withAnswers);
        if (txt == null || txt.isBlank()) {
            alert(Alert.AlertType.WARNING, "Nothing to export", "Generate or load a quiz first.");
            return;
        }

        // Save As dialog defaulting to Downloads
        var chooser = new javafx.stage.FileChooser();
        chooser.setTitle(withAnswers ? "Export Quiz as PDF (with answers)" : "Export Quiz as PDF");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF", "*.pdf"));
        var suggested = defaultDownloadsFile(withAnswers ? "quiz-with-answers" : "quiz");
        if (suggested.getParentFile().exists()) chooser.setInitialDirectory(suggested.getParentFile());
        chooser.setInitialFileName(suggested.getName());

        var owner = (tabPane != null && tabPane.getScene() != null) ? tabPane.getScene().getWindow() : null;
        java.io.File dest = chooser.showSaveDialog(owner);
        if (dest == null) return;

        disableAll(true);
        runAsync(
                () -> { pdfExporter.export(txt, dest); return dest; },
                out -> {
                    disableAll(false);
                    alert(Alert.AlertType.INFORMATION, "Exported", "PDF saved to:\n" + out.getAbsolutePath());
                }
        );
    }

    // ---------------- Helpers ----------------
    private void disableAll(boolean busy) {
        progress.setVisible(busy);
        tabPane.setDisable(busy);
    }

    private String buildExportText(boolean withAnswers) {
        if (!withAnswers) {
            // Export exactly what's visible
            return previewArea.getText();
        }
        // Prefer the full version we stored during generation
        if (lastGeneratedWithAnswers != null && !lastGeneratedWithAnswers.isBlank()) {
            return lastGeneratedWithAnswers;
        }
        // If we have structured questions, render a full version with answers
        if (lastGeneratedQuestions != null && !lastGeneratedQuestions.isEmpty()) {
            var q = new com.app.studysnap.model.Quiz("Export", null, null, true, 0);
            q.setQuestions(lastGeneratedQuestions);
            return renderer.renderAsText(q, true);
        }
        // Fallback: use whatever is on screen (may not contain answers)
        return previewArea.getText();
    }

    private java.io.File defaultDownloadsFile(String baseName) {
        var downloads = new java.io.File(System.getProperty("user.home"), "Downloads");
        if (!downloads.exists() || !downloads.isDirectory()) {
            downloads = new java.io.File(System.getProperty("user.home"));
        }
        var ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return new java.io.File(downloads, baseName + "-" + ts + ".pdf");
    }

    // Hide answers if required
    private String stripAnswers(String text) {
        if (text == null) return null;
        // Remove lines starting with "Answer:" (case-insensitive, tolerant spacing)
        return text.replaceAll("(?im)^\\s*Answer:\\s*.*\\R?", "");
    }

    // Async to run work off the UI thread and switch back on success
    private <T> void runAsync(java.util.concurrent.Callable<T> background, java.util.function.Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return background.call(); }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> alert(Alert.AlertType.ERROR, "Error", String.valueOf(task.getException())));
        new Thread(task, "quiz-bg").start();
    }

    private void alert(Alert.AlertType type, String header, String content) {
        Alert a = new Alert(type, content, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    public record PublicQuizRow(int quizId, String name, String subject, String description, String author) {
        public String getName() { return name; }
        public String getSubject() { return subject; }
        public String getDescription() { return description; }
        public String getAuthor() { return author; }
    }
}