package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.*;
import java.sql.*;
import java.util.*;

public class RestockDAO {
    private final DBConnection db = DBConnection.getInstance();

    public boolean executeRestock(Expense expense, List<ExpenseItem> items, int userId) {
        Connection conn = null;
        StockMovementDAO auditDAO = new StockMovementDAO();

        try {
            conn = db.getConnection();
            conn.setAutoCommit(false); // 🛡️ START ATOMIC TRANSACTION

            // 1. Insert Expense Header
            Integer expenseId = db.insertIntoTableAndGetId(conn, "expenses", expense.toMap());
            if (expenseId == null) throw new SQLException("Gagal membuat header pengeluaran.");

            for (ExpenseItem ei : items) {
                // 2. Lock and Update Stock
                String fetchSql = "SELECT stock FROM items WHERE id = ? FOR UPDATE";
                int currentStock = 0;
                try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                    ps.setInt(1, ei.getItemId());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) currentStock = rs.getInt("stock");
                }

                int newStock = currentStock + ei.getQuantity();
                db.updateField(conn, "items", "id", ei.getItemId(), Map.of("stock", newStock));

                // 3. Log Stock Movement
                auditDAO.logChangeInTransaction(conn, ei.getItemId(), userId,
                        ei.getQuantity(), "RESTOK #EXP-" + expenseId);

                // 4. Save Expense Detail
                Map<String, Object> detailMap = ei.toMap();
                detailMap.put("expense_id", expenseId);
                db.insertIntoTableAndGetId(conn, "expense_items", detailMap);
            }

            // 5. Update Global Kas Balance
            String sqlKas = "UPDATE kas SET balance = balance - ? WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlKas)) {
                ps.setDouble(1, expense.getTotal());
                ps.executeUpdate();
            }

            // 6. Record Kas Transaction
            KasTransaction kt = new KasTransaction(userId, "EXPENSE", expense.getTotal(), "Restok #EXP-" + expenseId);
            db.insertIntoTableAndGetId(conn, "kas_transactions", kt.toMap());

            conn.commit(); // ✅ EVERYTHING SAVED
            return true;

        } catch (Exception e) {
            System.err.println("🔥 TRANSACTION FAILED: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            db.releaseConnection(conn); // Return to pool
        }
    }
}