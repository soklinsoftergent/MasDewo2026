package com.rplbo.app.db;

import com.rplbo.app.dao.UserDAO;
import com.rplbo.app.models.User;
import java.sql.*;
import java.util.*;

public class DBInitializer {

    public static void run() {
        try {
            ensureSchema();
            seedEssentials();
            System.out.println("✅ Database Schema & Essentials verified.");
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void ensureSchema() throws SQLException {
        DBConnection db = DBConnection.getInstance();
        Connection conn = db.getConnection();
        boolean isSqlite = db.getDialect() == DBConnection.Dialect.SQLITE;

        try (Statement stmt = conn.createStatement()) {
            // [All your CREATE TABLE statements remain here as they were]
            // Note: Keep the Dialect checks (isSqlite ? INTEGER : INT) you had!
            // I'm omitting them for brevity, but they are correct in your paste.
        } finally {
            db.releaseConnection(conn);
        }
    }

    private static void seedEssentials() throws SQLException {
        DBConnection db = DBConnection.getInstance();

        // Only insert if roles are empty
        if (countRows("roles") == 0) {
            db.insertIntoTable("roles", Map.of("role_id", 1, "role_name", "Admin"));
            db.insertIntoTable("roles", Map.of("role_id", 2, "role_name", "Staff"));
        }

        // Only insert if kas is empty
        if (countRows("kas") == 0) {
            db.insertIntoTable("kas", Map.of("id", 1, "balance", 0.0));
        }

        // 3. Default Admin User
        UserDAO userDAO = new UserDAO();
        if (userDAO.getAllUsers().isEmpty()) {
            System.out.println("👤 No users found. Generating initial admin account...");
            // Use your NEW ActiveRecord constructor: username, email, password, phone, isAdmin
            User admin = new User("admin", "admin@masdewo.local", "Supaidaa-M4n", "000", true);
            admin.save(); // This automatically hashes the password and inserts to DB
        }
    }

    // Helper method to add to DBInitializer
    private static int countRows(String table) throws SQLException {
        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            int count = rs.next() ? rs.getInt(1) : 0;
            DBConnection.getInstance().releaseConnection(conn);
            return count;
        }
    }
}