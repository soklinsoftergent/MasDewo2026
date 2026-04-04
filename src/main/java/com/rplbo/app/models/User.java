package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.util.HashMap;
import java.util.Map;

public class User {
    private Integer userId; // Integer allows null for unsaved users
    private String username;
    private String userEmail;
    private String userPhoneNumber;
    private String userPasswdHash;
    private boolean isAdmin;

    // The Java equivalent of your BCRYPT_HASH_PATTERN regex
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    /**
     * Constructor (Equivalent to Python __init__)
     * Handles both plain passwords and already hashed passwords
     */
    public User(String username, String email, String passwordOrHash,
                String phoneNumber, boolean admin, Integer userId) {

        this.userId = userId;
        this.username = username;
        this.userEmail = email;
        this.userPhoneNumber = phoneNumber;
        this.isAdmin = admin;

        // Password logic: Check if already hashed using Regex
        if (passwordOrHash != null && passwordOrHash.matches(BCRYPT_PATTERN)) {
            this.userPasswdHash = passwordOrHash;
        } else {
            // If not hashed, hash it now using jBCrypt
            this.userPasswdHash = BCrypt.hashpw(passwordOrHash, BCrypt.gensalt());
        }
    }

    /**
     * Replicates your _execute_update method
     */
    private void executeUpdate(String field, Object value) {
        if (this.userId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            // Uses the updateField helper from your DBConnection utility
            DBConnection.getInstance().updateField("users", "user_id", this.userId, updates);
        }
    }

    // --- SETTERS (Active Record Style: Updates DB immediately) ---

    public void setUsername(String newUsername) {
        this.username = newUsername;
        executeUpdate("username", newUsername);
    }

    public void setUserEmail(String newUserEmail) {
        this.userEmail = newUserEmail;
        executeUpdate("email", newUserEmail);
    }
//
//    public void setUserFullName(String newUserFullName) {
//        this.userFullName = newUserFullName;
//        executeUpdate("full_name", newUserFullName);
//    }

    public void setUserPhoneNumber(String newUserPhoneNumber) {
        this.userPhoneNumber = newUserPhoneNumber;
        executeUpdate("phone_number", newUserPhoneNumber);
    }

    public void giveUserAdmin() {
        this.isAdmin = true;
        executeUpdate("role_id", 1); // 1 for Admin
    }

    public void rmUserAdmin() {
        this.isAdmin = false;
        executeUpdate("role_id", 2); // 2 for Staff
    }

    public void rmUserPhoneNumber() {
        this.userPhoneNumber = null;
        executeUpdate("phone_number", null);
    }

//    public void rmUserFullName() {
//        this.userFullName = null;
//        executeUpdate("full_name", null);
//    }

    // --- GETTERS ---

    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getUserEmail() { return userEmail; }
    public String getUserPhoneNumber() { return userPhoneNumber; }
    public String getUserPasswdHash() { return userPasswdHash; }
    public boolean isAdmin() { return isAdmin; }

    /**
     * Replicates your to_dict() method
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", userId);
        map.put("username", username);
        map.put("email", userEmail);
        map.put("phone_number", userPhoneNumber);
        map.put("admin", isAdmin);
        return map;
    }

    /**
     * Replicates your __str__ method
     */
    @Override
    public String toString() {
        return String.format("User ID: %s, Username: %s, Email: %s, Phone: %s, Admin: %s",
                userId, username, userEmail,
                userPhoneNumber != null ? userPhoneNumber : "N/A",
                isAdmin ? "Yes" : "No");
    }
}