package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ECommerceItem {
    private Integer eCommerceItemId; // Primary Key (ecom_item_id)
    private int itemId;              // Foreign Key to items
    private int ecommerceId;        // Foreign Key to ecommerces
    private Double priceOverride;    // Nullable if using base price
    private LocalDateTime addedAt;

    /**
     * Constructor for creating a NEW ECommerceItem
     */
    public ECommerceItem(int itemId, int ecommerceId, Double priceOverride) {
        this.itemId = itemId;
        this.ecommerceId = ecommerceId;
        this.priceOverride = priceOverride;
        this.addedAt = LocalDateTime.now();
    }

    /**
     * Constructor for loading from the Database
     */
    public ECommerceItem(Integer id, int itemId, int ecommerceId, Double priceOverride, LocalDateTime addedAt) {
        this.eCommerceItemId = id;
        this.itemId = itemId;
        this.ecommerceId = ecommerceId;
        this.priceOverride = priceOverride;
        this.addedAt = addedAt;
    }

    // --- Internal Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.eCommerceItemId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            // Uses the primary key 'ecom_item_id' from our SQL schema
            DBConnection.getInstance().updateField("ecommerce_items", "ecom_item_id", this.eCommerceItemId, updates);
        }
    }

    // --- Getters ---

    public Integer getECommerceItemId() { return eCommerceItemId; }
    public int getItemId() { return itemId; }
    public int getEcommerceId() { return ecommerceId; }
    public Double getPriceOverride() { return priceOverride; }
    public LocalDateTime getAddedAt() { return addedAt; }

    // --- Setters (Updates DB immediately) ---

    public void setPriceOverride(Double newPrice) {
        this.priceOverride = newPrice;
        executeUpdate("price_override", newPrice);
    }

    public void setItemId(int newItemId) {
        this.itemId = newItemId;
        executeUpdate("item_id", newItemId);
    }

    public void setEcommerceId(int newEcommerceId) {
        this.ecommerceId = newEcommerceId;
        executeUpdate("ecom_id", newEcommerceId);
    }

    // --- Database Logic ---

    public void refresh() {
        if (this.eCommerceItemId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "ecommerce_items", "ecom_item_id", this.eCommerceItemId)) {
            if (rs != null && rs.next()) {
                this.itemId = rs.getInt("item_id");
                this.ecommerceId = rs.getInt("ecom_id");
                this.priceOverride = rs.getDouble("price_override");
                this.addedAt = rs.getObject("created_at", LocalDateTime.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean save() {
        if (this.eCommerceItemId != null) return false;

        Map<String, Object> data = new HashMap<>();
        data.put("item_id", this.itemId);
        data.put("ecom_id", this.ecommerceId);
        data.put("price_override", this.priceOverride);
        data.put("created_at", this.addedAt);

        return DBConnection.getInstance().insertIntoTable("ecommerce_items", data);
    }

    /**
     * Replaces getDetails() dict
     */
    public Map<String, Object> toMap() {
        Map<String, Object> details = new HashMap<>();
        details.put("ID", eCommerceItemId);
        details.put("Item_ID", itemId);
        details.put("ECom_ID", ecommerceId);
        details.put("Price_Override", priceOverride);
        return details;
    }

    @Override
    public String toString() {
        return String.format("ECommerceItem[ID=%d, Item=%d, ECom=%d, PriceOverride=%.2f]",
                eCommerceItemId, itemId, ecommerceId, priceOverride);
    }
}