package com.rplbo.app.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class SaleItem extends ActiveRecord {
    private Integer saItId;
    private int saleId;
    private int itemId;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    // --- Constructors ---

    /** Constructor untuk item baru (sebelum simpan ke DB) */
    public SaleItem(int saleId, int itemId, int quantity, double unitPrice, double totalPrice) {
        this.saleId = saleId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    /** Constructor untuk memuat data dari Map (DB) */
    public SaleItem(Map<String, Object> data) {
        fromMap(data);
    }

    // --- ActiveRecord Implementation ---

    @Override protected String tableName() { return "sale_items"; }
    @Override protected String primaryKeyColumn() { return "sa_it_id"; }
    @Override protected Integer getId() { return saItId; }
    @Override protected void setId(Integer id) { this.saItId = id; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sale_id", saleId);
        data.put("item_id", itemId);
        data.put("quantity", quantity);
        data.put("unit_price", unitPrice);
        data.put("total_price", totalPrice);
        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.saItId = (Integer) data.get("sa_it_id");
        this.saleId = ((Number) data.get("sale_id")).intValue();
        this.itemId = ((Number) data.get("item_id")).intValue();
        this.quantity = ((Number) data.get("quantity")).intValue();
        this.unitPrice = ((Number) data.get("unit_price")).doubleValue();
        this.totalPrice = ((Number) data.get("total_price")).doubleValue();
    }

    // --- Setters (Otomatis update ke Database) ---

    public void setSaleId(int saleId) { this.saleId = saleId; executeUpdate("sale_id", saleId); }
    public void setItemId(int itemId) { this.itemId = itemId; executeUpdate("item_id", itemId); }
    public void setQuantity(int quantity) { this.quantity = quantity; executeUpdate("quantity", quantity); }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; executeUpdate("unit_price", unitPrice); }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; executeUpdate("total_price", totalPrice); }

    // --- Getters ---

    public Integer getSaItId() { return saItId; }
    public int getSaleId() { return saleId; }
    public int getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }

    @Override
    public String toString() {
        return String.format("SaleItem[ID=%d, Sale=%d, Item=%d, Qty=%d]", saItId, saleId, itemId, quantity);
    }
}