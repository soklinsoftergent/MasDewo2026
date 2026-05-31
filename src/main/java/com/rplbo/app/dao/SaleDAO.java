package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Sale;
import com.rplbo.app.models.SaleItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleDAO {

    private final DBConnection db =  DBConnection.getInstance();

    public boolean voidSale(int saleId) {
        // 1. Get Sale and Items
        // 2. Start Transaction
        // 3. Set is_cancelled = 1
        // 4. Loop through sale_items and ADD stock back to items table
        // 5. Create an EXPENSE in Kas for the lost revenue if already paid
        // 6. Log the action
        return true; // Simplified for logic overview
    }

    public List<SaleItem> getItemsForSale(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        // Use your generic helper to fetch all rows from sale_items
        List<Map<String, Object>> data = db.selectAll("sale_items");

        for (Map<String, Object> row : data) {
            if (((Number) row.get("sale_id")).intValue() == saleId) {
                items.add(new SaleItem(row));
            }
        }
        return items;
    }

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

    public List<Map<String, Object>> getAllSalesWithDetails() {
        // Complex join done in DAO, returning raw maps for UI rows
        String sql = "SELECT s.*, c.name as cust_name, u.username as staff_name FROM sales s " +
                "LEFT JOIN customers c ON s.cust_id = c.cust_id " +
                "LEFT JOIN users u ON s.user_id = u.user_id ORDER BY created_at DESC";
        // Logic to execute and return list...
        return db.selectAllCustom(sql);
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

    public static synchronized boolean executeFullSale(Sale sale, List<SaleItem> items) {
        DBConnection db = DBConnection.getInstance();
        Connection conn = null;
        StockMovementDAO auditDAO = new StockMovementDAO();

        try {
            conn = db.getConnection();
            conn.setAutoCommit(false); // Kunci Transaksi Dimulai

            // 1. Simpan Header Penjualan
            Integer newSaleId = db.insertIntoTableAndGetId("sales", sale.toMap());
            if (newSaleId == null) throw new SQLException("Gagal membuat ID penjualan.");

            // SQL untuk ambil stok dan nama (Manual agar tetap dalam satu koneksi transaksi)
            String checkSql = "SELECT stock, name FROM items WHERE id = ? FOR UPDATE";

            for (SaleItem si : items) {
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, si.getItemId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Barang ID " + si.getItemId() + " tidak ada.");

                        int currentStock = rs.getInt("stock");
                        String itemName = rs.getString("name");

                        // 2. Validasi Stok
                        if (currentStock < si.getQuantity()) {
                            throw new SQLException("Stok tidak cukup untuk: " + itemName);
                        }

                        // 3. Update Stok (Gunakan koneksi yang sama)
                        int updatedStock = currentStock - si.getQuantity();
                        String updateStockSql = "UPDATE items SET stock = ? WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateStockSql)) {
                            updateStmt.setInt(1, updatedStock);
                            updateStmt.setInt(2, si.getItemId());
                            updateStmt.executeUpdate();
                        }

                        // 4. Catat ke Audit Trail (Otomatis)
                        auditDAO.logChangeInTransaction(
                                conn, // Kirim koneksi yang sama!
                                si.getItemId(),
                                sale.getUserId(),
                                -si.getQuantity(),
                                "PENJUALAN #INV-" + String.format("%04d", newSaleId)
                        );

                        // 5. Simpan SaleItem
                        si.setSaleId(newSaleId);
                        // Kita asumsikan insertIntoTable juga dimodifikasi untuk menerima Connection
                        // atau lakukan manual di sini:
                        String insertItemSql = "INSERT INTO sale_items (sale_id, item_id, quantity, unit_price, total_price) VALUES (?,?,?,?,?)";
                        try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                            itemStmt.setInt(1, newSaleId);
                            itemStmt.setInt(2, si.getItemId());
                            itemStmt.setInt(3, si.getQuantity());
                            itemStmt.setDouble(4, si.getUnitPrice());
                            itemStmt.setDouble(5, si.getTotalPrice());
                            itemStmt.executeUpdate();
                        }
                    }
                }
            }

            conn.commit(); // BERHASIL: Simpan permanen
            System.out.println("✅ Transaksi #INV-" + newSaleId + " sukses dan log audit tercipta.");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Transaksi Gagal: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback(); // BATALKAN SEMUA: Stok kembali, Log dihapus
                    System.err.println("↩️ Database telah di-rollback.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            db.releaseConnection(conn); // Kembalikan koneksi ke pool
        }
    }

    // Di dalam SaleDAO.java
    public List<Sale> getSalesForStaff(int userId) {
        String sql = "SELECT * FROM sales WHERE user_id = ? ORDER BY created_at DESC";

        // Panggil selectAllCustom dengan parameter userId
        List<Map<String, Object>> rows = db.selectAllCustom(sql, userId);

        List<Sale> sales = new ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                sales.add(new Sale(row));
            }
        }
        return sales;
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
}
