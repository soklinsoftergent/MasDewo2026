package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.SaleItem;

import java.sql.Connection;
import java.util.ArrayList;
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

    public double getTotalRevenue() {
        Object res = db.fetchOneByKeyColumn("SUM(total_amount)", "sales", "is_cancelled", 0);
        return res != null ? ((Number) res).doubleValue() : 0.0;
    }

    public double getTotalProfit() {
        Object res = db.fetchOneByKeyColumn("SUM(profit)", "sales", "is_cancelled", 0);
        return res != null ? ((Number) res).doubleValue() : 0.0;
    }

    public int getTransactionCount() {
        Object res = db.fetchOneByKeyColumn("COUNT(*)", "sales", "is_cancelled", 0);
        return res != null ? ((Number) res).intValue() : 0;
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
        String sql = "SELECT s.*, c.name as customer_name, u.username as cashier_name " +
                "FROM sales s " +
                "LEFT JOIN customers c ON s.cust_id = c.cust_id " +
                "LEFT JOIN users u ON s.user_id = u.user_id " +
                "ORDER BY s.created_at DESC";
        return db.selectAllCustom(sql); // Use a custom query method in DBConnection
    }

}
