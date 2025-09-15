package com.app.studysnap.services;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NavigatorTest {

    Node dummyNode;

    @BeforeEach
    void setUp() {
        // Use a dummy StackPane to simulate a Node attached to a Scene
        dummyNode = new StackPane();
    }

    @Test
    void classLoads() {
        assertNotNull(Navigator.class);
    }

    @Test
    void goTo_MethodExists() throws NoSuchMethodException {
        assertNotNull(Navigator.class.getDeclaredMethod("goTo", Node.class, String.class));
    }

    @Test
    void showInDashboard_MethodExists() throws NoSuchMethodException {
        assertNotNull(Navigator.class.getDeclaredMethod("showInDashboard", Node.class, String.class));
    }

    @Test
    void loadView_PrivateMethodExists() throws NoSuchMethodException {
        assertNotNull(Navigator.class.getDeclaredMethod("loadView", String.class));
    }

    // You can also test that invalid input does not throw unhandled exceptions
    @Test
    void showInDashboard_InvalidNode_DoesNotThrow() {
        Node node = new StackPane(); // not attached to scene
        assertDoesNotThrow(() -> Navigator.showInDashboard(node, "nonexistent.fxml"));
    }

    @Test
    void goTo_InvalidFxml_DoesNotThrow() {
        assertDoesNotThrow(() -> Navigator.goTo(dummyNode, "nonexistent.fxml"));
    }
}

