package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.*;
import java.sql.*;
import java.util.*;

public class RestockDAO {
    private final DBConnection db = DBConnection.getInstance();

    /**
     * Menjalankan proses restok secara ATOMIK.
     * 1. Simpan Pengeluaran (Expense)
     * 2. Update Stok Barang (+ quantity)
     * 3. Catat Log Audit (StockMovementLog)
     * 4. Potong Saldo Kas (KasBalance)
     */
    public boolean executeRestock(Expense expense, List<ExpenseItem> items) {
        Connection conn = null;
        StockMovementDAO auditDAO = new StockMovementDAO();

        try {
            conn = db.getConnection();
            conn.setAutoCommit(false); // MULAI TRANSAKSI

            // 1. Simpan Header Pengeluaran & Ambil ID
            Integer expenseId = db.insertIntoTableAndGetId("expenses", expense.toMap());
            if (expenseId == null) throw new SQLException("Gagal mencatat pengeluaran.");

            for (ExpenseItem ei : items) {
                // 2. Ambil Stok Lama & Kunci Baris (Pessimistic Locking)
                String checkSql = "SELECT stock FROM items WHERE id = ? FOR UPDATE";
                int currentStock = 0;
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, ei.getItemId());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) currentStock = rs.getInt("stock");
                }

                // 3. Update Stok di Database
                int newStock = currentStock + ei.getQuantity();
                String updateSql = "UPDATE items SET stock = ?, version = version + 1 WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, newStock);
                    ps.setInt(2, ei.getItemId());
                    ps.executeUpdate();
                }

                // 4. Catat ke Riwayat Perubahan Stok (Otomatis)
                auditDAO.logChangeInTransaction(conn, ei.getItemId(), expense.getUserId(),
                        ei.getQuantity(), "RESTOK_SUPPLIER #EXP-" + expenseId);

                // 5. Simpan Detail Item Pengeluaran
                Map<String, Object> eiMap = ei.toMap();
                eiMap.put("expense_id", expenseId); // Link ke header
                db.insertIntoTable("expense_items", eiMap);
            }

            // 6. Potong Saldo Kas
            String updateKasSql = "UPDATE kas SET balance = balance - ? WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(updateKasSql)) {
                ps.setDouble(1, expense.getTotal());
                ps.executeUpdate();
            }

            conn.commit(); // SELESAI & SIMPAN SEMUA
            System.out.println("✅ Restok Berhasil. Stok bertambah & Kas berkurang.");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Restok Gagal: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            db.releaseConnection(conn);
        }
    }
}