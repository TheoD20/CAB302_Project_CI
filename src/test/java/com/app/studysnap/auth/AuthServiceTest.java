package com.app.studysnap.auth;

import com.app.studysnap.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    static class MockUserDAO implements IUserDAO {
        private int seq = 1;
        private final java.util.Map<Integer, User> byId = new java.util.HashMap<>();
        private User byEmail(String email) {
            return byId.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        }
        private User byUsername(String name) {
            return byId.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(name)).findFirst().orElse(null);
        }
        private User bySub(String sub) {
            return byId.values().stream().filter(u -> sub != null && sub.equals(u.getGoogleSub())).findFirst().orElse(null);
        }
        @Override public int addUser(User user) {
            user.setUserId(seq++);
            if (user.getAuthProvider() == null || user.getAuthProvider().isBlank()) user.setAuthProvider("LOCAL");
            byId.put(user.getUserId(), cloneUser(user));
            return user.getUserId();
        }
        @Override public void updateUser(User user) { byId.put(user.getUserId(), cloneUser(user)); }
        @Override public void deleteUser(int userId) { byId.remove(userId); }
        @Override public java.util.List<User> getAllUsers() { return new java.util.ArrayList<>(byId.values()); }
        @Override public User getUserById(int userId) { return cloneUser(byId.get(userId)); }
        @Override public User getUserByUsername(String username) { return cloneUser(byUsername(username)); }
        @Override public User getUserByEmail(String email) { return cloneUser(byEmail(email)); }
        @Override public boolean emailExists(String email) { return byEmail(email) != null; }
        @Override public boolean usernameExists(String username) { return byUsername(username) != null; }
        @Override public User getUserByGoogleSub(String googleSub) { return cloneUser(bySub(googleSub)); }
        @Override public int addGoogleUser(String username, String email, String googleSub) {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email.toLowerCase());
            u.setPassword(null);
            u.setAuthProvider("GOOGLE");
            u.setGoogleSub(googleSub);
            return addUser(u);
        }
        private static User cloneUser(User in) {
            if (in == null) return null;
            User u = new User();
            u.setUserId(in.getUserId());
            u.setUsername(in.getUsername());
            u.setEmail(in.getEmail());
            u.setPassword(in.getPassword());
            u.setAuthProvider(in.getAuthProvider());
            u.setGoogleSub(in.getGoogleSub());
            return u;
        }
    }

    private AuthService auth;
    private MockUserDAO dao;

    @BeforeEach
    void setUp() {
        dao = new MockUserDAO();
        auth = new AuthService(dao);
    }

    @Test
    void register_success_createsLocalUser() {
        User u = auth.register("Theo", "theo@example.com", "secret");
        assertNotNull(u);
        assertEquals("Theo", u.getUsername());
        assertEquals("theo@example.com", u.getEmail());
        assertEquals("LOCAL", u.getAuthProvider());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        auth.register("A", "dup@example.com", "x");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> auth.register("B", "dup@example.com", "y"));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
    }

    @Test
    void loginWithEmail_success_whenLocalAndPasswordMatches() {
        auth.register("A", "a@example.com", "pw");
        User u = auth.loginWithEmail("a@example.com", "pw");
        assertEquals("A", u.getUsername());
    }

    @Test
    void loginWithEmail_fails_whenWrongPassword() {
        auth.register("A", "a2@example.com", "pw");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> auth.loginWithEmail("a2@example.com", "nope"));
        assertTrue(ex.getMessage().toLowerCase().contains("invalid"));
    }

    @Test
    void loginWithEmail_fails_whenAccountIsGoogle() {
        dao.addGoogleUser("G", "g@example.com", "sub-1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> auth.loginWithEmail("g@example.com", "anything"));
        assertTrue(ex.getMessage().toLowerCase().contains("google"));
    }

    @Test
    void registerGoogleUser_succeeds_andSetsProvider() {
        User u = auth.registerGoogleUser("G", "gg@example.com", "sub-2");
        assertNotNull(u);
        assertEquals("GOOGLE", u.getAuthProvider());
        assertEquals("sub-2", u.getGoogleSub());
    }

    @Test
    void loginWithGoogle_linksExistingGoogleBySub() {
        auth.registerGoogleUser("G", "x@example.com", "sub-3");
        User u = auth.loginWithGoogle("sub-3", "x@example.com", "G");
        assertEquals("x@example.com", u.getEmail());
    }

    @Test
    void loginWithGoogle_provisionsWhenNoAccountExists() {
        User u = auth.loginWithGoogle("sub-4", "y@example.com", "Y");
        assertEquals("GOOGLE", u.getAuthProvider());
        assertEquals("y@example.com", u.getEmail());
    }

    @Test
    void registerGoogleUser_fails_whenLocalEmailAlreadyExists() {
        auth.register("Local", "h@example.com", "p");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> auth.registerGoogleUser("Name", "h@example.com", "sub-5"));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
    }
}