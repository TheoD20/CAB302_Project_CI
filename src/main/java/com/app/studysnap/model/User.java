package com.app.studysnap.model;

public class User
{
    private int userId;
    private String userName;
    private String email;
    private String password; // make sure to store hashed passwords !!!
    private String authProvider; // "LOCAL" or "GOOGLE"
    private String googleSub;

    // Default constructor
    public User() {}

    // Constructor with all fields
    public User(int userId, String userName, String email, String password, String authProvider, String googleSub) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.authProvider = authProvider;
        this.googleSub = googleSub;
    }

    // Constructor without userId (use this for creating new users)
    public User(String userName, String email, String password, String authProvider, String googleSub) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.authProvider = authProvider;
        this.googleSub = googleSub;
    }

    // Constructor without Google OAuth fields
    public User(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return userName; }
    public void setUsername(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public String getGoogleSub() { return googleSub; }
    public void setGoogleSub(String googleSub) { this.googleSub = googleSub; }

    // To string (omit password for security!)
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

