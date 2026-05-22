package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserDAO {
    private final DBConnection db;

    public UserDAO() {
        this.db = DBConnection.getInstance();
    }

    /**
     * Finds a user by their username
     */
    public User getUserByUsername(String username) {
        // Fetching all columns from 'users' table
        //            db.ensureConnection();
        Map <String, Object> result = db.fetchRow("users", "username", username);
        if (result != null) {
            return new User(result);
        } else {
            System.out.println("No user found with username: " + username);
            return null;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        // Uses your generic selectAll helper!
        List<Map<String, Object>> data = DBConnection.getInstance().selectAll("users");

        for (Map<String, Object> row : data) {
            users.add(new User(row)); // Use the Map-constructor we built
        }
        return users;
    }

}
