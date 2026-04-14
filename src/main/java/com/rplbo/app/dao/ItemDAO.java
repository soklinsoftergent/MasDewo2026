package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Item;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemDAO {
    private final DBConnection db;

    public ItemDAO() {
        this.db = DBConnection.getInstance();
    }

    /**
     * Fetch every item in the warehouse.
     * Useful for the main Inventory Table.
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try (ResultSet rs = db.selectAll("items")) {
            while (rs != null && rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Search items by name.
     * Useful for the Search Bar in the UI.
     */
    public List<Item> searchByName(String query) {
        List<Item> items = new ArrayList<>();
        // Use a custom query for LIKE search
        String sql = "SELECT * FROM items WHERE name LIKE ?";
        try (java.sql.PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Find items that are running low on stock.
     * Essential for the "stok kritis" badge on your Dashboard.
     */
    public List<Item> getLowStockItems(int threshold) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE stock <= ?";
        try (java.sql.PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, threshold);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    /**
     * Update stock level (positive for restock, negative for sale).
     * Uses your generic updateField method.
     */
    public boolean updateStock(int itemId, int currentStock) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("stock", currentStock);
        return db.updateField("items", "id", itemId, updates);
    }

    /**
     * Helper to convert Database Row -> Java Object
     */
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        return new Item(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("stock"),
                rs.getInt("it_ty_id"),
                rs.getDouble("purchase_price"),
                rs.getDouble("selling_price")
        );
    }
    public String getTypeNameById(int typeId) {
        try (ResultSet rs = db.fetchOneByKeyColumn("name", "item_types", "it_ty_id", typeId)) {
            if (rs != null && rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}