package com.app.studysnap.model;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqliteUserDAOTest {

    SqliteUserDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        // In-memory SQLite DB
        Connection conn = DriverManager.getConnection("jdbc:sqlite::memory:");

        // Override private connection field
        dao = new SqliteUserDAO();
        Field connField = SqliteUserDAO.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(dao, conn);

        // Create Users table
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");

            st.execute("""
                CREATE TABLE Users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT,
                    auth_provider TEXT NOT NULL DEFAULT 'LOCAL',
                    google_sub TEXT UNIQUE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Badges (
                    badge_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    icon_path TEXT,
                    type TEXT,
                    goal INTEGER
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS BadgeProgress (
                    user_id INTEGER NOT NULL,
                    badge_id INTEGER NOT NULL,
                    is_earned INTEGER NOT NULL DEFAULT 0,
                    progress INTEGER NOT NULL DEFAULT 0,
                    progress_goal INTEGER,
                    PRIMARY KEY (user_id, badge_id),
                    FOREIGN KEY (user_id)  REFERENCES Users(user_id)  ON DELETE CASCADE,
                    FOREIGN KEY (badge_id) REFERENCES Badges(badge_id) ON DELETE CASCADE
                )
            """);
        }
    }

    @Test
    void classLoads() {
        assertNotNull(SqliteUserDAO.class);
    }

    @Test
    void constructor_Exists() throws Exception {
        assertNotNull(SqliteUserDAO.class.getDeclaredConstructor());
    }

    @Test
    void methodsExist() throws Exception {
        Class<?> clazz = SqliteUserDAO.class;
        assertNotNull(clazz.getDeclaredMethod("addUser", User.class));
        assertNotNull(clazz.getDeclaredMethod("addGoogleUser", String.class, String.class, String.class));
        assertNotNull(clazz.getDeclaredMethod("updateUser", User.class));
        assertNotNull(clazz.getDeclaredMethod("deleteUser", int.class));
        assertNotNull(clazz.getDeclaredMethod("getAllUsers"));
        assertNotNull(clazz.getDeclaredMethod("getUserById", int.class));
        assertNotNull(clazz.getDeclaredMethod("getUserByUsername", String.class));
        assertNotNull(clazz.getDeclaredMethod("getUserByEmail", String.class));
        assertNotNull(clazz.getDeclaredMethod("emailExists", String.class));
        assertNotNull(clazz.getDeclaredMethod("usernameExists", String.class));
        assertNotNull(clazz.getDeclaredMethod("getUserByGoogleSub", String.class));
    }

    @Test
    void addUser_DoesNotThrowAndReturnsId() {
        User u = new User("testuser", "email@gmail.com", "pass", "LOCAL", null);
        int id = dao.addUser(u);
        assertTrue(id > 0);
    }

    @Test
    void addGoogleUser_DoesNotThrowAndReturnsId() {
        int id = dao.addGoogleUser("googleuser", "google@gmail.com", "sub123");
        assertTrue(id > 0);
    }

    @Test
    void getUserById_ReturnsUser() {
        User u = new User("user1", "u1@gmail.com", "p", "LOCAL", null);
        int id = dao.addUser(u);

        User fetched = dao.getUserById(id);
        assertNotNull(fetched);
        assertEquals("user1", fetched.getUsername());
    }

    @Test
    void updateUser_ChangesData() {
        User u = new User("user2", "u2@gmail.com", "p", "LOCAL", null);
        int id = dao.addUser(u);
        u.setUserId(id);
        u.setUsername("updated");
        dao.updateUser(u);

        User fetched = dao.getUserById(id);
        assertEquals("updated", fetched.getUsername());
    }

    @Test
    void deleteUser_RemovesUser() {
        User u = new User("user3", "u3@gmail.com", "p", "LOCAL", null);
        int id = dao.addUser(u);

        dao.deleteUser(id);
        assertNull(dao.getUserById(id));
    }

    @Test
    void getAllUsers_ReturnsList() {
        dao.addUser(new User("a", "a@gmail.com", "p", "LOCAL", null));
        dao.addUser(new User("b", "b@gmail.com", "p", "LOCAL", null));

        List<User> list = dao.getAllUsers();
        assertEquals(2, list.size());
    }

    @Test
    void emailExists_ReturnsTrueIfExists() {
        dao.addUser(new User("x", "x@gmail.com", "p", "LOCAL", null));
        assertTrue(dao.emailExists("x@gmail.com"));
        assertFalse(dao.emailExists("nonexistent@gmail.com"));
    }

    @Test
    void usernameExists_ReturnsTrueIfExists() {
        dao.addUser(new User("y", "y@gmail.com", "p", "LOCAL", null));
        assertTrue(dao.usernameExists("y"));
        assertFalse(dao.usernameExists("nope"));
    }

    @Test
    void getUserByGoogleSub_ReturnsUser() {
        dao.addGoogleUser("guser", "g@gmail.com", "sub123");
        User fetched = dao.getUserByGoogleSub("sub123");
        assertNotNull(fetched);
        assertEquals("guser", fetched.getUsername());
        assertNull(dao.getUserByGoogleSub("fakeSub"));
    }
}

