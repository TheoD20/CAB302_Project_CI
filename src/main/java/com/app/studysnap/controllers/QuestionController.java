package com.app.studysnap.controllers;

import com.app.studysnap.model.Question;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Label;


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

    public void setData(Question question){
        question_content.setText(question.getQuestion());
        option1.setText(question.getOption1());
        option2.setText(question.getOption2());
        option3.setText(question.getOption3());
        option4.setText(question.getOption4());
        option5.setText(question.getOption5());
    }

    @FXML
    public void initialize() {

    }
}
