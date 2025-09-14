package com.app.studysnap.auth;

import com.app.studysnap.model.User;

public final class Session {
    private static User currentUser;

    Session() {}

    public static void setCurrentUser(User u) { currentUser = u; }
    public static User getCurrentUser() { return currentUser; }
    public static void clear() { currentUser = null; }
}
