package com.app.studysnap.controllers;

import com.app.studysnap.model.Question;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class QuestionControllerTest {

    private QuestionController controller;

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already started — safe to ignore
        }
    }


    @BeforeEach
    void setup() throws Exception {
        controller = new QuestionController();

        // Inject all FXML fields via reflection
        injectField("root", new AnchorPane());
        injectField("optionsBox", new VBox());
        injectField("question_content", new Label());
        injectField("option1", new RadioButton());
        injectField("option2", new RadioButton());
        injectField("option3", new RadioButton());
        injectField("option4", new RadioButton());
        injectField("option5", new RadioButton());
        injectField("correctFooter", new Label());

        controller.initialize();
    }

    private void injectField(String name, Object value) throws Exception {
        Field f = QuestionController.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(controller, value);
    }

    @Test
    void testInitializeSetsToggleGroup() throws Exception {
        RadioButton o1 = getRadio("option1");
        RadioButton o2 = getRadio("option2");
        RadioButton o3 = getRadio("option3");

        assertNotNull(o1.getToggleGroup());
        assertEquals(o1.getToggleGroup(), o2.getToggleGroup());
        assertEquals(o1.getToggleGroup(), o3.getToggleGroup());
    }

    @Test
    void testSetDataPopulatesTextsAndResets() throws Exception {
        Question q = new Question();
        q.setQuestion("What is Java?");
        q.setOption1("Fruit");
        q.setOption2("Coffee");
        q.setOption3("Programming");
        q.setOption4("Planet");
        q.setOption5("Car");
        q.setCorrectOption(3);

        Platform.runLater(() -> controller.setData(q));
        sleepFx();

        Label questionLabel = (Label) getField("question_content");
        assertEquals("What is Java?", questionLabel.getText());

        RadioButton o1 = getRadio("option1");
        assertEquals("Fruit", o1.getText());
        assertFalse(o1.isSelected());

        Label footer = (Label) getField("correctFooter");
        assertFalse(footer.isVisible());
    }

    @Test
    void testGetSelectedOptionIndexAndIsCorrect() throws Exception {
        Question q = makeSampleQuestion();
        Platform.runLater(() -> controller.setData(q));
        sleepFx();

        assertEquals(-1, controller.getSelectedOptionIndex());

        RadioButton o3 = getRadio("option3");
        Platform.runLater(() -> o3.setSelected(true));
        sleepFx();

        assertEquals(3, controller.getSelectedOptionIndex());
        assertTrue(controller.isCorrect());
    }

    @Test
    void testShowResultCorrectSelectionHidesFooter() throws Exception {
        Question q = makeSampleQuestion();
        Platform.runLater(() -> controller.setData(q));
        sleepFx();

        Platform.runLater(() -> controller.showResult(3, 3));
        sleepFx();

        RadioButton o3 = getRadio("option3");
        assertTrue(o3.getStyleClass().contains("opt-selected-correct"));

        Label footer = (Label) getField("correctFooter");
        assertFalse(footer.isVisible());
    }

    @Test
    void testShowResultWrongSelectionShowsFooter() throws Exception {
        Question q = makeSampleQuestion();
        Platform.runLater(() -> controller.setData(q));
        sleepFx();

        Platform.runLater(() -> controller.showResult(2, 3));
        sleepFx();

        RadioButton o2 = getRadio("option2");
        RadioButton o3 = getRadio("option3");
        Label footer = (Label) getField("correctFooter");

        assertTrue(o2.getStyleClass().contains("opt-selected-wrong"));
        assertTrue(o3.getStyleClass().contains("opt-correct-answer"));
        assertTrue(footer.isVisible());
        assertTrue(footer.getText().contains("Correct answer"));
    }


    private Question makeSampleQuestion() {
        Question q = new Question();
        q.setQuestion("Which is correct?");
        q.setOption1("One");
        q.setOption2("Two");
        q.setOption3("Three");
        q.setOption4("Four");
        q.setOption5("Five");
        q.setCorrectOption(3);
        return q;
    }

    private RadioButton getRadio(String name) throws Exception {
        return (RadioButton) getField(name);
    }

    private Object getField(String name) throws Exception {
        Field f = QuestionController.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(controller);
    }

    private void sleepFx() {
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
    }
}
