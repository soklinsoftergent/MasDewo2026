package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import com.rplbo.app.util.ValidationUtil;

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

    public boolean registerNewUser(String username, String email, String password) {
        if (!ValidationUtil.isValidEmail(email)) {
            System.err.println("Error: format email salah");
            return false;
        }

        //Jika valid, buat obyek user dan simpan

        User newUser = new User(username, email, password, "081", false);
        return newUser.save();
    }

    // Tambahkan ini di UserDAO.java
    public boolean isUsernameOrEmailExists(String username, String email) {
        String sql = "SELECT count(*) FROM users WHERE username = ? OR email = ?";
        List<Map<String, Object>> res = DBConnection.getInstance().selectAllCustom(sql, username, email);
        if (res != null && !res.isEmpty()) {
            int count = Integer.parseInt(res.get(0).get("count(*)").toString());
            return count > 0;
        }
        return false;
    }

    public List<Map<String, Object>> getUnifiedEmployeeLogs(int userId) {
        String sql =
                "SELECT 'Penjualan' as aksi, CONCAT('#INV-', LPAD(sale_id, 4, '0')) as referensi, total_amount as nominal, created_at as waktu " +
                        "FROM sales WHERE user_id = ? " +
                        "UNION ALL " +
                        "SELECT 'Restok' as aksi, CONCAT('#EXP-', LPAD(expense_id, 4, '0')) as referensi, total as nominal, created_at as waktu " +
                        "FROM expenses WHERE user_id = ? " +
                        "UNION ALL " +
                        "SELECT 'Absen Masuk' as aksi, '-' as referensi, 0 as nominal, clock_in as waktu " +
                        "FROM attendance WHERE user_id = ? AND clock_in IS NOT NULL " +
                        "UNION ALL " +
                        "SELECT 'Absen Keluar' as aksi, '-' as referensi, 0 as nominal, clock_out as waktu " +
                        "FROM attendance WHERE user_id = ? AND clock_out IS NOT NULL " +
                        "ORDER BY waktu DESC";

        // Kita kirim userId 4 kali karena ada 4 tanda tanya (?) di query UNION
        return DBConnection.getInstance().selectAllCustom(sql, userId, userId, userId, userId);
    }

}
