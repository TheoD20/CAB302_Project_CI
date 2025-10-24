package com.app.studysnap.controllers;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuizGeneratorControllerTest {

    private QuizGeneratorController controller;

    @BeforeAll
    void initJavaFX() throws InterruptedException {

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already started
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new QuizGeneratorController();

        inject("previewArea", new TextArea());
        inject("progress", new ProgressIndicator());
        inject("tabPane", new TabPane());
        inject("fileDropZone", new StackPane());
        inject("includeAnswersUpload", new CheckBox());
        inject("includeAnswersPaste", new CheckBox());
        inject("includeAnswersPrompt", new CheckBox());
        inject("publicIncludeAnswersCheck", new CheckBox());
    }



    @Test
    @Order(1)
    void testStripAnswers_removesAnswerLines() throws InterruptedException {
        runOnFxThread(() -> {
            String input = """
                    Q1: What is 2+2?
                    Answer: 4
                    Q2: What is 3+3?
                    Answer: 6
                    """;

            String expected = """
                    Q1: What is 2+2?
                    Q2: What is 3+3?
                    """;

            try {
                String result = (String) invokePrivate("stripAnswers", String.class, input);

                // Normalize whitespace to avoid cross-platform line ending issues
                String normalizedExpected = expected.replaceAll("\\s+", "");
                String normalizedResult = result.replaceAll("\\s+", "");

                assertEquals(normalizedExpected, normalizedResult,
                        "stripAnswers() should remove all 'Answer:' lines correctly");
            } catch (Exception e) {
                fail(e);
            }
        });
    }

    @Test
    @Order(2)
    void testSyncIncludeAnswerChecks_setsAllCheckboxes() throws InterruptedException {
        runOnFxThread(() -> {
            CheckBox upload = (CheckBox) get("includeAnswersUpload");
            CheckBox paste = (CheckBox) get("includeAnswersPaste");
            CheckBox prompt = (CheckBox) get("includeAnswersPrompt");
            CheckBox pub = (CheckBox) get("publicIncludeAnswersCheck");


            invokePrivateWithBoolean("syncIncludeAnswerChecks", true);
            assertTrue(upload.isSelected());
            assertTrue(paste.isSelected());
            assertTrue(prompt.isSelected());
            assertTrue(pub.isSelected());


            invokePrivateWithBoolean("syncIncludeAnswerChecks", false);
            assertFalse(upload.isSelected());
            assertFalse(paste.isSelected());
            assertFalse(prompt.isSelected());
            assertFalse(pub.isSelected());
        });
    }


    private void inject(String fieldName, Object value) throws Exception {
        Field f = QuizGeneratorController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private Object get(String fieldName) {
        try {
            Field f = QuizGeneratorController.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object invokePrivate(String methodName, Class<?> paramType, Object arg) {
        try {
            var m = QuizGeneratorController.class.getDeclaredMethod(methodName, paramType);
            m.setAccessible(true);
            return m.invoke(controller, arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokePrivateWithBoolean(String methodName, boolean param) {
        try {
            var m = QuizGeneratorController.class.getDeclaredMethod(methodName, boolean.class);
            m.setAccessible(true);
            m.invoke(controller, param);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnFxThread(Runnable task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } finally {
                latch.countDown();
            }
        });
        latch.await();
    }
}
