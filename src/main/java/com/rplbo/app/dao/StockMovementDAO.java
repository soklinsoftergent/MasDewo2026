package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockMovementDAO {

    /**
     * Metode internal untuk mencatat log di dalam sebuah transaksi yang sedang berjalan.
     */
    public void logChangeInTransaction(Connection conn, int itemId, int userId, int change, String reason) throws SQLException {
        String sql = "INSERT INTO stock_movement_logs (item_id, user_id, quantity_changed, reason) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, change);
            pstmt.setString(4, reason);
            pstmt.executeUpdate();
            System.out.println("📝 [Audit] Log tercatat otomatis: " + reason);
        }
    }

    public List<Map<String, Object>> getHistoryForItem(int itemId) {
        // JOIN with users to show the name of the person who made the change
        String sql = "SELECT l.*, u.username FROM stock_movement_logs l " +
                "JOIN users u ON l.user_id = u.user_id " +
                "WHERE l.item_id = ? ORDER BY l.created_at DESC";

        // Use a custom query method in your DBConnection...
        return DBConnection.getInstance().selectAllCustom(sql, itemId);
    }
}