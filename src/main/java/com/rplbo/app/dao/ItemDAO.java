package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Item;
import com.rplbo.app.util.InventoryTrie;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ItemDAO {
    private InventoryTrie searchTree = new InventoryTrie();

    public void initializeSearchTree() {
        List<Item> all = getAllItems();
        for (Item item : all) {
            searchTree.insert(item);
        }
        System.out.println("Search tree initialized");
    }

    /**
     * Use this to implement a search Bar in the UI
     * @param query
     * @return
     */
    public List<Item> searchFast(String query) {
        return searchTree.search(query);
    }

    /**
     * Fetch every item in the warehouse.
     * Useful for the main Inventory Table.
     */
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        List<Map<String, Object>> data = DBConnection.getInstance().selectAll("items");

        for (Map<String, Object> row : data) {
            items.add(new Item(row));
        }
        return items;
    }



    public boolean restockItem(int itemId, int quantity, int userId) {
        // 1. Get current item
        Map<String, Object> data = DBConnection.getInstance().fetchRow("items", "id", itemId);
        if (data == null) {return false;}

        Item item = new Item(data);

        // 2. Update stock
        item.setStock(item.getStock() + quantity);

        // 3. Log the movement (Your Audit Trail feature!)
        StockMovementDAO logDAO = new StockMovementDAO();
        logDAO.logChange(itemId, userId, quantity, "MANUAL_RESTOCK");

        return true;
    }
    /**
     * Find items that are running low on stock.
     * Essential for the "stok kritis" badge on your Dashboard.
     */
    public List<Item> getLowStockItems(int threshold) {
        List<Item> items = new ArrayList<>();
        // Instead of writing a new SQL query, we can filter our already-loaded list
        // or write a quick SQL statement:
        String sql = "SELECT * FROM items WHERE stock <= " + threshold;

        // Using the generic selectAll logic but with a filter:
        for (Map<String, Object> row : DBConnection.getInstance().selectAll("items")) {
            int stock = ((Number) row.get("stock")).intValue();
            if (stock <= threshold) {
                items.add(new Item(row));
            }
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
        return DBConnection.getInstance().updateField("items", "id", itemId, updates);
    }

    public String getTypeNameById(int typeId) {
        Object result = DBConnection.getInstance().fetchOneByKeyColumn("name", "item_types", "it_ty_id", typeId);
        return (result != null) ? result.toString() : "Unknown";
    }

    public Map<String, Integer> getCategoryDistribution() {
        Map<String, Integer> dist = new HashMap<>();
        String sql = "SELECT t.name, COUNT(i.id) FROM items i " +
                "JOIN item_types t ON i.it_ty_id = t.it_ty_id GROUP BY t.name";
        // Logic to run query and populate map...
        return dist;
    }
}