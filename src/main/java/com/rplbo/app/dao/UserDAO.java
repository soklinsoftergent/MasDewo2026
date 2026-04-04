package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        try (ResultSet rs = db.fetchOneByKeyColumn("*", "users", "username", username)) {
            if (rs != null && rs.next()) {
                // IMPORTANT: In your SQL, role_id 1 is Admin, 2 is Staff
                boolean isAdmin = (rs.getInt("role_id") == 1);

                return new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("phonenumber"),
                        isAdmin,
                        rs.getInt("user_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Saves a new user to the database
     */
    public boolean saveUser(User user) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("username", user.getUsername());
        data.put("email", user.getUserEmail());
        data.put("password_hash", user.getUserPasswdHash());
        data.put("role_id", user.isAdmin() ? 1 : 2);

        return db.insertIntoTable("users", data);
    }
}