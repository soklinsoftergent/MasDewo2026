package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ECommerceItem {
    private Integer eCommerceItemId; // Primary Key (ecom_item_id)
    private int itemId;              // Foreign Key to items
    private int ecommerceId;        // Foreign Key to ecommerces
    private Double priceOverride;    // Nullable if using base price
    private LocalDateTime addedAt;

    /**
     * Contructor for a new link not in database
     * @param itemId
     * @param ecommerceId
     * @param priceOverride
     */
    public ECommerceItem(int itemId, int ecommerceId, Double priceOverride) {
        this.itemId = itemId;
        this.ecommerceId = ecommerceId;
        this.priceOverride = priceOverride;
        this.addedAt = LocalDateTime.now();
    }

    /**
     * Constructor for loading from Database Map
     * Matches the output of db.selectAll() or db.fetchRow()
     */
    public ECommerceItem(Map<String, Object> data) {
        this.eCommerceItemId = (Integer) data.get("ecom_item_id");
        this.itemId = (Integer) data.get("item_id");
        this.ecommerceId = (Integer) data.get("ecom_id");

        // Safely handle nullable price override and potential type mismatches
        Object price = data.get("price_override");
        this.priceOverride = (price != null) ? ((Number) price).doubleValue() : null;

        this.addedAt = (data.get("created_at") instanceof LocalDateTime) ?
                (LocalDateTime) data.get("created_at") : LocalDateTime.now();
    }

    // --- Internal Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.eCommerceItemId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
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

        // Use the generic fetchRow helper! No more manual ResultSets in models.
        Map<String, Object> data = DBConnection.getInstance().fetchRow("ecommerce_items", "ecom_item_id", this.eCommerceItemId);

        if (data != null) {
            this.itemId = (Integer) data.get("item_id");
            this.ecommerceId = (Integer) data.get("ecom_id");
            Object price = data.get("price_override");
            this.priceOverride = (price != null) ? ((Number) price).doubleValue() : null;
        }
    }

    public boolean save() {
        if (this.eCommerceItemId != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("item_id", this.itemId);
        data.put("ecom_id", this.ecommerceId);
        data.put("price_override", this.priceOverride);
        data.put("created_at", this.addedAt);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("ecommerce_items", data);
        if (newId != null) {
            this.eCommerceItemId = newId;
            return true;
        }
        return false;
    }

    /**
     * Replaces getDetails() dict
     */
    public Map<String, Object> toMap() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("ID", eCommerceItemId);
        details.put("Item_ID", itemId);
        details.put("ECom_ID", ecommerceId);
        details.put("Price_Override", priceOverride);
        return details;
    }

    @Override
    public String toString() {
        return String.format("ECommerceItem[ID=%d, ItemID=%d, PlatformID=%d]", eCommerceItemId, itemId, ecommerceId);
    }
}
