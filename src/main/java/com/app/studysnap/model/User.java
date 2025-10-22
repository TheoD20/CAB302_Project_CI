package com.app.studysnap.model;

/**
 * An object class representing an application user.
 * <p>
 * A user may authenticate locally (username/email + password)
 * with {@code authProvider} being {@code "LOCAL"} or via Google OAuth,
 * in which case {@code authProvider} is {@code "GOOGLE"} and {@code googleSub} holds
 * the OpenID Connect subject identifier.
 * </p>
 */
public class User {
    private int userId;
    private String userName;
    private String email;
    private String password; // hashed password only
    private String authProvider; // "LOCAL" or "GOOGLE"
    private String googleSub;

    /**
     * Creates an empty {@code User}. Fields can be set via setters.
     */
    public User() {}

    /**
     * Creates a {@code User} with all fields populated.
     * @param userId database primary key
     * @param userName username
     * @param email user email
     * @param password hashed password (never raw)
     * @param authProvider authentication provider ("LOCAL" or "GOOGLE")
     * @param googleSub Google OpenID subject (if provider is GOOGLE), otherwise {@code null}
     */
    public User(int userId, String userName, String email, String password, String authProvider, String googleSub) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.authProvider = authProvider;
        this.googleSub = googleSub;
    }

    /**
     * Creates a {@code User} without an id (useful before persistence).
     * @param userName username
     * @param email user email
     * @param password hashed password (never raw)
     * @param authProvider authentication provider ("LOCAL" or "GOOGLE")
     * @param googleSub Google OpenID subject (if provider is GOOGLE), otherwise {@code null}
     */
    public User(String userName, String email, String password, String authProvider, String googleSub) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.authProvider = authProvider;
        this.googleSub = googleSub;
    }

    /**
     * Convenience constructor for a local-account user (no Google fields).
     * @param userName username
     * @param email user email
     * @param password hashed password (never raw)
     */
    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    /** @return the user id (database primary key) */
    public int getUserId() { return userId; }

    /** Sets the user id (database primary key). */
    public void setUserId(int userId) { this.userId = userId; }

    /** @return the username */
    public String getUsername() { return userName; }

    /** Sets the username. */
    public void setUsername(String userName) { this.userName = userName; }

    /** @return the email address */
    public String getEmail() { return email; }

    /** Sets the email address. */
    public void setEmail(String email) { this.email = email; }

    /** @return the hashed password (never raw) */
    public String getPassword() { return password; }

    /** Sets the hashed password (never store raw). */
    public void setPassword(String password) { this.password = password; }

    /** @return the authentication provider ("LOCAL" or "GOOGLE") */
    public String getAuthProvider() { return authProvider; }

    /** Sets the authentication provider ("LOCAL" or "GOOGLE"). */
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    /** @return the Google OpenID subject (if applicable), otherwise {@code null} */
    public String getGoogleSub() { return googleSub; }

    /** Sets the Google OpenID subject (if applicable). */
    public void setGoogleSub(String googleSub) { this.googleSub = googleSub; }

    /**
     * String representation excluding sensitive password content.
     * @return a concise description of the user
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", provider='" + authProvider + '\'' +
                '}';
    }
}