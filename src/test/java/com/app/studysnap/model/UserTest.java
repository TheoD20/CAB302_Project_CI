package com.app.studysnap.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    // Constructors
    @Test
    void defaultConstructor_Exists() throws Exception {
        Class<?> clazz = User.class;
        assertNotNull(clazz.getDeclaredConstructor());
    }

    @Test
    void fullConstructor_Exists() throws Exception {
        Class<?> clazz = User.class;
        assertNotNull(clazz.getDeclaredConstructor(int.class, String.class, String.class, String.class, String.class, String.class));
    }

    @Test
    void noIdConstructor_Exists() throws Exception {
        Class<?> clazz = User.class;
        assertNotNull(clazz.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class));
    }

    @Test
    void minimalConstructor_Exists() throws Exception {
        Class<?> clazz = User.class;
        assertNotNull(clazz.getDeclaredConstructor(String.class, String.class, String.class));
    }

    // Getters and setters
    @Test
    void gettersAndSetters_WorkCorrectly() {
        user.setUserId(10);
        user.setUsername("John");
        user.setEmail("john@example.com");
        user.setPassword("secret");
        user.setAuthProvider("LOCAL");
        user.setGoogleSub("sub123");

        assertEquals(10, user.getUserId());
        assertEquals("John", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
        assertEquals("LOCAL", user.getAuthProvider());
        assertEquals("sub123", user.getGoogleSub());
    }

    // Test full constructor sets all fields
    @Test
    void fullConstructor_SetsFields() {
        User u = new User(1, "Alice", "alice@example.com", "pw", "LOCAL", "subX");
        assertEquals(1, u.getUserId());
        assertEquals("Alice", u.getUsername());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("pw", u.getPassword());
        assertEquals("LOCAL", u.getAuthProvider());
        assertEquals("subX", u.getGoogleSub());
    }

    // Test noId constructor
    @Test
    void noIdConstructor_SetsFields() {
        User u = new User("Bob", "bob@example.com", "pw", "LOCAL", "subY");
        assertEquals("Bob", u.getUsername());
        assertEquals("bob@example.com", u.getEmail());
        assertEquals("pw", u.getPassword());
        assertEquals("LOCAL", u.getAuthProvider());
        assertEquals("subY", u.getGoogleSub());
    }

    // Test minimal constructor
    @Test
    void minimalConstructor_SetsFields() {
        User u = new User("Charlie", "charlie@example.com", "pw");
        assertEquals("Charlie", u.getUsername());
        assertEquals("charlie@example.com", u.getEmail());
        assertEquals("pw", u.getPassword());
        assertNull(u.getAuthProvider());
        assertNull(u.getGoogleSub());
    }

    // Test toString does not include password
    @Test
    void toString_DoesNotContainPassword() {
        user.setPassword("mypw");
        String str = user.toString();
        assertFalse(str.contains("mypw"));
        assertTrue(str.contains("userName=") || str.contains("userId="));
    }
}
