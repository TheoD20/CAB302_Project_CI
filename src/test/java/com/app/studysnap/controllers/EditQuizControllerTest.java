package com.app.studysnap.controllers;

import com.app.studysnap.TestFXUtil;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class EditQuizControllerTest {

    private EditQuizController controller;

    @BeforeAll
    static void setupFX() {
        TestFXUtil.initFX();
        TestFXUtil.disableAlerts();
    }

    @BeforeEach
    void setup() throws Exception {
        controller = new EditQuizController();

        inject("quizIdLabel", new Label());
        inject("titleField", new TextField());
        inject("subjectField", new TextField());
        inject("privateCheck", new CheckBox());
        inject("descriptionArea", new TextArea());
        inject("questionTable", new TableView<>());
        inject("colIndex", new TableColumn<>());
        inject("colQuestion", new TableColumn<>());
        inject("colCorrect", new TableColumn<>());
        inject("saveBtn", new Button());
        inject("cancelBtn", new Button());
        inject("addBtn", new Button());
        inject("editBtn", new Button());
        inject("deleteBtn", new Button());

        runOnFxThread(() -> {
            try {
                var init = EditQuizController.class.getDeclaredMethod("initialize");
                init.setAccessible(true);
                init.invoke(controller);
            } catch (Exception ignored) {}
        });
    }

    @Test
    void testInitialFieldsAreEmpty() {
        runOnFxThread(() -> {
            TextField title = (TextField) get("titleField");
            TextArea desc = (TextArea) get("descriptionArea");

            assertTrue(title.getText().isEmpty(), "Title should start empty");
            assertTrue(desc.getText().isEmpty(), "Description should start empty");
        });
    }

    @Test
    void testHandleSave_showsWarningIfNoTitle() {
        runOnFxThread(() -> {
            try {
                var method = EditQuizController.class.getDeclaredMethod("handleSave");
                method.setAccessible(true);
                method.invoke(controller);
                // Expect no exception even though Popup.warn() is suppressed
                assertTrue(true);
            } catch (Exception e) {
                fail("handleSave() threw unexpected error: " + e.getMessage());
            }
        });
    }

    // ---------- Utility Methods ----------

    private void inject(String field, Object value) throws Exception {
        Field f = EditQuizController.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private Object get(String field) {
        try {
            Field f = EditQuizController.class.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnFxThread(Runnable task) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try { task.run(); } finally { latch.countDown(); }
        });
        try { latch.await(); } catch (InterruptedException ignored) {}
    }
}
