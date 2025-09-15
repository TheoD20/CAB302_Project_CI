package com.app.studysnap.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class DashboardControllerTest {

    DashboardController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new DashboardController();

        // Inject a dummy StackPane into the private contentArea field
        Field contentField = DashboardController.class.getDeclaredField("contentArea");
        contentField.setAccessible(true);  // allow access to private field
        contentField.set(controller, new StackPane());
    }

    // Test class loads
    @Test
    void classLoads() {
        assertNotNull(DashboardController.class);
    }

    // Test FXML fields exist
    @Test
    void fxmlFields_Exist() throws NoSuchFieldException {
        Field rootField = DashboardController.class.getDeclaredField("root");
        Field contentField = DashboardController.class.getDeclaredField("contentArea");
        assertNotNull(rootField);
        assertNotNull(contentField);
    }

    // Test private methods exist
    @Test
    void privateMethods_Exist() throws NoSuchMethodException {
        assertNotNull(DashboardController.class.getDeclaredMethod("initialize"));
        assertNotNull(DashboardController.class.getDeclaredMethod("handleNav", ActionEvent.class));
        assertNotNull(DashboardController.class.getDeclaredMethod("handleLogout", ActionEvent.class));
        assertNotNull(DashboardController.class.getDeclaredMethod("safeLoadCenter", String.class));
    }

    // Test initialize does not throw
    @Test
    void initialize_DoesNotThrow() throws Exception {
        Method initializeMethod = DashboardController.class.getDeclaredMethod("initialize");
        initializeMethod.setAccessible(true);
        assertDoesNotThrow(() -> initializeMethod.invoke(controller));
    }

    // Test safeLoadCenter does not throw when called with a string
    @Test
    void safeLoadCenter_DoesNotThrow() throws Exception {
        Method method = DashboardController.class.getDeclaredMethod("safeLoadCenter", String.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller, "home.fxml"));
    }

    // Test handleNav with null event does not throw
    @Test
    void handleNav_NullEvent_DoesNotThrow() throws Exception {
        Method handleNavMethod = DashboardController.class.getDeclaredMethod("handleNav", ActionEvent.class);
        handleNavMethod.setAccessible(true);
        ActionEvent event = new ActionEvent();
        assertDoesNotThrow(() -> handleNavMethod.invoke(controller, event));
    }

    // Test handleNav with button userData does not throw
    @Test
    void handleNav_ButtonWithUserData_DoesNotThrow() throws Exception {
        Method handleNavMethod = DashboardController.class.getDeclaredMethod("handleNav", ActionEvent.class);
        handleNavMethod.setAccessible(true);

        Button btn = new Button();
        btn.setUserData("home.fxml");
        ActionEvent event = new ActionEvent(btn, null);

        assertDoesNotThrow(() -> handleNavMethod.invoke(controller, event));
    }

    // Test handleLogout with dummy button does not throw
    @Test
    void handleLogout_DoesNotThrow() throws Exception {
        Method handleLogoutMethod = DashboardController.class.getDeclaredMethod("handleLogout", ActionEvent.class);
        handleLogoutMethod.setAccessible(true);

        Button btn = new Button();
        ActionEvent event = new ActionEvent(btn, null);

        assertDoesNotThrow(() -> handleLogoutMethod.invoke(controller, event));
    }
}
