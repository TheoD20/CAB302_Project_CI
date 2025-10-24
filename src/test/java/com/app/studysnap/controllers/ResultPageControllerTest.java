package com.app.studysnap.controllers;

import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ResultPageControllerTest {

    private ResultPageController controller;


    @BeforeAll
    static void initFX() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await(2, TimeUnit.SECONDS);
        } catch (IllegalStateException ignored) {
            // already started
        }
    }

    @BeforeEach
    void setup() throws Exception {
        controller = new ResultPageController();


        inject("scoreLabel", new Label());
        inject("timeTaken", new Label());
        inject("resultChart", new PieChart());
        inject("reviewLayout", new VBox());
        inject("resultBar", new BarChart<>(new CategoryAxis(), new NumberAxis()));


        Platform.runLater(() -> {
            try {
                Method m = ResultPageController.class.getDeclaredMethod("initialize");
                m.setAccessible(true);
                m.invoke(controller);
            } catch (Exception ignored) {}
        });
        Thread.sleep(100);
    }


    private void inject(String name, Object value) throws Exception {
        Field f = ResultPageController.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private Object getField(String name) throws Exception {
        Field f = ResultPageController.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(controller);
    }

    private void sleepFx() {
        try { Thread.sleep(150); } catch (InterruptedException ignored) {}
    }



    @Test
    void testInitializeSetsDefaults() throws Exception {

        Platform.runLater(() -> {
            try {
                Method m = ResultPageController.class.getDeclaredMethod("initialize");
                m.setAccessible(true);
                m.invoke(controller);
            } catch (Exception ignored) {}
        });
        sleepFx();

        Label scoreLabel = (Label) getField("scoreLabel");
        Label timeTaken = (Label) getField("timeTaken");
        PieChart chart = (PieChart) getField("resultChart");
        VBox review = (VBox) getField("reviewLayout");

        assertEquals("--/--", scoreLabel.getText());
        assertEquals("00:00", timeTaken.getText());
        assertTrue(chart.getData().isEmpty());
        assertTrue(review.getChildren().isEmpty());
    }

    @Test
    void testSetResultUpdatesScoreAndChart() throws Exception {
        // ✅ Use a valid Quiz constructor
        Quiz quiz = new Quiz("Sample Quiz", "Math", "Practice test", false, 1);
        controller.setQuiz(quiz);


        Question q1 = new Question();
        q1.setQuestion("What is 2+2?");
        q1.setCorrectOption(2);

        Question q2 = new Question();
        q2.setQuestion("Capital of France?");
        q2.setCorrectOption(3);


        QuestionController qc1 = new QuestionController();
        QuestionController qc2 = new QuestionController();
        injectQuestionUI(qc1);
        injectQuestionUI(qc2);
        qc1.setData(q1);
        qc2.setData(q2);

        Platform.runLater(() -> controller.setResult(1, 2, List.of(qc1, qc2), 75));
        sleepFx();

        Label scoreLabel = (Label) getField("scoreLabel");
        Label timeTaken = (Label) getField("timeTaken");
        PieChart chart = (PieChart) getField("resultChart");
        VBox review = (VBox) getField("reviewLayout");
        BarChart<?, ?> bar = (BarChart<?, ?>) getField("resultBar");

        assertEquals("1/2", scoreLabel.getText());
        assertEquals("00:01:15", timeTaken.getText());
        assertEquals(2, chart.getData().size());
        assertFalse(review.getChildren().isEmpty());
        assertFalse(bar.getData().isEmpty());
    }


    private void injectQuestionUI(QuestionController qc) throws Exception {
        for (Field f : QuestionController.class.getDeclaredFields()) {
            f.setAccessible(true);
            Class<?> t = f.getType();
            if (t == javafx.scene.control.RadioButton.class)
                f.set(qc, new javafx.scene.control.RadioButton());
            else if (t == Label.class)
                f.set(qc, new Label());
            else if (t == VBox.class)
                f.set(qc, new VBox());
            else if (t == javafx.scene.layout.AnchorPane.class)
                f.set(qc, new javafx.scene.layout.AnchorPane());
        }

        Platform.runLater(() -> {
            try {
                Method m = QuestionController.class.getDeclaredMethod("initialize");
                m.setAccessible(true);
                m.invoke(qc);
            } catch (Exception ignored) {}
        });
        sleepFx();
    }
}
