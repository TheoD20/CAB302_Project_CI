package com.app.studysnap.auth;

import com.app.studysnap.model.User;

/**
 * Holds the currently authenticated {@link User} for the running application session.
 */
public final class Session {

    /**
     * The current authenticated user.
     */
    private static User currentUser;

    /**
     * Package-private constructor to prevent external instantiation.
     */
    Session() {}

    /**
     * Sets the current authenticated user.
     * @param u the authenticated {@link User}
     */
    public static void setCurrentUser(User u) { currentUser = u; }

    /**
     * Returns the current authenticated user.
     * @return the current {@link User}
     */
    public static User getCurrentUser() { return currentUser; }

    /**
     * Clears the current session by removing the authenticated user reference.
     */
    public static void clear() { currentUser = null; }
}