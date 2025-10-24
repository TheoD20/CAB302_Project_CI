package com.app.studysnap.controllers;

import com.app.studysnap.model.Question;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import static com.app.studysnap.services.TextParser.trim;

/**
 * Controller for a single multiple-choice question card.
 * <p>
 * Binds question text and up to five options into radio buttons, tracks the user's selection,
 * and renders result styling (correct/incorrect) after submission.
 * </p>
 */
public class QuestionController {

    // Fxml
    @FXML private Label question_content;
    @FXML private RadioButton option1;
    @FXML private RadioButton option2;
    @FXML private RadioButton option3;
    @FXML private RadioButton option4;
    @FXML private RadioButton option5;

    @FXML private VBox optionsBox;
    @FXML private Label correctFooter;

    @FXML private AnchorPane root;

    /** The question model displayed by this controller. */
    private Question question;

    /** Toggle group to ensure only one option is selected. */
    private final ToggleGroup optionsGroup = new ToggleGroup();

    /**
     * Default constructor:
     * Creates a new {@code QuestionController}.
     */
    public QuestionController() {}

    /**
     * Returns the root node of this card (for embedding in parent layouts).
     * @return the root {@link Node}
     */
    public Node getRootNode() {
        return root;
    }

    /**
     * Gets the active {@link Question} model.
     * @return the question model
     */
    public Question getQuestion() {
        return this.question;
    }

    /**
     * Populates the view with the given {@link Question} and resets selection/styling.
     * @param question the question to display
     */
    public void setData(Question question){
        this.question = question; // must store it

        question_content.setText(trim(question.getQuestion()));
        option1.setText(trim(question.getOption1()));
        option2.setText(trim(question.getOption2()));
        option3.setText(trim(question.getOption3()));
        option4.setText(trim(question.getOption4()));
        option5.setText(trim(question.getOption5()));

        // Reset footer visibility/content if present
        if (correctFooter != null) {
            correctFooter.setManaged(false);
            correctFooter.setVisible(false);
            correctFooter.setText("Correct answer: ");
        }

        // Reset option styles and clear selection for a fresh load
        clearOptionStyles();
        option1.setSelected(false);
        option2.setSelected(false);
        option3.setSelected(false);
        option4.setSelected(false);
        option5.setSelected(false);
    }

    /**
     * JavaFX initialization: wires radio buttons to a common {@link ToggleGroup} and
     * enables wrapping to fit responsive widths.
     */
    @FXML
    public void initialize() {
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);
        option5.setToggleGroup(optionsGroup);
        clearOptionStyles();

        final var widthSource = (optionsBox != null ? optionsBox.widthProperty() : root.widthProperty());
        if (question_content != null) {
            question_content.setWrapText(true);
            question_content.prefWidthProperty().bind(widthSource);
            question_content.setMinHeight(Region.USE_PREF_SIZE);
        }
        RadioButton[] rbs = { option1, option2, option3, option4, option5 };
        for (RadioButton rb : rbs) {
            if (rb == null) continue;
            rb.setWrapText(true);
            rb.prefWidthProperty().bind(widthSource);
            rb.setMinHeight(Region.USE_PREF_SIZE);
        }
    }

    /**
     * Returns the index of the selected option (1–5), or -1 if none selected.
     * @return selected option index, or -1
     */
    public int getSelectedOptionIndex(){
        if(optionsGroup.getSelectedToggle() == null) return -1;
        if(optionsGroup.getSelectedToggle() == option1) return 1;
        if(optionsGroup.getSelectedToggle() == option2) return 2;
        if(optionsGroup.getSelectedToggle() == option3) return 3;
        if(optionsGroup.getSelectedToggle() == option4) return 4;
        if(optionsGroup.getSelectedToggle() == option5) return 5;

        // set default to returning -1 meaning nothing selected.
        return -1;
    }

    /**
     * Checks if the current selection matches the question's correct option.
     * @return {@code true} if selected == correct; otherwise {@code false}
     */
    public boolean isCorrect(){
        return getSelectedOptionIndex() == question.getCorrectOption();
    }

    /**
     * Applies result styling: highlights the user's selection (green/red) and
     * outlines the true correct option. Optionally shows a footer with the correct text.
     * @param selectedIndex the option index the user selected (or -1)
     * @param correctIndex the correct option index
     */
    public void showResult(int selectedIndex, int correctIndex){
        // Keep user's selection; clear previous visual styles
        clearOptionStyles();

        // Re-apply only what the user picked
        RadioButton selectedBtn = getOptionByIndex(selectedIndex);
        if (selectedBtn != null) {
            selectedBtn.setSelected(true);
            if (selectedIndex == correctIndex) {
                addStyleClass(selectedBtn, "opt-selected-correct"); // green
            } else {
                addStyleClass(selectedBtn, "opt-selected-wrong");   // red
            }
        }

        // Softly outline the true correct option
        RadioButton correctBtn = getOptionByIndex(correctIndex);
        if (correctBtn != null) {
            addStyleClass(correctBtn, "opt-correct-answer"); // green border
        }

        boolean answeredCorrectly = (selectedIndex == correctIndex);
        if (!answeredCorrectly && correctFooter != null) {
            String correctText = getOptionText(correctIndex);
            correctFooter.setText("Correct answer: " + (correctText == null ? "" : correctText));
            correctFooter.setManaged(true);
            correctFooter.setVisible(true);
        } else if (correctFooter != null) {
            correctFooter.setManaged(false);
            correctFooter.setVisible(false);
        }
    }

    /**
     * Returns the label text for the specified option index, or {@code null} if out of range.
     * @param index option index (1–5)
     * @return option text or {@code null}
     */
    public String getOptionText(int index) {
        RadioButton rb = getOptionByIndex(index);
        return rb == null ? null : rb.getText();
    }

    /**
     * Resolves a radio button by its 1–5 index.
     * @param index option index (1–5)
     * @return the corresponding {@link RadioButton}, or {@code null} if out of range
     */
    private RadioButton getOptionByIndex(int index){
        return switch (index){
            case 1 -> option1;
            case 2 -> option2;
            case 3 -> option3;
            case 4 -> option4;
            case 5 -> option5;
            default -> null;
        };
    }

    /** Clears all styling classes from option buttons and applies the baseline class. */
    private void clearOptionStyles() {
        resetStyle(option1);
        resetStyle(option2);
        resetStyle(option3);
        resetStyle(option4);
        resetStyle(option5);
    }

    /** Resets the style for a single radio button to the default "opt-line" class. */
    private void resetStyle(RadioButton rb) {
        if (rb == null) return;
        rb.setStyle("");

        rb.getStyleClass().removeAll("opt-line", "opt-selected-correct", "opt-selected-wrong", "opt-blank", "opt-correct-answer");
        rb.getStyleClass().add("opt-line");
    }

    /** Adds a CSS style class to a radio button if not already present. */
    private void addStyleClass(RadioButton rb, String cls) {
        if (rb == null) return;
        if (!rb.getStyleClass().contains(cls)) {
            rb.getStyleClass().add(cls);
        }
    }
}