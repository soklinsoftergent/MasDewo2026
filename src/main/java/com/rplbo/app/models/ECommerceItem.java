package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ECommerceItem extends ActiveRecord {

    private Integer eCommerceItemId;
    private int itemId;
    private int ecommerceId;
    private Double priceOverride;
    private String externalListingId;
    private int syncedStock;
    private String syncStatus;
    private LocalDateTime lastSync;
    private LocalDateTime addedAt;

    /**
     * Constructor from database row
     */
    public ECommerceItem(Map<String, Object> data) {
        fromMap(data);
    }

    /**
     * Constructor for NEW record
     */
    public ECommerceItem(int itemId, int ecommerceId, Double priceOverride) {
        this.itemId = itemId;
        this.ecommerceId = ecommerceId;
        this.priceOverride = priceOverride;
        this.addedAt = LocalDateTime.now();
    }

    // =====================================================
    // ActiveRecord Implementation
    // =====================================================

    @Override
    protected String tableName() {
        return "ecommerce_items";
    }

    @Override
    protected String primaryKeyColumn() {
        return "ecom_item_id";
    }

    @Override
    protected Integer getId() {
        return eCommerceItemId;
    }

    @Override
    protected void setId(Integer id) {
        this.eCommerceItemId = id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("item_id", itemId);
        map.put("ecom_id", ecommerceId);
        map.put("price_override", priceOverride);
        map.put("created_at", addedAt);
        map.put("external_listing_id", externalListingId);
        map.put("synced_stock", syncedStock);
        map.put("sync_status", syncStatus);
        map.put("last_sync", lastSync);
        return map;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.eCommerceItemId = (Integer) data.get("ecom_item_id");
        this.itemId = (Integer) data.get("item_id");
        this.ecommerceId = (Integer) data.get("ecom_id");
        this.priceOverride = data.get("price_override") != null ? ((Number) data.get("price_override")).doubleValue() : null;
        this.addedAt = (LocalDateTime) data.get("created_at");
        this.externalListingId = (String) data.get("external_listing_id");
        this.syncedStock = ((Number) data.get("synced_stock")).intValue();
        this.syncStatus = (String) data.get("sync_status");
        this.lastSync = (LocalDateTime) data.get("last_sync");
    }

    // =====================================================
    // Getters
    // =====================================================

    public Integer getECommerceItemId() {
        return eCommerceItemId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getEcommerceId() {
        return ecommerceId;
    }

    public Double getPriceOverride() {
        return priceOverride;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public String getExternalListingId() { return externalListingId; }

    // =====================================================
    // Setters (Auto DB Update)
    // =====================================================

    public void setItemId(int itemId) {
        this.itemId = itemId;
        executeUpdate("item_id", itemId);
    }

    public void setEcommerceId(int ecommerceId) {
        this.ecommerceId = ecommerceId;
        executeUpdate("ecom_id", ecommerceId);
    }

    public void setPriceOverride(Double priceOverride) {
        this.priceOverride = priceOverride;
        executeUpdate("price_override", priceOverride);
    }

    // Setters for Sync logic
    public void markSynced(int stock) {
        this.syncedStock = stock;
        this.syncStatus = "SYNCED";
        this.lastSync = LocalDateTime.now();
        executeUpdate("synced_stock", syncedStock);
        executeUpdate("sync_status", syncStatus);
        executeUpdate("last_sync", lastSync);
    }

    // =====================================================
    // Utility
    // =====================================================

    public Map<String, Object> getDetails() {
        Map<String, Object> details = new LinkedHashMap<>();

        details.put("ID", eCommerceItemId);
        details.put("Item_ID", itemId);
        details.put("ECom_ID", ecommerceId);
        details.put("Price_Override", priceOverride);

        return details;
    }

    @Override
    public String toString() {
        return String.format(
                "ECommerceItem[ID=%d, ItemID=%d, PlatformID=%d]",
                eCommerceItemId,
                itemId,
                ecommerceId
        );
    }
}