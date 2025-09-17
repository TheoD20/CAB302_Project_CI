package com.app.studysnap.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class DashboardControllerTest {

    private DashboardController controller;

    // Initialize JavaFX toolkit before all tests
    @BeforeAll
    static void initToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown); // Initialize JavaFX toolkit
        latch.await();
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new DashboardController();

        // Inject a dummy StackPane into contentArea (simulating FXML injection)
        Field contentField = DashboardController.class.getDeclaredField("contentArea");
        contentField.setAccessible(true);
        contentField.set(controller, new StackPane());
    }

    @Test
    void classLoads() {
        assertNotNull(DashboardController.class);
    }

    @Test
    void privateMethods_Exist() throws NoSuchMethodException {
        assertNotNull(DashboardController.class.getDeclaredMethod("initialize"));
        assertNotNull(DashboardController.class.getDeclaredMethod("handleNav", ActionEvent.class));
        assertNotNull(DashboardController.class.getDeclaredMethod("handleLogout", ActionEvent.class));
        assertNotNull(DashboardController.class.getDeclaredMethod("safeLoadCenter", String.class));
    }
}
