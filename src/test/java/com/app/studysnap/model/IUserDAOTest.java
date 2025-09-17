package com.app.studysnap.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class IUserDAOTest {
    IUserDAO iUserDAO;

    @BeforeEach
    void setUp() {
        try {
            iUserDAO = null;
        } catch (Throwable t) {
            iUserDAO = null;
        }
    }

    // Test if method to add a user exists
    @Test
    void addUser_MethodExists() throws Exception {
        Class<?> MyClass = IUserDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("addUser", User.class));
    }

    // Test if method to get all existing users exists
    @Test
    void getAllUsers_MethodExists() throws Exception {
        Class<?> MyClass = IUserDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getAllUsers"));
    }

    // Test if method to get a user by username exists
    @Test
    void getUserByUsername_MethodExists() throws Exception {
        Class<?> MyClass = IUserDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getUserByUsername", String.class));
    }

    // Test if method to get a user by email exists
    @Test
    void getUserByEmail_MethodExists() throws Exception {
        Class<?> MyClass = IUserDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getUserByEmail", String.class));
    }

    // Test if method to test for existing email exists
    @Test
    void emailExists_MethodExists() throws Exception {
        Class<?> MyClass = IUserDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("emailExists", String.class));
    }

    // Test if method to test for existing username exists
    @Test
    void usernameExists_MethodExists() throws Exception {
        Class<?> MyClass = IUserDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("usernameExists", String.class));
    }

    // Test if method to get users by Google ID exists
    @Test
    void getUserByGoogleSub_MethodExists() throws Exception {
        Class<?> MyClass = IUserDAO.class;
        assertNotNull(MyClass.getDeclaredMethod("getUserByGoogleSub", String.class));
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(IUserDAO.class);
    }
}