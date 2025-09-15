package com.app.studysnap.controllers;

import com.app.studysnap.auth.AuthService;
import com.app.studysnap.auth.Session;
import com.app.studysnap.model.SqliteUserDAO;
import com.app.studysnap.model.User;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SignupControllerTest {

    private SignupController controller;
    private TextField nameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;

    @BeforeEach
    void setup() throws Exception {
        controller = new SignupController();

        nameField = (TextField) getPrivateField(controller, "nameField");
        emailField = (TextField) getPrivateField(controller, "emailField");
        passwordField = (PasswordField) getPrivateField(controller, "passwordField");
        confirmPasswordField = (PasswordField) getPrivateField(controller, "confirmPasswordField");

        // Clear UI
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");

        // Reset session
        Session.setCurrentUser(null);
    }

    private Object getPrivateField(Object obj, String name) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }

    private void callHandleSignup() throws Exception {
        Method method = controller.getClass().getDeclaredMethod("handleSignup");
        method.setAccessible(true);
        method.invoke(controller);
    }

    @Test
    void testPasswordMismatch() throws Exception {
        nameField.setText("Alice");
        emailField.setText("alice@example.com");
        passwordField.setText("pass1");
        confirmPasswordField.setText("pass2");

        callHandleSignup();

        // Password mismatch should prevent signup
        assertNull(Session.getCurrentUser());
    }

    @Test
    void testSuccessfulSignup() throws Exception {
        nameField.setText("Bob");
        emailField.setText("bob@example.com");
        passwordField.setText("mypassword");
        confirmPasswordField.setText("mypassword");

        callHandleSignup();

        User user = Session.getCurrentUser();
        assertNotNull(user);
        assertEquals("Bob", user.getUsername());
        assertEquals("bob@example.com", user.getEmail());
    }

    @Test
    void testDuplicateEmailSignup() throws Exception {
        // Register first user
        AuthService auth = new AuthService(new SqliteUserDAO());
        auth.register("Charlie", "charlie@example.com", "pass");

        // Try registering again with same email
        nameField.setText("Charlie2");
        emailField.setText("charlie@example.com");
        passwordField.setText("pass");
        confirmPasswordField.setText("pass");

        callHandleSignup();

        // Should not overwrite existing user
        User user = Session.getCurrentUser();
        assertNull(user); // Signup failed
    }
}
