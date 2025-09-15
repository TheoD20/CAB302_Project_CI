package com.app.studysnap.controllers;

import com.app.studysnap.model.Question;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;


import java.util.ArrayList;
import java.util.List;

public class QuestionController {
    @FXML
    private Label question_content;
    @FXML
    private RadioButton option1;
    @FXML
    private RadioButton option2;
    @FXML
    private RadioButton option3;
    @FXML
    private RadioButton option4;
    @FXML
    private RadioButton option5;

    @FXML
    private AnchorPane root;
    private Question question; // store the current question
    private ToggleGroup optionsGroup = new ToggleGroup();


    public Node getRootNode(){
        return root;
    }

    public Question getQuestion(){
        return this.question;
    }
    public void setData(Question question){
        this.question = question; // must store it

        question_content.setText(question.getQuestion());
        option1.setText(question.getOption1());
        option2.setText(question.getOption2());
        option3.setText(question.getOption3());
        option4.setText(question.getOption4());
        option5.setText(question.getOption5());
    }

    @FXML
    public void initialize() {
        option1.setToggleGroup(optionsGroup);
        option2.setToggleGroup(optionsGroup);
        option3.setToggleGroup(optionsGroup);
        option4.setToggleGroup(optionsGroup);
        option5.setToggleGroup(optionsGroup);
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

    //Check the correctness.
    public boolean isCorrect(){
        return getSelectedOptionIndex() == question.getCorrectOption();
    }

    public void showResult(int selectedIndex, int correctIndex){
        // Clear selection
        option1.setSelected(false);
        option2.setSelected(false);
        option3.setSelected(false);
        option4.setSelected(false);
        option5.setSelected(false);

        // Reset styles
        option1.setStyle("");
        option2.setStyle("");
        option3.setStyle("");
        option4.setStyle("");
        option5.setStyle("");

        // Mark the user's selection
        RadioButton selectedBtn = getOptionByIndex(selectedIndex);
        if(selectedBtn != null){
            selectedBtn.setSelected(true);
            if(selectedIndex == correctIndex){
                selectedBtn.setStyle("-fx-mark-color: green; -fx-text-fill: green;");
            } else {
                selectedBtn.setStyle("-fx-mark-color: red; -fx-text-fill: red;");
            }
        }

        RadioButton correctBtn = getOptionByIndex(correctIndex);
        if(correctBtn != null){
            correctBtn.setStyle("-fx-mark-color: green; -fx-text-fill: green;");
            correctBtn.setSelected(true);
        }
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
}
