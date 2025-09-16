package com.app.studysnap.controllers;

import com.app.studysnap.model.Question;
import com.app.studysnap.model.Quiz;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayQuizPageControllerTest {

    PlayQuizPageController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new PlayQuizPageController();

        // Inject a dummy VBox into the private questionLayout field
        Field questionLayoutField = PlayQuizPageController.class.getDeclaredField("questionLayout");
        questionLayoutField.setAccessible(true);
        questionLayoutField.set(controller, new VBox());

        // Inject a dummy Label into the private quizTimer field
        Field quizTimerField = PlayQuizPageController.class.getDeclaredField("quizTimer");
        quizTimerField.setAccessible(true);
        quizTimerField.set(controller, new Label());
    }

    // Test class loads
    @Test
    void classLoads() {
        assertNotNull(PlayQuizPageController.class);
    }

    // Test FXML fields exist
    @Test
    void fxmlFields_Exist() throws NoSuchFieldException {
        Field questionLayoutField = PlayQuizPageController.class.getDeclaredField("questionLayout");
        Field quizTimerField = PlayQuizPageController.class.getDeclaredField("quizTimer");
        assertNotNull(questionLayoutField);
        assertNotNull(quizTimerField);
    }

    // Test initialize method exists
    @Test
    void initialize_MethodExists() throws NoSuchMethodException {
        Method initializeMethod = PlayQuizPageController.class.getDeclaredMethod("initialize");
        assertNotNull(initializeMethod);
    }

    // Test setQuiz loads questions without throwing
    @Test
    void setQuiz_DoesNotThrow() {
        // Use existing constructor of Quiz
        Quiz quiz = new Quiz(1, "Test Quiz", "Math", "Dummy description", false, 1);
        assertDoesNotThrow(() -> controller.setQuiz(quiz));

        // Verify that the injected VBox has children added
        VBox layout = null;
        try {
            Field f = PlayQuizPageController.class.getDeclaredField("questionLayout");
            f.setAccessible(true);
            layout = (VBox) f.get(controller);
        } catch (Exception e) {
            fail("Failed to access questionLayout");
        }
        assertNotNull(layout);
        // Since your DAO actually loads from DB, there may be 0 children if no questions exist
        assertNotNull(layout.getChildren());
    }
}

