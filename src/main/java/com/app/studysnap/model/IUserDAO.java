package com.app.studysnap.model;

import com.app.studysnap.exceptions.DataAccessException;

import java.util.List;

/**
 * Data access for users and their metadata.
 * <p>
 * Persist new users, query for existing using different filters.
 * Provide helpers for updating and deleting.
 * </p>
 */
public interface IUserDAO {

    /**
     * Inserts a new user and returns the generated id.
     * @param user user to insert
     * @return new user id
     * @throws DataAccessException on persistence failure
     */
    int addUser(User user);

    /**
     * Updates an existing user (matched by {@code user_id}).
     * @param user updated user model
     * @throws DataAccessException on update failure
     */
    void updateUser(User user);

    /**
     * Deletes a user by id.
     * @param userId user id
     * @throws DataAccessException on delete failure
     */
    void deleteUser(int userId);

    /**
     * Returns all users.
     * @return non-null list (possibly empty)
     * @throws DataAccessException on query failure
     */
    List<User> getAllUsers();

    /**
     * Looks up a user by id.
     * @param userId user id
     * @return user or {@code null} if not found
     * @throws DataAccessException on query failure
     */
    User getUserById(int userId);

    /**
     * Looks up a user by username.
     * @param username username
     * @return user or {@code null} if not found
     * @throws DataAccessException on query failure
     */
    User getUserByUsername(String username);

    /**
     * Looks up a user by email (normalized to lower-case).
     * @param email email
     * @return user or {@code null} if not found
     * @throws DataAccessException on query failure
     */
    User getUserByEmail(String email);

    /**
     * Returns whether a user exists with the given email.
     * @param email email
     * @return true if exists, false otherwise
     * @throws DataAccessException on query failure
     */
    boolean emailExists(String email);

    /**
     * Returns whether a user exists with the given username.
     * @param username username
     * @return true if exists, false otherwise
     * @throws DataAccessException on query failure
     */
    boolean usernameExists(String username);

    /**
     * Looks up a user by Google ID (sub).
     * @param googleSub Google sub identifier
     * @return user or {@code null} if not found
     * @throws DataAccessException on query failure
     */
    User getUserByGoogleSub(String googleSub);

    /**
     * Inserts a new user for a Google OAuth sign-up and returns the generated id.
     * @param username display name
     * @param email email (verified by Google)
     * @param googleSub Google sub id
     * @return new user id
     * @throws DataAccessException on persistence failure
     */
    int addGoogleUser(String username, String email, String googleSub);
}

