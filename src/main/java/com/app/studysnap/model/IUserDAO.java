package com.app.studysnap.model;

import java.util.List;

/**
 Interface for the User Data Access Object that handles
 the CRUD operations for the User class with the database.
 */
public interface IUserDAO {
    int addUser(User user);
    void updateUser(User user);
    void deleteUser(int userId);
    List<User> getAllUsers();
    User getUserById(int userId);
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    boolean emailExists(String email);
    boolean usernameExists(String username);
    User getUserByGoogleSub(String googleSub);
    int addGoogleUser(String username, String email, String googleSub);
}

