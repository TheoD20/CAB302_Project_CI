package com.app.studysnap.auth;

import com.app.studysnap.model.*;

public class AuthService {
    private final IUserDAO users;
    public AuthService(IUserDAO users) { this.users = users; }

    // Register a new user given username, email and password
    public User register(String username, String email, String rawPassword) {
        if (isBlank(username) || isBlank(email) || isBlank(rawPassword))
            throw new IllegalArgumentException("Username, email and password are required.");

        if (users.emailExists(email))
            throw new IllegalArgumentException("Email already in use.");

        // For now: store as-is. Later: hash before saving.
        User u = new User();
        u.setUsername(username.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPassword(rawPassword);

        int id = users.addUser(u);
        u.setUserId(id);
        return u;
    }

    // Register a new user given name, email and Google sub (ID)
    public User registerGoogleUser(String name, String email, String googleSub) {
        if (isBlank(email)) throw new IllegalArgumentException("Email is required for Google signup.");

        // If name is empty, get first part of email
        if (isBlank(name))  name = email.split("@")[0];

        // If email exists as LOCAL → block to avoid password breach
        User existing = users.getUserByEmail(email);
        if (existing != null && "LOCAL".equals(existing.getAuthProvider())) {
            throw new IllegalArgumentException("This email is already registered with a password. Use email login.");
        }

        // If already a GOOGLE user, ensure sub is set; otherwise return existing
        User bySub = users.getUserByGoogleSub(googleSub);
        if (bySub != null) return bySub;

        if (existing != null && "GOOGLE".equals(existing.getAuthProvider())) {
            if (existing.getGoogleSub() == null && googleSub != null) {
                existing.setGoogleSub(googleSub);
                users.updateUser(existing);
            }
            return existing;
        }

        int id = users.addGoogleUser(name.trim(), email.trim().toLowerCase(), googleSub);
        User u = users.getUserById(id);
        return u != null ? u : users.getUserByEmail(email);
    }

    // Login existing user given email and password
    public User loginWithEmail(String email, String rawPassword) {
        if (isBlank(email) || isBlank(rawPassword))
            throw new IllegalArgumentException("Email and password are required.");
        User u = users.getUserByEmail(email.trim().toLowerCase());
        if (u == null) throw new IllegalArgumentException("Invalid email or password.");
        if (!"LOCAL".equals(u.getAuthProvider()))
            throw new IllegalArgumentException("This account uses Google Sign-In. Use 'Sign in with Google'.");
        if (!rawPassword.equals(u.getPassword()))
            throw new IllegalArgumentException("Invalid email or password.");
        return u;
    }

    // login existing user given google ID
    public User loginWithGoogle(String googleSub, String email, String nameFallback) {
        if (!isBlank(googleSub)) {
            User bySub = users.getUserByGoogleSub(googleSub);
            if (bySub != null) return bySub;
        }
        User byEmail = users.getUserByEmail(email);
        if (byEmail != null) {
            if ("GOOGLE".equals(byEmail.getAuthProvider())) {
                if (byEmail.getGoogleSub() == null && !isBlank(googleSub)) {
                    byEmail.setGoogleSub(googleSub);
                    users.updateUser(byEmail);
                }
                return byEmail;
            }
            // Email is a LOCAL account: do NOT auto-link
            throw new IllegalArgumentException("An account with this email uses a password. Use email login.");
        }

        // No account exists → auto-provision Google user (smooth UX)
        String name = isBlank(nameFallback) ? email.split("@")[0] : nameFallback;
        return registerGoogleUser(name, email, googleSub);
    }

    // Test if a string is null or empty
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}