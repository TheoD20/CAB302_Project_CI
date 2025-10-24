package com.app.studysnap.controllers;

import com.app.studysnap.exceptions.DataAccessException;
import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.SqliteQuestionDAO;
import com.app.studysnap.model.SqliteQuizDAO;
import com.app.studysnap.services.Navigator;
import com.app.studysnap.services.Popup;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.List;

import static com.app.studysnap.services.TextParser.isBlank;
import static com.app.studysnap.services.TextParser.trim;

/**
 * Controller responsible for editing a {@link Quiz}, including quiz metadata and its questions.
 */
public class EditQuizController {

    // Topbar
    @FXML private Label quizIdLabel;

    // Form
    @FXML private TextField titleField;
    @FXML private TextField subjectField;
    @FXML private CheckBox privateCheck;
    @FXML private TextArea descriptionArea;

    // Questions table
    @FXML private TableView<Question> questionTable;
    @FXML private TableColumn<Question, Number> colIndex;
    @FXML private TableColumn<Question, String> colQuestion;
    @FXML private TableColumn<Question, String> colCorrect;

    // Buttons
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;

    // State
    private final SqliteQuizDAO quizDAO;
    private final SqliteQuestionDAO questionDAO;
    private Quiz quiz;
    private final ObservableList<Question> questions = FXCollections.observableArrayList();

    /**
     * Creates a new {@code EditQuizController} and initializes DAO dependencies.
     */
    public EditQuizController() {
        SqliteQuizDAO qd;
        SqliteQuestionDAO qsd;
        try { qd = new SqliteQuizDAO(); } catch (Throwable t) { qd = null; }
        try { qsd = new SqliteQuestionDAO(); } catch (Throwable t) { qsd = null; }
        this.quizDAO = qd;
        this.questionDAO = qsd;
    }

    /**
     * JavaFX initialization: configures table columns.
     */
    @FXML
    private void initialize() {
        if (colIndex != null) {
            colIndex.setCellValueFactory(data ->
                    new ReadOnlyObjectWrapper<>(questionTable.getItems().indexOf(data.getValue()) + 1));
            colIndex.setSortable(false);
        }
        if (colQuestion != null) {
            colQuestion.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(trim(c.getValue().getQuestion())));
            colQuestion.setCellFactory(col -> {
                TableCell<Question, String> cell = new TableCell<>() {
                    private final Label label = new Label();
                    { label.setWrapText(true); label.setMaxWidth(Double.MAX_VALUE); }
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            label.setText(item);
                            label.setMinHeight(Region.USE_PREF_SIZE);
                            setGraphic(label);
                        }
                    }
                };
                return cell;
            });
        }
        if (colCorrect != null) {
            colCorrect.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                    "Option " + Math.max(1, c.getValue().getCorrectOption())
            ));
        }

        if (questionTable != null) {
            questionTable.setItems(questions);
            questionTable.setFixedCellSize(-1);
            questionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }
    }

    /**
     * Binds a {@link Quiz} instance to the form and loads its questions.
     *
     * @param quiz the quiz to edit; if {@code null}, an error popup is shown and the method returns
     */
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        if (quiz == null) {
            Popup.error("No quiz provided.");
            return;
        }

        if (quizIdLabel != null) {
            quizIdLabel.setText("#" + quiz.getQuizId());
        }

        titleField.setText(trim(quiz.getTitle()));
        subjectField.setText(trim(quiz.getSubject()));
        privateCheck.setSelected(quiz.get_is_private());
        descriptionArea.setText(trim(quiz.getDescription()));

        refreshQuestions();
    }

    /**
     * Reloads the questions for the current quiz into the list.
     * Safely handles if {@code quiz} or {@code questionDAO} is {@code null}.
     */
    private void refreshQuestions() {
        questions.clear();
        if (quiz == null || questionDAO == null) return;
        List<Question> list = questionDAO.getQuestionsForQuiz(quiz.getQuizId());
        if (list != null) questions.addAll(list);
    }

    /**
     * Persists quiz metadata changes through {@link SqliteQuizDAO} and returns to the dashboard.
     * Validation: title is required (non-blank).
     * Any errors show a popup with details.
     */
    @FXML
    private void handleSave() {
        if (quiz == null) return;
        String title = titleField.getText();
        if (isBlank(title)) {
            Popup.warn("Title is required.");
            return;
        }
        // update quiz object
        try {
            quiz.setTitle(title);
            quiz.setSubject(subjectField.getText());
            quiz.set_is_private(privateCheck.isSelected());
            quiz.setDescription(descriptionArea.getText());

            if (quizDAO == null) throw new DataAccessException("Quiz DAO unavailable.");
            quizDAO.updateQuiz(quiz); // assumes this method exists in your DAO

            Popup.info("Quiz saved.");
            Navigator.goTo(cancelBtn, "dashboard.fxml");
        } catch (Exception ex) {
            Popup.error("Could not save quiz:\n" + ex.getMessage());
        }
    }

    /**
     * Cancels editing (with confirmation) and navigates back to the dashboard.
     * @throws IOException propagated if navigation fails
     */
    @FXML
    private void handleCancel() throws IOException {
        if (!Popup.confirm("Discard changes", "Return to Home without saving?")) return;
        Navigator.goTo(cancelBtn, "dashboard.fxml");
    }

    /**
     * Opens the add-question dialog, persists the new question via DAO, and refreshes the table.
     * Any errors show a popup with details.
     */
    @FXML
    private void handleAddQuestion() {
        Question q = questionDialog(null);
        if (q == null) return;
        try {
            q.setQuizId(quiz.getQuizId());
            if (questionDAO == null) throw new DataAccessException("Question DAO unavailable.");
            questionDAO.addQuestion(q);
            refreshQuestions();
            Popup.info("Question added.");
        } catch (Exception ex) {
            Popup.error("Could not add question:\n" + ex.getMessage());
        }
    }

    /**
     * Edits the selected question using a dialog and persists changes via DAO.
     * Any errors show a popup with details.
     */
    @FXML
    private void handleEditQuestion() {
        Question sel = questionTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Popup.warn("Select a question to edit.");
            return;
        }
        Question edited = questionDialog(sel);
        if (edited == null) return;

        try {
            sel.setQuestion(edited.getQuestion());
            sel.setOption1(edited.getOption1());
            sel.setOption2(edited.getOption2());
            sel.setOption3(edited.getOption3());
            sel.setOption4(edited.getOption4());
            sel.setOption5(edited.getOption5());
            sel.setCorrectOption(edited.getCorrectOption());

            if (questionDAO == null) throw new DataAccessException("Question DAO unavailable.");
            questionDAO.updateQuestion(sel);
            questionTable.refresh();
            Popup.info("Question updated.");
        } catch (Exception ex) {
            Popup.error("Could not update question:\n" + ex.getMessage());
        }
    }

    /**
     * Deletes the selected question after confirmation and updates the table.
     * Any errors show a popup with details.
     */
    @FXML
    private void handleDeleteQuestion() {
        Question sel = questionTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            Popup.warn("Select a question to delete.");
            return;
        }
        if (!Popup.confirm("Delete question", "Are you sure you want to delete this question?")) return;

        try {
            if (questionDAO == null) throw new DataAccessException("Question DAO unavailable.");
            questionDAO.deleteQuestion(sel.getQuestionId());
            questions.remove(sel);
            Popup.info("Question deleted.");
        } catch (Exception ex) {
            Popup.error("Could not delete question:\n" + ex.getMessage());
        }
    }

    /**
     * Builds and displays the add/edit question dialog, returning the composed {@link Question}
     * when the user confirms, or {@code null} if canceled/invalid.
     * @param existing an existing question to edit, or {@code null} to create a new one
     * @return the resulting {@link Question}, or {@code null} if the dialog is canceled
     */
    private Question questionDialog(Question existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Question" : "Edit Question");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8); grid.setPadding(new Insets(8));

        TextArea qText = new TextArea(existing == null ? "" : trim(existing.getQuestion()));
        qText.setPromptText("Question text"); qText.setWrapText(true); qText.setPrefRowCount(3);

        TextField o1 = new TextField(existing == null ? "" : trim(existing.getOption1()));
        TextField o2 = new TextField(existing == null ? "" : trim(existing.getOption2()));
        TextField o3 = new TextField(existing == null ? "" : trim(existing.getOption3()));
        TextField o4 = new TextField(existing == null ? "" : trim(existing.getOption4()));
        TextField o5 = new TextField(existing == null ? "" : trim(existing.getOption5()));

        ComboBox<Integer> correct = new ComboBox<>(FXCollections.observableArrayList(1,2,3,4,5));
        correct.setValue(existing == null ? 1 : Math.max(1, existing.getCorrectOption()));
        correct.setEditable(false);

        int r = 0;
        grid.add(new Label("Question"), 0, r); grid.add(qText, 1, r++);
        grid.add(new Label("Option 1"), 0, r); grid.add(o1, 1, r++);
        grid.add(new Label("Option 2"), 0, r); grid.add(o2, 1, r++);
        grid.add(new Label("Option 3"), 0, r); grid.add(o3, 1, r++);
        grid.add(new Label("Option 4"), 0, r); grid.add(o4, 1, r++);
        grid.add(new Label("Option 5"), 0, r); grid.add(o5, 1, r++);
        grid.add(new Label("Correct option"), 0, r); grid.add(correct, 1, r++);

        dialog.getDialogPane().setContent(grid);

        // validate title
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (isBlank(qText.getText())) {
                Popup.warn("Question text cannot be empty.");
                evt.consume();
            }
        });

        var res = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (res != ButtonType.OK) return null;

        Question out = new Question();
        if (existing != null) {
            out.setQuestionId(existing.getQuestionId());
            out.setQuizId(existing.getQuizId());
        } else if (quiz != null) {
            out.setQuizId(quiz.getQuizId());
        }
        out.setQuestion(qText.getText());
        out.setOption1(o1.getText());
        out.setOption2(o2.getText());
        out.setOption3(o3.getText());
        out.setOption4(o4.getText());
        out.setOption5(o5.getText());
        out.setCorrectOption(correct.getValue());
        return out;
    }
}