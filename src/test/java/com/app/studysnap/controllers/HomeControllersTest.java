package com.app.studysnap.controllers;

import com.app.studysnap.model.Quiz;
import com.app.studysnap.model.User;
import com.app.studysnap.auth.Session;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    HomeController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new HomeController();

        // Inject dummy FXML nodes
        Field welcomeField = HomeController.class.getDeclaredField("welcomeLabel");
        welcomeField.setAccessible(true);
        welcomeField.set(controller, new Label());

        Field deckField = HomeController.class.getDeclaredField("deckContainer");
        deckField.setAccessible(true);
        deckField.set(controller, new FlowPane());

        Field emptyField = HomeController.class.getDeclaredField("emptyState");
        emptyField.setAccessible(true);
        emptyField.set(controller, new VBox());

        // Set a dummy current user
        Session.setCurrentUser(new User("testUser", "email@test.com", "pass"));
    }

    @AfterEach
    void tearDown() {
        Session.setCurrentUser(null);
    }

    // Test class loads
    @Test
    void classLoads() {
        assertNotNull(HomeController.class);
    }

    // Test FXML fields exist
    @Test
    void fxmlFields_Exist() throws NoSuchFieldException {
        Field welcomeField = HomeController.class.getDeclaredField("welcomeLabel");
        Field deckField = HomeController.class.getDeclaredField("deckContainer");
        Field emptyField = HomeController.class.getDeclaredField("emptyState");
        assertNotNull(welcomeField);
        assertNotNull(deckField);
        assertNotNull(emptyField);
    }

    // Test initialize does not throw
    @Test
    void initialize_DoesNotThrow() throws Exception {
        Method initMethod = HomeController.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        assertDoesNotThrow(() -> initMethod.invoke(controller));
    }

    // Test private nz method
    @Test
    void nz_ReturnsEmptyStringIfNull() throws Exception {
        Method nzMethod = HomeController.class.getDeclaredMethod("nz", String.class);
        nzMethod.setAccessible(true);
        String result = (String) nzMethod.invoke(controller, (Object) null);
        assertEquals("", result);
        assertEquals("test", nzMethod.invoke(controller, "test"));
    }

    // Test private safeFileName method
    @Test
    void safeFileName_ReplacesIllegalCharacters() throws Exception {
        Method method = HomeController.class.getDeclaredMethod("safeFileName", String.class);
        method.setAccessible(true);
        String name = (String) method.invoke(controller, "My:Quiz/Name?");
        assertEquals("My_Quiz_Name_", name);
    }

    // Test showEmpty toggles visibility
    @Test
    void showEmpty_TogglesVisibility() throws Exception {
        Method method = HomeController.class.getDeclaredMethod("showEmpty", boolean.class);
        method.setAccessible(true);
        VBox empty = (VBox) HomeController.class.getDeclaredField("emptyState").get(controller);

        method.invoke(controller, true);
        assertTrue(empty.isVisible());
        assertTrue(empty.isManaged());

        method.invoke(controller, false);
        assertFalse(empty.isVisible());
        assertFalse(empty.isManaged());
    }

    // Test buildCard returns non-null Node
    @Test
    void buildCard_ReturnsVBox() throws Exception {
        Method method = HomeController.class.getDeclaredMethod("buildCard", Quiz.class);
        method.setAccessible(true);
        Quiz q = new Quiz("Test Quiz", null, null, true, 1); // minimal constructor
        Object node = method.invoke(controller, q);
        assertNotNull(node);
    }

    // Test loadMyQuizzes does not throw
    @Test
    void loadMyQuizzes_DoesNotThrow() throws Exception {
        Method method = HomeController.class.getDeclaredMethod("loadMyQuizzes");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller));
    }
}
