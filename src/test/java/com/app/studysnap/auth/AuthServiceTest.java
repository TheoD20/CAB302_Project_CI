package com.app.studysnap.auth;

import com.app.studysnap.exceptions.AlreadyExistsException;
import com.app.studysnap.exceptions.AuthenticationException;
import com.app.studysnap.exceptions.ValidationException;
import com.app.studysnap.model.IUserDAO;
import com.app.studysnap.model.SqliteUserDAO;
import com.app.studysnap.model.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    IUserDAO users;
    AuthService authService;

    @BeforeEach
    void setUp() {
        users = new SqliteUserDAO();
        authService = new AuthService(users);
    }

    @AfterEach
    void clearDB() {
        if (users == null) return;
        for (User u : users.getAllUsers()) {
            users.deleteUser(u.getUserId());
        }
    }

    // Constructor: signature exists
    @Test
    void authService_Constructor_MethodExists() throws Exception {
        assertNotNull(AuthService.class.getDeclaredConstructor(IUserDAO.class));
    }

    // Constructor: null DAO -> ValidationException
    @Test
    void constructor_NullArgs_Throws() {
        assertThrows(ValidationException.class, () -> new AuthService(null));
    }

    // register method exists
    @Test
    void register_MethodExists() throws Exception {
        assertNotNull(AuthService.class.getDeclaredMethod("register", String.class, String.class, String.class));
    }

    // Registering a valid LOCAL user
    @Test
    void register_ValidUser_Succeeds() {
        User u = authService.register("alice", "alice@example.com", "secret123");
        assertNotNull(u);
        assertEquals("alice", u.getUsername());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("LOCAL", u.getAuthProvider());
        assertNotNull(users.getUserByEmail("alice@example.com"));
    }

    // Blank fields -> ValidationException (custom)
    @Test
    void register_BlankInputs_Throws() {
        assertThrows(ValidationException.class, () -> authService.register(" ", "x@y.com", "p"));
        assertThrows(ValidationException.class, () -> authService.register("bob", " ", "p"));
        assertThrows(ValidationException.class, () -> authService.register("bob", "b@y.com", " "));
        assertThrows(ValidationException.class, () -> authService.register(null, "b@y.com", "p"));
        assertThrows(ValidationException.class, () -> authService.register("bob", null, "p"));
        assertThrows(ValidationException.class, () -> authService.register("bob", "b@y.com", null));
    }

    // Duplicate email -> AlreadyExistsException (custom)
    @Test
    void register_DuplicateEmail_Throws() {
        authService.register("alice", "alice@example.com", "secret123");
        assertThrows(AlreadyExistsException.class, () -> authService.register("alice2", "alice@example.com", "pw"));
    }

    // google register method exists
    @Test
    void registerGoogleUser_MethodExists() throws Exception {
        assertNotNull(AuthService.class.getDeclaredMethod("registerGoogleUser", String.class, String.class, String.class));
    }

    @Test
    void registerGoogleUser_Valid_Succeeds() {
        User u = authService.registerGoogleUser("carol", "carol@example.com", "sub-123");
        assertNotNull(u);
        assertEquals("carol", u.getUsername());
        assertEquals("carol@example.com", u.getEmail());
        assertEquals("GOOGLE", u.getAuthProvider());
        assertEquals("sub-123", u.getGoogleSub());
        assertNotNull(users.getUserByGoogleSub("sub-123"));
    }

    // Google-register with email owned by LOCAL -> AuthenticationException (custom conflict)
    @Test
    void registerGoogleUser_DuplicateEmail_Throws() {
        authService.register("dave", "dave@example.com", "pw");
        assertThrows(AuthenticationException.class, () -> authService.registerGoogleUser("dave", "dave@example.com", "sub-x"));
    }

    // email login method exists
    @Test
    void loginWithEmail_MethodExists() throws Exception {
        assertNotNull(AuthService.class.getDeclaredMethod("loginWithEmail", String.class, String.class));
    }

    @Test
    void loginWithEmail_Valid_ReturnsUser() {
        User created = authService.register("ellen", "ellen@example.com", "pw");
        assertNotNull(created);
        User logged = authService.loginWithEmail("ellen@example.com", "pw");
        assertNotNull(logged);
        assertEquals("ellen@example.com", logged.getEmail());
    }

    @Test
    void loginWithEmail_Invalid_Throws() {
        // valid-looking email, absent user
        assertThrows(AuthenticationException.class, () -> authService.loginWithEmail("nope@example.com", "pw"));

        authService.register("fran", "fran@example.com", "pw1");
        // wrong password
        assertThrows(AuthenticationException.class, () -> authService.loginWithEmail("fran@example.com", "wrong"));
    }

    // google login method exists
    @Test
    void loginWithGoogle_MethodExists() throws Exception {
        assertNotNull(AuthService.class.getDeclaredMethod("loginWithGoogle", String.class, String.class, String.class));
    }

    @Test
    void loginWithGoogle_ExistingGoogleSub_ReturnsUser() {
        User g = authService.registerGoogleUser("gina", "gina@example.com", "sub-999");
        assertNotNull(g);
        User logged = authService.loginWithGoogle("sub-999", "gina@example.com", "gina");
        assertNotNull(logged);
        assertEquals("gina@example.com", logged.getEmail());
        assertEquals("sub-999", logged.getGoogleSub());
    }

    @Test
    void loginWithGoogle_NewUser_AutoSign_ReturnsUser() {
        User logged = authService.loginWithGoogle("sub-new", "newuser@example.com", "New User");
        assertNotNull(logged);
        assertEquals("newuser@example.com", logged.getEmail());
        assertEquals("sub-new", logged.getGoogleSub());
        assertEquals("GOOGLE", logged.getAuthProvider());
    }

    // Class loads
    @Test
    void classLoads() {
        assertNotNull(AuthService.class);
    }
}