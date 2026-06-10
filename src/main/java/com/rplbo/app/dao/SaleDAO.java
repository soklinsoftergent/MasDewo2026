package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Item;
import com.rplbo.app.models.KasTransaction;
import com.rplbo.app.models.Sale;
import com.rplbo.app.models.SaleItem;
import com.rplbo.app.services.OmniSyncService;
import com.rplbo.app.util.DataStateSignal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SaleDAO {

    private final DBConnection db =  DBConnection.getInstance();

    // Di dalam SaleDAO.java

    public double getTotalRevenue() {
        Object res = db.fetchOneByKeyColumn("SUM(total_amount)", "sales", "is_cancelled", 0);
        if (res == null || res.toString().equals("null")) return 0.0;

        // Gunakan toString() lalu parse. Ini anti-error casting.
        return Double.parseDouble(res.toString());
    }

    public double getTotalProfit() {
        Object res = db.fetchOneByKeyColumn("SUM(profit)", "sales", "is_cancelled", 0);
        if (res == null || res.toString().equals("null")) return 0.0;

        return Double.parseDouble(res.toString());
    }

    public int getTransactionCount() {
        Object res = db.fetchOneByKeyColumn("COUNT(*)", "sales", "is_cancelled", 0);
        if (res == null || res.toString().equals("null")) return 0;

        // Untuk integer, parse ke Double dulu baru ke int (menghindari error jika ada desimal)
        return (int) Double.parseDouble(res.toString());
    }

    public List<Map<String, Object>> getAllSalesDetailed() {
        // Tambahkan s.* agar semua field model Sale terpenuhi
        String sql = "SELECT s.*, c.name AS customer_name, u.username AS cashier_name, e.ecom_name AS platform_name, " +
                "(SELECT COUNT(*) FROM sale_items WHERE sale_id = s.sale_id) AS item_count " +
                "FROM sales s " +
                "LEFT JOIN customers c ON s.cust_id = c.cust_id " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "LEFT JOIN ecommerces e ON s.ecom_id = e.ecom_id " +
                "ORDER BY s.created_at DESC";
        return db.selectAllCustom(sql);
    }

    // Tambahkan/Update di SaleDAO.java
    public List<Map<String, Object>> getItemsForSaleDetailed(int saleId) {
        String sql = "SELECT si.*, i.name AS item_name " +
                "FROM sale_items si " +
                "JOIN items i ON si.item_id = i.id " +
                "WHERE si.sale_id = ?";
        return DBConnection.getInstance().selectAllCustom(sql, saleId);
    }

    /**
     * BAYAR: Memproses transaksi baru.
     */
    public static synchronized boolean executeFullSale(Sale sale, List<SaleItem> items) {
        DBConnection db = DBConnection.getInstance();
        Connection conn = null;
        boolean success; // 1. Definisikan flag success

        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            // --- PROSES DATABASE LOKAL ---

            // 1. Simpan Header Penjualan
            Integer newSaleId = db.insertIntoTableAndGetId(conn, "sales", sale.toMap());
            if (newSaleId == null) throw new SQLException("Gagal simpan penjualan.");

            for (SaleItem si : items) {
                // A. Ambil Stok Terbaru & Kunci Baris
                int currentStock = 0;
                String checkSql = "SELECT stock FROM items WHERE id = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setInt(1, si.getItemId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) currentStock = rs.getInt("stock");
                        else throw new SQLException("Barang ID " + si.getItemId() + " tidak ditemukan.");
                    }
                }

                // B. Validasi Stok
                if (currentStock < si.getQuantity()) {
                    throw new SQLException("Stok tidak cukup untuk barang ID: " + si.getItemId());
                }

                // C. Potong Stok
                db.updateField(conn, "items", "id", si.getItemId(), Map.of("stock", currentStock - si.getQuantity()));

                // D. Sinkronisasi ID & Simpan Detail
                si.setSaleId(newSaleId);
                db.insertIntoTable(conn, "sale_items", si.toMap());

                // E. Catat Audit Log
                new StockMovementDAO().logChangeInTransaction(conn, si.getItemId(), sale.getUserId(),
                        -si.getQuantity(), "PENJUALAN #INV-" + newSaleId);
            }

            // 2. Catat Keuangan (Kas Transaction)
            Map<String, Object> kasData = new LinkedHashMap<>();
            kasData.put("user_id", sale.getUserId());
            kasData.put("type", "INCOME");
            kasData.put("amount", sale.getTotalAmount());
            kasData.put("description", "Penjualan #INV-" + String.format("%04d", newSaleId));
            db.insertIntoTableAndGetId(conn, "kas_transactions", kasData);

            // 3. Update Saldo Kas Utama
            String sqlUpdateKas = "UPDATE kas SET balance = balance + ? WHERE id = 1";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateKas)) {
                ps.setDouble(1, sale.getTotalAmount());
                int rowsAffected = ps.executeUpdate();
                success = rowsAffected >= 1;
                if (success) {
                    conn.commit();
                    DataStateSignal.fireAll();
                    System.out.println("[SaleDAO] Database Lokal Berhasil Diupdate.");
//                    return true;
                }
            }
//            // 4. COMMIT TRANSAKSI LOKAL
//            conn.commit();
            success = true; // Set ke true jika semua SQL di atas berhasil
//            // Di dalam executeFullSale atau executeRestock, tepat setelah conn.commit()
//            if (success) {
//                conn.commit();
//                com.rplbo.app.util.DataStateSignal.fireAll(); // Nyalakan sinyal perubahan
//                return true;
//            }
        } catch (Exception e) {
            System.err.println("Transaksi Gagal: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            success = false;
        } finally {
            db.releaseConnection(conn);
        }

        // --- SINKRONISASI OMNICHANNEL (OUTSIDE TRANSACTION) ---
        if (success) {
            // Jalankan di thread terpisah agar UI Kasir tidak membeku (lag) saat kirim data ke internet
            new Thread(() -> {
                try {
                    System.out.println("Memulai sinkronisasi stok ke marketplace...");
                    OmniSyncService sync = new OmniSyncService();

                    for (SaleItem si : items) {
                        // Ambil data barang terbaru dari DB (stok yang sudah dikurangi)
                        Map<String, Object> itemData = db.fetchRow("items", "id", si.getItemId());
                        if (itemData != null) {
                            Item freshItem = new Item(itemData);
                            // Update stok di Shopee, Tokopedia, dll.
                            sync.syncStockToAllPlatforms(freshItem);
                        }
                    }
                    System.out.println("Semua platform sudah sinkron.");
                } catch (Exception e) {
                    System.err.println("Gagal sinkronisasi cloud: " + e.getMessage());
                }
            }).start();
        }
        return success;
    }

    /**
     * BATAL: Membatalkan transaksi yang sudah sukses.
     */
    public boolean voidSale(int saleId, int adminId) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            // 1. Cek apakah sudah dibatalkan
            Map<String, Object> sale = db.fetchRow("sales", "sale_id", saleId);
            if ((int)sale.get("is_cancelled") == 1) return false;

            // 2. Balikkan Stok
            List<Map<String, Object>> items = getItemsForSaleDetailed(saleId);
            for (Map<String, Object> item : items) {
                int itemId = (int) item.get("item_id");
                int qty = (int) item.get("quantity");
                db.updateField(conn, "items", "id", itemId, Map.of("stock", "stock + " + qty));
                new StockMovementDAO().logChangeInTransaction(conn, itemId, adminId, qty, "VOID #INV-" + saleId);
            }

            // 3. Update Status Batal
            db.updateField(conn, "sales", "sale_id", saleId, Map.of("is_cancelled", 1));

            // 4. Potong Kas (Refund)
            double amount = ((Number) sale.get("total_amount")).doubleValue();
            db.updateField(conn, "kas", "id", 1, Map.of("balance", "balance - " + amount));
            KasTransaction kt = new KasTransaction(adminId, "EXPENSE", amount, "Pembatalan #INV-" + saleId);
            db.insertIntoTableAndGetId(conn, "kas_transactions", kt.toMap());

            conn.commit();
            return true;
        } catch (Exception e) {
            try { if(conn!=null) conn.rollback(); } catch (Exception ex) {}
            return false;
        } finally { db.releaseConnection(conn); }
    }

    public double getTotalSalesInShift(int userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        String sql = "SELECT SUM(total_amount) FROM sales " +
                "WHERE user_id = ? AND created_at BETWEEN ? AND ? AND is_cancelled = 0";

        List<Map<String, Object>> res = db.selectAllCustom(sql, userId, startTime, endTime);

        if (res != null && !res.isEmpty()) {
            Object val = res.get(0).get("SUM(total_amount)");
            return (val != null) ? ((Number) val).doubleValue() : 0.0;
        }
        return 0.0;
    }

    public List<Map<String, Object>> getMonthlyReportData() {
        String sql = "SELECT " +
                     "DATE(s.created_at) as Tanggal, " +
                     "COUNT(s.sale_id) as Total_Transaksi, " +
                     "SUM(s.total_amount) as Omset, " +
                     "SUM(s.profit) as Total_Laba " +
                     "FROM sales s " +
                     "WHERE s.is_cancelled = 0 " +
                     "GROUP BY DATE(s.created_at) " +
                     "ORDER BY s.created_at DESC";
        return db.selectAllCustom(sql);
    }

    public Map<String, Object> getClosingReport(int userId) {
        String sql = "SELECT COUNT(*) as total_orders, SUM(total_amount) as total_cash " +
                "FROM sales WHERE user_id = ? AND DATE(created_at) = CURRENT_DATE AND is_cancelled = 0";
        List<Map<String, Object>> results = db.selectAllCustom(sql, userId);
        return (results != null && !results.isEmpty()) ? results.get(0) : null;
    }

    public List<Map<String, Object>> getDetailedSalesReport() {
        String sql = "SELECT s.sale_id AS 'ID Invois', " +
                "c.name AS 'Pelanggan', " +
                "u.username AS 'Kasir', " +
                "s.total_amount AS 'Total Harga', " +
                "s.profit AS 'Laba', " +
                "s.created_at AS 'Waktu Transaksi' " +
                "FROM sales s " +
                "LEFT JOIN customers c ON s.cust_id = c.cust_id " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "WHERE s.is_cancelled = 0 " +
                "ORDER BY s.created_at DESC";

        return db.selectAllCustom(sql); // Menggunakan selectAllCustom yang mendukung Varargs
    }

    /**
     * Mengambil informasi detail satu penjualan beserta nama relasinya.
     */
    public Map<String, Object> getSaleWithDetails(int saleId) {
        String sql = "SELECT s.*, c.name AS customer_name, u.username AS cashier_name, e.ecom_name AS platform_name " +
                "FROM sales s " +
                "LEFT JOIN customers c ON s.cust_id = c.cust_id " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "LEFT JOIN ecommerces e ON s.ecom_id = e.ecom_id " +
                "WHERE s.sale_id = ?";

        List<Map<String, Object>> result = db.selectAllCustom(sql, saleId);
        return (result != null && !result.isEmpty()) ? result.get(0) : null;
    }

    // Tambahkan di SaleDAO.java
    public List<Map<String, Object>> getSalesByUserIdDetailed(int userId) {
        String sql = "SELECT s.*, c.name AS customer_name, u.username AS cashier_name, " +
                "e.ecom_name AS platform_name, " +
                "(SELECT COUNT(*) FROM sale_items WHERE sale_id = s.sale_id) AS item_count " +
                "FROM sales s " +
                "LEFT JOIN customers c ON s.cust_id = c.cust_id " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "LEFT JOIN ecommerces e ON s.ecom_id = e.ecom_id " +
                "WHERE s.user_id = ? " + // <--- FILTER NYA DI SINI
                "ORDER BY s.created_at DESC";
        return db.selectAllCustom(sql, userId);
    }

}
