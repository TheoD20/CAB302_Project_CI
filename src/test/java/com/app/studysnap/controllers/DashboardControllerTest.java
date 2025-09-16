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
    void fxmlFields_Exist() {
        Field[] fields = DashboardController.class.getDeclaredFields();
        boolean hasContentArea = false;
        boolean hasRoot = false;

        for (Field f : fields) {
            if (f.getName().equals("contentArea")) hasContentArea = true;
            if (f.getName().equals("root")) hasRoot = true;
        }

        assertTrue(hasContentArea, "contentArea field should exist");
        assertTrue(hasRoot, "root field should exist");
    }

    @Test
    void privateMethods_Exist() throws NoSuchMethodException {
        assertNotNull(DashboardController.class.getDeclaredMethod("initialize"));
        assertNotNull(DashboardController.class.getDeclaredMethod("handleNav", ActionEvent.class));
        assertNotNull(DashboardController.class.getDeclaredMethod("handleLogout", ActionEvent.class));
        assertNotNull(DashboardController.class.getDeclaredMethod("safeLoadCenter", String.class));
    }

    @Test
    void initialize_DoesNotThrow() throws Exception {
        Method initializeMethod = DashboardController.class.getDeclaredMethod("initialize");
        initializeMethod.setAccessible(true);
        runOnFxThread(() -> assertDoesNotThrow(() -> initializeMethod.invoke(controller)));
    }

    @Test
    void safeLoadCenter_DoesNotThrow() throws Exception {
        Method method = DashboardController.class.getDeclaredMethod("safeLoadCenter", String.class);
        method.setAccessible(true);
        runOnFxThread(() -> assertDoesNotThrow(() -> method.invoke(controller, "home.fxml")));
    }

    @Test
    void handleNav_NullEvent_DoesNotThrow() throws Exception {
        Method handleNavMethod = DashboardController.class.getDeclaredMethod("handleNav", ActionEvent.class);
        handleNavMethod.setAccessible(true);
        runOnFxThread(() -> assertDoesNotThrow(() -> handleNavMethod.invoke(controller, new ActionEvent())));
    }

    @Test
    void handleNav_ButtonWithUserData_DoesNotThrow() throws Exception {
        Method handleNavMethod = DashboardController.class.getDeclaredMethod("handleNav", ActionEvent.class);
        handleNavMethod.setAccessible(true);

        Button btn = new Button();
        btn.setUserData("home.fxml");
        ActionEvent event = new ActionEvent(btn, null);

        runOnFxThread(() -> assertDoesNotThrow(() -> handleNavMethod.invoke(controller, event)));
    }

    @Test
    void handleLogout_DoesNotThrow() throws Exception {
        Method handleLogoutMethod = DashboardController.class.getDeclaredMethod("handleLogout", ActionEvent.class);
        handleLogoutMethod.setAccessible(true);

        Button btn = new Button();
        ActionEvent event = new ActionEvent(btn, null);

        runOnFxThread(() -> assertDoesNotThrow(() -> handleLogoutMethod.invoke(controller, event)));
    }

    // Helper method to run code on JavaFX thread and wait
    private void runOnFxThread(Runnable runnable) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            runnable.run();
            latch.countDown();
        });
        latch.await();
    }
}
