package com.app.studysnap.controllers;

import com.app.studysnap.model.Question;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestionControllerTest {

    private QuestionController controller;

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = QuestionController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new QuestionController();

        // Inject JavaFX controls into private fields
        setPrivateField("question_content", new Label());
        setPrivateField("option1", new RadioButton());
        setPrivateField("option2", new RadioButton());
        setPrivateField("option3", new RadioButton());
        setPrivateField("option4", new RadioButton());
        setPrivateField("option5", new RadioButton());
    }

    @Test
    void testSetDataPopulatesUI() {
        Question q = new Question(
                1, 101, "What is Java?",
                "A programming language", "A coffee", "An island",
                "A car brand", "All of the above", 1
        );

        controller.setData(q);

        // Verify values
        assertEquals("What is Java?", ((Label) getPrivateValue("question_content")).getText());
        assertEquals("A programming language", ((RadioButton) getPrivateValue("option1")).getText());
        assertEquals("A coffee", ((RadioButton) getPrivateValue("option2")).getText());
        assertEquals("An island", ((RadioButton) getPrivateValue("option3")).getText());
        assertEquals("A car brand", ((RadioButton) getPrivateValue("option4")).getText());
        assertEquals("All of the above", ((RadioButton) getPrivateValue("option5")).getText());
    }

    private Object getPrivateValue(String fieldName) {
        try {
            Field field = QuestionController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
