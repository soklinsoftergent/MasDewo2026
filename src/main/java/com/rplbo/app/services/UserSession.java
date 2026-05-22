package com.rplbo.app.services;

import com.rplbo.app.models.User;

import java.security.AuthProvider;

public class UserSession {

    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public boolean isUserEmpty() {
        return this.currentUser == null;
    }

    public boolean isAdmin() {
        return !isUserEmpty() && getCurrentUser().isAdmin();
    }
}
