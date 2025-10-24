package com.app.studysnap.controllers;

import com.app.studysnap.auth.Session;
import com.app.studysnap.model.User;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class ProfileControllerTest {

    private static boolean javaFxStarted = false;
    private ProfileController controller;
    private User testUser;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        if (!javaFxStarted) {
            javaFxStarted = true;
            try {
                CountDownLatch latch = new CountDownLatch(1);
                Platform.startup(() -> {
                    // do nothing
                });
                latch.countDown();
            } catch (IllegalStateException e) {
                // Already started — ignore
            }
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new ProfileController();

        inject("usernameField", new TextField());
        inject("emailField", new TextField());
        inject("displayName", new Label());
        inject("statusLabel", new Label());
        inject("saveButton", new Button());
        inject("deleteButton", new Button());
        inject("providerLabel", new Label());
        inject("providerValue", new Label());

        testUser = new User(1, "Alice", "alice@example.com", "pass", "LOCAL", null);
        Session.setCurrentUser(testUser);

        runOnFxThread(() -> {
            try {
                var initMethod = ProfileController.class.getDeclaredMethod("initialize");
                initMethod.setAccessible(true);
                initMethod.invoke(controller);
            } catch (Exception e) {
                fail("Failed to invoke initialize(): " + e.getMessage());
            }
        });
    }

    @AfterEach
    void tearDown() {
        Session.clear();
    }

    @Test
    void testInitialValuesPopulated() {
        runOnFxThread(() -> {
            TextField username = (TextField) getField("usernameField");
            TextField email = (TextField) getField("emailField");
            Label display = (Label) getField("displayName");
            Label provider = (Label) getField("providerValue");

            assertEquals("Alice", username.getText());
            assertEquals("alice@example.com", email.getText());
            assertEquals("Alice", display.getText());
            assertEquals("LOCAL", provider.getText());
        });
    }

    @Test
    void testSaveButtonDisabledInitially() {
        runOnFxThread(() -> {
            Button saveBtn = (Button) getField("saveButton");
            assertTrue(saveBtn.isDisable(), "Save button should be disabled initially");
        });
    }

    @Test
    void testValidateDirtyEnablesSaveButton() {
        runOnFxThread(() -> {
            TextField username = (TextField) getField("usernameField");
            Button saveBtn = (Button) getField("saveButton");

            username.setText("Bob");
            try {
                var validateDirty = ProfileController.class.getDeclaredMethod("validateDirty");
                validateDirty.setAccessible(true);
                validateDirty.invoke(controller);
            } catch (Exception e) {
                fail("Failed to invoke validateDirty: " + e.getMessage());
            }

            assertFalse(saveBtn.isDisable(), "Save button should be enabled after change");
        });
    }


    private void inject(String fieldName, Object value) throws Exception {
        Field field = ProfileController.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private Object getField(String fieldName) {
        try {
            Field field = ProfileController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runOnFxThread(Runnable task) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try { task.run(); } finally { latch.countDown(); }
            });
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
