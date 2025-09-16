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

public class QuestionController {

    @FXML private Label question_content;
    @FXML private RadioButton option1;
    @FXML private RadioButton option2;
    @FXML private RadioButton option3;
    @FXML private RadioButton option4;
    @FXML private RadioButton option5;

    @FXML private VBox optionsBox;
    @FXML private Label correctFooter;

    @FXML private AnchorPane root;

    private Question question; // store the current question
    private final ToggleGroup optionsGroup = new ToggleGroup();

    public Node getRootNode() {
        return root;
    }

    public Question getQuestion() {
        return this.question;
    }

    public void setData(Question question){
        this.question = question; // must store it

        question_content.setText(safe(question.getQuestion()));
        option1.setText(safe(question.getOption1()));
        option2.setText(safe(question.getOption2()));
        option3.setText(safe(question.getOption3()));
        option4.setText(safe(question.getOption4()));
        option5.setText(safe(question.getOption5()));

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

    // Returns the index of the selected option (1–5), or -1 if none selected
    public int getSelectedOptionIndex(){
        if(optionsGroup.getSelectedToggle() == null) return -1;
        if(optionsGroup.getSelectedToggle() == option1) return 1;
        if(optionsGroup.getSelectedToggle() == option2) return 2;
        if(optionsGroup.getSelectedToggle() == option3) return 3;
        if(optionsGroup.getSelectedToggle() == option4) return 4;
        if(optionsGroup.getSelectedToggle() == option5) return 5;
        return -1; // set default to returning -1 meaning nothing selected.
    }

    // Check the correctness
    public boolean isCorrect(){
        return getSelectedOptionIndex() == question.getCorrectOption();
    }

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

    public int getOptionCount() { return 5; }

    public String getOptionText(int index) {
        RadioButton rb = getOptionByIndex(index);
        return rb == null ? null : rb.getText();
    }

    public boolean isOptionSelected(int index) {
        RadioButton rb = getOptionByIndex(index);
        return rb != null && rb.isSelected();
    }

    public boolean isOptionCorrect(int index) {
        return index == getCorrectIndex();
    }

    public int getCorrectIndex() {
        return question.getCorrectOption();
    }

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

    private void clearOptionStyles() {
        resetStyle(option1);
        resetStyle(option2);
        resetStyle(option3);
        resetStyle(option4);
        resetStyle(option5);
    }

    private void resetStyle(RadioButton rb) {
        if (rb == null) return;
        rb.setStyle("");

        rb.getStyleClass().removeAll("opt-line", "opt-selected-correct", "opt-selected-wrong", "opt-blank", "opt-correct-answer");
        rb.getStyleClass().add("opt-line");
    }

    private void addStyleClass(RadioButton rb, String cls) {
        if (rb == null) return;
        if (!rb.getStyleClass().contains(cls)) {
            rb.getStyleClass().add(cls);
        }
    }

    private static String safe(String s) { return (s == null) ? "" : s; }
}