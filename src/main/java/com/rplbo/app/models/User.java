package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.util.LinkedHashMap;
import java.util.Map;

public class User extends ActiveRecord {

    private Integer userId;
    private String username;
    private String userEmail;
    private String userPhoneNumber;
    private String userPasswdHash;
    private boolean isAdmin;
    private boolean isActive;

    private static final String BCRYPT_PATTERN =
            "^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$";

    /**
     * Constructor for NEW user
     */
    public User(String username, String email, String password, String phone, boolean admin) {
        this.username = username;
        this.userEmail = email;
        this.userPhoneNumber = phone;
        this.isAdmin = admin;
        this.isActive = true;

        this.userPasswdHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Constructor from database row
     */
    public User(Map<String, Object> data) {
        fromMap(data);
    }

    // =========================================================
    // ACTIVE RECORD IMPLEMENTATION
    // =========================================================

    @Override
    protected String tableName() {
        return "users";
    }

    @Override
    protected String primaryKeyColumn() {
        return "user_id";
    }

    @Override
    protected Integer getId() {
        return this.userId;
    }

    @Override
    protected void setId(Integer id) {
        this.userId = id;
    }

    @Override
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

    @Override
    public void fromMap(Map<String, Object> data) {
        this.userId = ((Number) data.get("user_id")).intValue();
        this.username = (String) data.get("username");
        this.userEmail = (String) data.get("email");
        this.userPhoneNumber = (String) data.get("phonenumber");
        this.userPasswdHash = (String) data.get("password_hash");

        int roleId = ((Number) data.get("role_id")).intValue();
        this.isAdmin = (roleId == 1);

        Object activeObj = data.get("is_active");

        if (activeObj instanceof Boolean) {
            this.isActive = (Boolean) activeObj;
        } else if (activeObj instanceof Number) {
            this.isActive = ((Number) activeObj).intValue() == 1;
        } else {
            this.isActive = true;
        }
    }

    // =========================================================
    // SETTERS (AUTO UPDATE DB)
    // =========================================================

    public void setEmail(String email) {
        this.userEmail = email;
        executeUpdate("email", email);
    }

    public void setPhoneNumber(String phone) {
        this.userPhoneNumber = phone;
        executeUpdate("phonenumber", phone);
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        executeUpdate("role_id", admin ? 1 : 2);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        executeUpdate("is_active", active);
    }

    public void setUsername(String username) {
        this.username = username;
        executeUpdate("username", username);
    }

    public void setUserPasswdHash(String hash) {
        this.userPasswdHash = hash;
        executeUpdate("password_hash", hash);
    }

    public void setPassword(String rawPassword) {
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        setUserPasswdHash(hash);
    }

    // =========================================================
    // GETTERS
    // =========================================================

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public String getUserPasswdHash() {
        return userPasswdHash;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isActive() {
        return isActive;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    public boolean checkPassword(String rawPassword) {
        return BCrypt.checkpw(rawPassword, this.userPasswdHash);
    }

    public boolean isPasswordHashed() {
        return userPasswdHash != null &&
                userPasswdHash.matches(BCRYPT_PATTERN);
    }

    @Override
    public String toString() {
        return String.format(
                "User ID: %s, Username: %s, Email: %s, Phone: %s, Admin: %s",
                userId,
                username,
                userEmail,
                userPhoneNumber != null ? userPhoneNumber : "N/A",
                isAdmin ? "Yes" : "No"
        );
    }

    // RUN INI DULU SUPAYA DAPAT ADMIN
    public static void main(String[] args) {
        DBConnection.initialize();
        User newUser = new User("Dewo", "gentisamudra@gmail.com", "SapeidherM4n-","082135317248", true);
        newUser.save();
    }
}