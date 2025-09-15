package com.app.studysnap.controllers;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordControllerTest {

    private ResetPasswordController controller;

    private TextField emailField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Label messageLabel;

    @BeforeEach
    void setup() throws Exception {
        controller = new ResetPasswordController();

        emailField = (TextField) getPrivateField(controller, "emailField");
        newPasswordField = (PasswordField) getPrivateField(controller, "newPasswordField");
        confirmPasswordField = (PasswordField) getPrivateField(controller, "confirmPasswordField");
        messageLabel = (Label) getPrivateField(controller, "messageLabel");

        emailField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        messageLabel.setText("");
    }

    private Object getPrivateField(Object obj, String name) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(obj);
    }

    private void callHandleResetPassword() throws Exception {
        Method method = controller.getClass().getDeclaredMethod("handleResetPassword");
        method.setAccessible(true);
        method.invoke(controller);
    }

    @Test
    void testPasswordMismatch() throws Exception {
        emailField.setText("user@example.com");
        newPasswordField.setText("pass1");
        confirmPasswordField.setText("pass2");

        callHandleResetPassword();

        assertEquals("Passwords do not match.", messageLabel.getText());
    }

    @Test
    void testResetFailsForUnknownEmail() throws Exception {
        emailField.setText("nonexistent@example.com");
        newPasswordField.setText("password123");
        confirmPasswordField.setText("password123");

        callHandleResetPassword();

        // Your AuthService throws IllegalArgumentException with this message
        assertEquals("User with this email does not exist.", messageLabel.getText());
    }
}
