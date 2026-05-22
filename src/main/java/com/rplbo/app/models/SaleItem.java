package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SaleItem {
    private Integer saItId; // Primary Key (sa_it_id)
    private int saleId;
    private int itemId;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    /**
     * Constructor for creating a NEW SaleItem
     */
    public SaleItem(int saleId, int itemId, int quantity, double unitPrice, double totalPrice) {
        this.saleId = saleId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    /**
     * Constructor for loading from Database Map
     * Matches the output of db.selectAll("sale_items")
     */
    public SaleItem(Map<String, Object> data) {
        this.saItId = (Integer) data.get("sa_it_id");
        this.saleId = ((Number) data.get("sale_id")).intValue();
        this.itemId = ((Number) data.get("item_id")).intValue();
        this.quantity = ((Number) data.get("quantity")).intValue();
        this.unitPrice = ((Number) data.get("unit_price")).doubleValue();
        this.totalPrice = ((Number) data.get("total_price")).doubleValue();
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.saItId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("sale_items", "sa_it_id", this.saItId, updates);
        }
    }

    // --- Setters (Updates DB immediately) ---

    public void setSaleId(int saleId) {
        this.saleId = saleId;
        executeUpdate("sale_id", saleId);
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
        executeUpdate("item_id", itemId);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        executeUpdate("quantity", quantity);
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        executeUpdate("unit_price", unitPrice);
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        executeUpdate("total_price", totalPrice);
    }

    // --- Getters ---

    public Integer getSaItId() { return saItId; }
    public int getSaleId() { return saleId; }
    public int getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }


    // --- Database Operations ---


    public boolean save() {
        if (this.saItId != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sale_id", this.saleId);
        data.put("item_id", this.itemId);
        data.put("quantity", this.quantity);
        data.put("unit_price", this.unitPrice);
        data.put("total_price", this.totalPrice);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("sale_items", data);
        if (newId != null) {
            this.saItId = newId;
            return true;
        }
        return false;
    }

    public void refresh() {
        if (this.saItId == null) return;

        // Uses the generic fetchRow helper from DBConnection
        Map<String, Object> data = DBConnection.getInstance().fetchRow("sale_items", "sa_it_id", this.saItId);

        if (data != null) {
            this.saleId = ((Number) data.get("sale_id")).intValue();
            this.itemId = ((Number) data.get("item_id")).intValue();
            this.quantity = ((Number) data.get("quantity")).intValue();
            this.unitPrice = ((Number) data.get("unit_price")).doubleValue();
            this.totalPrice = ((Number) data.get("total_price")).doubleValue();
        }
    }

    @Override
    public String toString() {
        return String.format("SaleItem[ID=%d, Sale=%d, Item=%d, Qty=%d]", saItId, saleId, itemId, quantity);
    }
}
