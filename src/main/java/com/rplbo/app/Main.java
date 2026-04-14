package com.rplbo.app;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.dao.ItemDAO;
import com.rplbo.app.models.Item;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Connection (Use your dev_user credentials)
        DBConnection.initialize("127.0.0.1", "Dewa", "Supaidaa-M4n", "masdewo");
        ItemDAO itemDAO = new ItemDAO();

        System.out.println("=== 📦 STARTING ITEM DAO TEST ===");

        // TEST 1: Get All Items (Verifies your bulk data import)
        System.out.println("\nTest 1: Fetching all items...");
        List<Item> allItems = itemDAO.getAllItems();
        System.out.println("Total items found: " + allItems.size());
        if (!allItems.isEmpty()) {
            System.out.println("First item in DB: " + allItems.get(0).getName());
        }

        // TEST 2: Search Logic (Simulates the Search Bar)
        System.out.println("\nTest 2: Searching for 'Adapter'...");
        List<Item> searchResults = itemDAO.searchByName("Adapter");
        for (Item item : searchResults) {
            System.out.println("Found: " + item.getName() + " (Stock: " + item.getStock() + ")");
        }

        // TEST 3: Low Stock Badge (Simulates Dashboard alert)
        System.out.println("\nTest 3: Checking for critical stock (<= 3)...");
        List<Item> criticalItems = itemDAO.getLowStockItems(3);
        for (Item item : criticalItems) {
            System.out.println("⚠️ CRITICAL: " + item.getName() + " | Qty: " + item.getStock());
        }

        // TEST 4: Stock Update (Simulates a Sale or Restock)
        if (!allItems.isEmpty()) {
            Item firstItem = allItems.get(0);
            int oldStock = firstItem.getStock();
            int newStock = oldStock + 10;

            System.out.println("\nTest 4: Updating stock for " + firstItem.getName());
            boolean success = itemDAO.updateStock(firstItem.getId(), newStock);

            if (success) {
                System.out.println("✅ Stock updated successfully from " + oldStock + " to " + newStock);
            } else {
                System.out.println("❌ Stock update failed!");
            }
        }

        System.out.println("\n=== ✅ TEST SUITE COMPLETE ===");
    }
}