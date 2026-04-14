package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Item {
    private Integer id;
    private String name;
    private int stock;
    private int itemTypeId;
    private double purchasePrice;
    private double sellingPrice;

    /**
     * Constructor for creating a NEW Item (ID not yet set)
     */
    public Item(String name, int stock, int itemTypeId, double purchasePrice, double sellingPrice) {
        this.name = name;
        this.stock = stock;
        this.itemTypeId = itemTypeId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    /**
     * Constructor for loading an Item from the Database (ID included)
     */
    public Item(Integer id, String name, int stock, int itemTypeId, double purchasePrice, double sellingPrice) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.itemTypeId = itemTypeId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.id != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            // Uses 'id' as the primary key column name from our SQL schema
            DBConnection.getInstance().updateField("items", "id", this.id, updates);
        }
    }

    // --- Business Logic Methods (From Python) ---

    /**
     * Checks if the item is sold out based on current stock
     */
    public boolean isSoldOut() {
        return this.stock <= 0;
    }

    /**
     * Increments item stock and persists the change to the database
     */
    public void addItemStock(int amount) {
        this.stock += amount;
        executeUpdate("stock", this.stock);
    }

    // --- Setters (Triggers immediate DB update) ---

    public void setName(String name) {
        this.name = name;
        executeUpdate("name", name);
    }

    public void setStock(int stock) {
        this.stock = stock;
        executeUpdate("stock", stock);
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
        executeUpdate("purchase_price", purchasePrice);
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
        executeUpdate("selling_price", sellingPrice);
    }

    // --- Getters ---

    public Integer getId() { return id; }
    public String getName() { return name; }
    public int getStock() { return stock; }
    public int getItemTypeId() { return itemTypeId; }
    public double getPurchasePrice() { return purchasePrice; }
    public double getSellingPrice() { return sellingPrice; }

    // --- Database Operations ---

    /**
     * Persists a NEW item to the database and retrieves the generated ID
     */
    public boolean save() {
        if (this.id != null) return false; // Already exists

        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("stock", this.stock);
        data.put("it_ty_id", this.itemTypeId);
        data.put("purchase_price", this.purchasePrice);
        data.put("selling_price", this.sellingPrice);

        boolean success = DBConnection.getInstance().insertIntoTable("items", data);

        if (success) {
            // Replicate Python's fetch of the LAST_INSERT_ID()
            try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("LAST_INSERT_ID()", "items", "1", 1)) {
                if (rs != null && rs.next()) {
                    this.id = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(
                "Item ID         : %d\n" +
                        "Name            : %s\n" +
                        "Stock           : %d\n" +
                        "Type ID         : %d\n" +
                        "Purchase Price  : %.2f\n" +
                        "Selling Price   : %.2f",
                id, name, stock, itemTypeId, purchasePrice, sellingPrice
        );
    }
}