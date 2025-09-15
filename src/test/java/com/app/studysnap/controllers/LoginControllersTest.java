package com.app.studysnap.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class LoginControllerTest {

    LoginController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LoginController();

        // Inject dummy FXML fields
        Field emailField = LoginController.class.getDeclaredField("emailField");
        emailField.setAccessible(true);
        emailField.set(controller, new TextField());

        Field passwordField = LoginController.class.getDeclaredField("passwordField");
        passwordField.setAccessible(true);
        passwordField.set(controller, new PasswordField());

        Field loginButton = LoginController.class.getDeclaredField("loginButton");
        loginButton.setAccessible(true);
        loginButton.set(controller, new Button());

        Field googleButton = LoginController.class.getDeclaredField("googleLoginButton");
        googleButton.setAccessible(true);
        googleButton.set(controller, new Button());

        Field toSignup = LoginController.class.getDeclaredField("toSignupLink");
        toSignup.setAccessible(true);
        toSignup.set(controller, new Hyperlink());

        Field forgotPass = LoginController.class.getDeclaredField("forgotPassLink");
        forgotPass.setAccessible(true);
        forgotPass.set(controller, new Hyperlink());
    }

    @Test
    void classLoads() {
        assertNotNull(LoginController.class);
    }

    @Test
    void fxmlFieldsExist() throws NoSuchFieldException {
        assertNotNull(LoginController.class.getDeclaredField("emailField"));
        assertNotNull(LoginController.class.getDeclaredField("passwordField"));
        assertNotNull(LoginController.class.getDeclaredField("loginButton"));
        assertNotNull(LoginController.class.getDeclaredField("googleLoginButton"));
        assertNotNull(LoginController.class.getDeclaredField("toSignupLink"));
        assertNotNull(LoginController.class.getDeclaredField("forgotPassLink"));
    }

    @Test
    void privateMethodsExist() throws NoSuchMethodException {
        assertNotNull(LoginController.class.getDeclaredMethod("handleLogin"));
        assertNotNull(LoginController.class.getDeclaredMethod("handleGoogleLogin"));
        assertNotNull(LoginController.class.getDeclaredMethod("goToSignup"));
        assertNotNull(LoginController.class.getDeclaredMethod("goToPassReset"));
    }

    @Test
    void handleLogin_DoesNotThrow() throws Exception {
        Method method = LoginController.class.getDeclaredMethod("handleLogin");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller));
    }

    @Test
    void handleGoogleLogin_DoesNotThrow() throws Exception {
        Method method = LoginController.class.getDeclaredMethod("handleGoogleLogin");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller));
    }

    @Test
    void goToSignup_DoesNotThrow() throws Exception {
        Method method = LoginController.class.getDeclaredMethod("goToSignup");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller));
    }

    @Test
    void goToPassReset_DoesNotThrow() throws Exception {
        Method method = LoginController.class.getDeclaredMethod("goToPassReset");
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(controller));
    }
}

