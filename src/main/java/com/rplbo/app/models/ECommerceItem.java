package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ECommerceItem extends ActiveRecord {

    private Integer eCommerceItemId;
    private int itemId;
    private int ecommerceId;
    private Double priceOverride;
    private LocalDateTime addedAt;

    /**
     * Constructor for NEW record
     */
    public ECommerceItem(int itemId, int ecommerceId, Double priceOverride) {
        this.itemId = itemId;
        this.ecommerceId = ecommerceId;
        this.priceOverride = priceOverride;
        this.addedAt = LocalDateTime.now();
    }

    /**
     * Constructor from database row
     */
    public ECommerceItem(Map<String, Object> data) {
        fromMap(data);
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
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("item_id", itemId);
        data.put("ecom_id", ecommerceId);
        data.put("price_override", priceOverride);
        data.put("created_at", addedAt);

        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {

        this.eCommerceItemId = (Integer) data.get("ecom_item_id");

        Object itemObj = data.get("item_id");
        this.itemId = itemObj == null ? 0 : ((Number) itemObj).intValue();

        Object ecomObj = data.get("ecom_id");
        this.ecommerceId = ecomObj == null ? 0 : ((Number) ecomObj).intValue();

        Object priceObj = data.get("price_override");
        this.priceOverride = priceObj == null
                ? null
                : ((Number) priceObj).doubleValue();

        Object createdObj = data.get("created_at");
        this.addedAt = createdObj instanceof LocalDateTime
                ? (LocalDateTime) createdObj
                : LocalDateTime.now();
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