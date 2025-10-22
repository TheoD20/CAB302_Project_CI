package com.app.studysnap.auth;

import com.app.studysnap.exceptions.AlreadyExistsException;
import com.app.studysnap.exceptions.AuthenticationException;
import com.app.studysnap.exceptions.ResourceNotFoundException;
import com.app.studysnap.exceptions.ValidationException;
import com.app.studysnap.model.IUserDAO;
import com.app.studysnap.model.User;
import org.mindrot.jbcrypt.BCrypt;

import static com.app.studysnap.services.TextParser.isBlank;

/**
 * AuthService Class handles user authentication such as register, login and password resets.
 */
public class AuthService {
    private final IUserDAO users;

    /**
     * Constructs a new AuthService with specified UserDAO object.
     *
     * @param users The user DAO linked to the user's database.
     * @throws ValidationException if {@code users} is {@code null}.
     */
    public AuthService(IUserDAO users) {
        if (users == null) throw new ValidationException("UserDAO must not be null.");
        this.users = users;
    }

    /**
     * Registers a new LOCAL user with the specified username, email, and password.
     * Creates the user, hashes the password with BCrypt, persists via DAO, and sets the session.
     *
     * @param username The user's display/handle name.
     * @param email The user's email (login identifier).
     * @param rawPassword The user's plaintext password to be hashed.
     * @return The newly created {@link User} with ID set and active session.
     * @throws ValidationException if any parameter is blank.
     * @throws AlreadyExistsException if the email is already registered.
     */
    public User register(String username, String email, String rawPassword) {
        if (isBlank(username) || isBlank(email) || isBlank(rawPassword))
            throw new ValidationException("Username, email and password are required.");

        if (users.emailExists(email))
            throw new AlreadyExistsException("Email already in use.");

        User u = new User();
        u.setUsername(username.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        u.setAuthProvider("LOCAL");

        int id = users.addUser(u);
        u.setUserId(id);

        Session.setCurrentUser(u);
        return u;
    }

    /**
     * Registers a new Google user, or links/returns an existing Google-linked account.
     * @param name Display name from Google (can be blank).
     * @param email Google account email (must not be blank).
     * @param googleSub Google subject (unique Google user ID), may be {@code null}.
     * @return The matched or newly provisioned {@link User} with current session set.
     * @throws ValidationException if {@code email} is blank.
     * @throws AuthenticationException if a conflicting LOCAL account already uses the email.
     */
    public User registerGoogleUser(String name, String email, String googleSub) {
        if (isBlank(email)) throw new ValidationException("Email is required for Google signup.");

        // If name is empty, get first part of email
        if (isBlank(name))  name = email.split("@")[0];

        // If email exists as LOCAL → block to avoid password breach
        User existing = users.getUserByEmail(email);
        if (existing != null && "LOCAL".equals(existing.getAuthProvider())) {
            throw new AuthenticationException("This email is already registered with a password. Use email login.");
        }

        // If already a GOOGLE user, ensure sub is set; otherwise return existing
        User bySub = users.getUserByGoogleSub(googleSub);
        if (bySub != null) {
            Session.setCurrentUser(bySub);
            return bySub;
        }

        if (existing != null && "GOOGLE".equals(existing.getAuthProvider())) {
            if (existing.getGoogleSub() == null && googleSub != null) {
                existing.setGoogleSub(googleSub);
                users.updateUser(existing);
            }
            Session.setCurrentUser(existing);
            return existing;
        }

        int id = users.addGoogleUser(name.trim(), email.trim().toLowerCase(), googleSub);
        User u = users.getUserById(id);
        if (u == null) u = users.getUserByEmail(email);
        if (u != null) {
            u.setAuthProvider("GOOGLE");
            u.setGoogleSub(googleSub);
            Session.setCurrentUser(u);
        }
        return u;
    }

    /**
     * Logs in an existing LOCAL (email/password) account.
     * Validates credentials using BCrypt and sets the session on success.
     * @param email The user's email.
     * @param rawPassword The user's plaintext password.
     * @return The authenticated {@link User} with current session set.
     * @throws ValidationException if any parameter is blank.
     * @throws AuthenticationException if the account is not LOCAL or credentials are invalid.
     */
    public User loginWithEmail(String email, String rawPassword) {
        if (isBlank(email) || isBlank(rawPassword))
            throw new ValidationException("Email and password are required.");
        User u = users.getUserByEmail(email.trim().toLowerCase());
        if (u == null) throw new AuthenticationException("Invalid email or password.");
        if (!"LOCAL".equals(u.getAuthProvider()))
            throw new AuthenticationException("This account uses Google Sign-In. Use 'Sign in with Google'.");

        if (!BCrypt.checkpw(rawPassword, u.getPassword()))
            throw new AuthenticationException("Invalid email or password.");

        Session.setCurrentUser(u);
        return u;
    }

    /**
     * Logs in a user using Google Sign-In details.
     * If {@code googleSub} matches, logs in directly; otherwise attempts by email.
     * If no account exists, auto-provisions a new Google user.
     * @param googleSub unique Google user ID, may be blank.
     * @param email Email from Google to match or provision.
     * @param nameFallback Fallback display name if Google name is not present.
     * @return The authenticated or newly provisioned {@link User} with current session set.
     * @throws AuthenticationException if the email belongs to a LOCAL account.
     */
    public User loginWithGoogle(String googleSub, String email, String nameFallback) {
        if (!isBlank(googleSub)) {
            User bySub = users.getUserByGoogleSub(googleSub);
            if (bySub != null) {
                Session.setCurrentUser(bySub);
                return bySub;
            }
        }
        User byEmail = users.getUserByEmail(email);
        if (byEmail != null) {
            if ("GOOGLE".equals(byEmail.getAuthProvider())) {
                if (byEmail.getGoogleSub() == null && !isBlank(googleSub)) {
                    byEmail.setGoogleSub(googleSub);
                    users.updateUser(byEmail);
                }
                Session.setCurrentUser(byEmail);
                return byEmail;
            }
            // Email is a LOCAL account: do NOT auto-link
            throw new AuthenticationException("An account with this email uses a password. Use email login.");
        }

        // No account exists → auto-provision Google user
        String name = isBlank(nameFallback) ? email.split("@")[0] : nameFallback;
        User u = registerGoogleUser(name, email, googleSub);
        Session.setCurrentUser(u);
        return u;
    }

    /**
     * Resets the password of a LOCAL account identified by email.
     * Google-linked accounts cannot reset passwords via this method.
     *
     * @param email The account email to reset.
     * @param newPassword The new plaintext password to hash and store.
     * @throws ResourceNotFoundException if no account exists for the email.
     * @throws AuthenticationException if the account is Google-linked (no password reset).
     */
    public void resetPassword(String email, String newPassword) {
        var user = users.getUserByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("No account found with this email.");
        }
        if (user.getGoogleSub() != null) {
            throw new AuthenticationException("This account uses Google Sign-In. Password reset not available.");
        }
        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        users.updateUser(user);
    }
}
