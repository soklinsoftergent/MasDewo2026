package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.util.LinkedHashMap;
import java.util.Map;

public class User {
    private Integer userId; // Integer allows null for unsaved users
    private String username;
    private String userEmail;
    private String userPhoneNumber;
    private String userPasswdHash;
    private boolean isAdmin;
    private boolean isActive;

    // The Java equivalent of your BCRYPT_HASH_PATTERN regex
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    /**
     * Constructor for creating a NEW User
     * (Automatically hashes the password)
     */
    public User(String username, String email, String password, String phone, boolean admin) {
        this.username = username;
        this.userEmail = email;
        this.userPhoneNumber = phone;
        this.isAdmin = admin;
        this.isActive = true;

        // Hash password immediately
        this.userPasswdHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Constructor for loading from Database Map
     * Matches the output of db.selectAll("users") or db.fetchRow()
     */
    public User(Map<String, Object> data) {
        this.userId = (Integer) data.get("user_id");
        this.username = (String) data.get("username");
        this.userEmail = (String) data.get("email");
        this.userPhoneNumber = (String) data.get("phonenumber");
        this.userPasswdHash = (String) data.get("password_hash");

        // Role ID mapping: 1 = Admin, 2 = Staff
        int roleId = ((Number) data.get("role_id")).intValue();
        this.isAdmin = (roleId == 1);

        // Handle boolean is_active
        Object activeObj = data.get("is_active");
        this.isActive = (activeObj == null || (Boolean) activeObj);
    }

    /**
     * Replicates your _execute_update method
     */
    private void executeUpdate(String field, Object value) {
        if (this.userId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("users", "user_id", this.userId, updates);
        }
    }

    public boolean save() {
        if (this.userId != null) return false;
        Map<String, Object> data = toMap();
        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("users", data);
        if (newId != null) {
            this.userId = newId;
            return true;
        }
        return false;
    }

    public void refresh() {
        if (this.userId == null) return;
        Map<String, Object> data = DBConnection.getInstance().fetchRow("users", "user_id", this.userId);
        if (data != null) {
            this.username = (String) data.get("username");
            this.userEmail = (String) data.get("email");
            this.userPhoneNumber = (String) data.get("phonenumber");
            int roleId = ((Number) data.get("role_id")).intValue();
            this.isAdmin = (roleId == 1);
        }
    }

    // --- SETTERS (Active Record Style: Updates DB immediately) ---
    public void setEmail(String email) { this.userEmail = email; executeUpdate("email", email); }
    public void setPhoneNumber(String phone) { this.userPhoneNumber = phone; executeUpdate("phonenumber", phone); }
    public void setAdmin(boolean admin) {this.isAdmin = admin;executeUpdate("role_id", admin ? 1 : 2);}
    public void setActive(boolean active) {this.isActive = active;executeUpdate("is_active", active);}
    public void setUsername(String newUsername) {this.username = newUsername;executeUpdate("username", newUsername);}
    public void setUserEmail(String newUserEmail) {this.userEmail = newUserEmail;executeUpdate("email", newUserEmail);}
    public void setUserPhoneNumber(String newUserPhoneNumber) {this.userPhoneNumber = newUserPhoneNumber;executeUpdate("phonenumber", newUserPhoneNumber);}
    public void giveUserAdmin() {this.isAdmin = true;executeUpdate("role_id", 1); }
    public void rmUserAdmin() {this.isAdmin = false;executeUpdate("role_id", 2); }
    public void rmUserPhoneNumber() {this.userPhoneNumber = null;executeUpdate("phonenumber", null);}
    public void setUserPasswdHash(String passwordHash) { this.userPasswdHash = passwordHash; executeUpdate("password_hash", passwordHash);}

    // --- GETTERS ---

    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getUserEmail() { return userEmail; }
    public String getUserPhoneNumber() { return userPhoneNumber; }
    public String getUserPasswdHash() { return userPasswdHash; }
    public String getPasswordHash() { return userPasswdHash; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isActive() { return isActive; }

    /**
     * Replicates your to_dict() method
     */
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", this.username);
        data.put("email", this.userEmail);
        data.put("password_hash", this.userPasswdHash);
        data.put("phonenumber", this.userPhoneNumber);
        data.put("role_id", this.isAdmin ? 1 : 2);
        data.put("is_active", this.isActive);
        return data;
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

    public static void main(String[] args) {
        DBConnection.initialize();
        System.out.println(DBConnection.getInstance().selectAll("users"));
        User newUser = new User("Dewa", "masdewo@gmail.com", "masdewo123", "082135317234", true);
        newUser.save();
        System.out.println(DBConnection.getInstance().selectAll("users"));
    }
}
