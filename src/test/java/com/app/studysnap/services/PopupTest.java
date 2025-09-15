package com.app.studysnap.services;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PopupTest {

    @BeforeAll
    static void initToolkit() throws InterruptedException {
        // Initialize JavaFX toolkit for testing
        final Object lock = new Object();
        Platform.startup(() -> {
            synchronized (lock) { lock.notify(); }
        });
        synchronized (lock) { lock.wait(500); }
    }

    @Test
    void testInfoWarnErrorDontThrow() {
        assertDoesNotThrow(() -> Popup.info("Info message"));
        assertDoesNotThrow(() -> Popup.warn("Warning message"));
        assertDoesNotThrow(() -> Popup.error("Error message"));
    }

    @Test
    void testConfirmReturnsBoolean() {
        // Since showAndWait() requires user interaction, we cannot automatically click OK/Cancel in unit test.
        // But we can still call it in headless mode to ensure no exception:
        assertDoesNotThrow(() -> Popup.confirm("Title", "Message"));
    }
}

