package com.app.studysnap.services;

import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import org.junit.jupiter.api.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AsyncTest {

    @BeforeAll
    static void initToolkit() throws InterruptedException {
        // Initialize JavaFX toolkit
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        latch.await();
    }

    @Test
    void run_TaskCompletes_Success() throws InterruptedException {
        ProgressIndicator pi = new ProgressIndicator();
        TabPane tabPane = new TabPane();

        CountDownLatch latch = new CountDownLatch(1);

        Callable<String> work = () -> "Hello";
        Async.run(work, result -> {
            assertEquals("Hello", result);
            latch.countDown();
        }, pi, tabPane);

        // Wait for async task to finish
        boolean finished = latch.await(2, TimeUnit.SECONDS);
        assertTrue(finished);

        // Verify UI components state
        Platform.runLater(() -> {
            assertFalse(pi.isVisible());
            assertFalse(tabPane.isDisabled());
        });
    }

    @Test
    void run_TaskThrows_ErrorHandled() throws InterruptedException {
        ProgressIndicator pi = new ProgressIndicator();
        TabPane tabPane = new TabPane();

        CountDownLatch latch = new CountDownLatch(1);

        Callable<String> work = () -> { throw new RuntimeException("fail"); };
        Async.run(work, result -> fail("Should not succeed"), pi, tabPane);

        // Wait briefly to let error handling run
        Thread.sleep(500);

        Platform.runLater(() -> {
            assertFalse(pi.isVisible());
            assertFalse(tabPane.isDisabled());
        });
    }
}
