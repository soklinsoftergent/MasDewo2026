package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.*;
import java.sql.*;
import java.util.*;

public class RestockDAO {
    private final DBConnection db = DBConnection.getInstance();

    public boolean executeRestock(Expense expense, List<ExpenseItem> items, int userId) {
        DBConnection db = DBConnection.getInstance();
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            // 1. Simpan Header Expense
            Integer expenseId = db.insertIntoTableAndGetId(conn, "expenses", expense.toMap());

            for (ExpenseItem ei : items) {
                // 1. SINKRONISASI ID: Pasangkan ID Pengeluaran yang baru dibuat ke detail item
                ei.setExpenseId(expenseId);

                // 2. Ambil Stok Lama & Kunci Row (Pessimistic Locking)
                // Kita lakukan manual agar tetap dalam satu koneksi transaksi 'conn'
                int currentStock = 0;
                String checkSql = "SELECT stock FROM items WHERE id = ?";
                if (db.getDialect() == DBConnection.Dialect.MYSQL) {
                    checkSql += " FOR UPDATE";
                }
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, ei.getItemId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            currentStock = rs.getInt("stock");
                        } else {
                            throw new SQLException("Barang ID " + ei.getItemId() + " tidak ditemukan.");
                        }
                    }
                }

                // 3. Update Stok di tabel 'items' (Gunakan penambahan stok)
                int updatedStock = currentStock + ei.getQuantity();
                Map<String, Object> stockUpdate = new HashMap<>();
                stockUpdate.put("stock", updatedStock);

                // Gunakan overload updateField yang menerima 'conn'
                boolean stockUpdated = db.updateField(conn, "items", "id", ei.getItemId(), stockUpdate);
                if (!stockUpdated) throw new SQLException("Gagal update stok barang.");

                // 4. Simpan ke tabel 'expense_items' (Gunakan overload insert yang menerima 'conn')
                boolean itemSaved = db.insertIntoTable(conn, "expense_items", ei.toMap());
                if (!itemSaved) throw new SQLException("Gagal menyimpan detail pengeluaran barang.");

                // 5. Catat ke Audit Trail Otomatis (Gunakan conn transaksi)
                // Change positif (+) karena ini barang masuk
                new StockMovementDAO().logChangeInTransaction(
                        conn,
                        ei.getItemId(),
                        userId,
                        ei.getQuantity(),
                        "RESTOK_SUPPLIER #EXP-" + expenseId
                );
            }

            // --- FIX: TAMBAHKAN CATATAN KAS DI SINI (Pengeluaran) ---
            Map<String, Object> kasData = new LinkedHashMap<>();
            kasData.put("user_id", userId);
            kasData.put("type", "EXPENSE");
            kasData.put("amount", expense.getTotal());
            kasData.put("description", "Restok Barang #EXP-" + expenseId);
            db.insertIntoTableAndGetId(conn, "kas_transactions", kasData);

            // --- UPDATE SALDO KAS UTAMA ---
            String sqlUpdateKas = "UPDATE kas SET balance = balance - ? WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateKas)) {
                ps.setDouble(1, expense.getTotal());
//                ps.executeUpdate();
                if (ps.executeUpdate() >= 1) {
                    conn.commit();
                    com.rplbo.app.util.DataStateSignal.fireAll(); // Nyalakan sinyal perubahan
                    return true;
                }
            }
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            db.releaseConnection(conn);
        }
        return false;
    }
}