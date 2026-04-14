package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
     * Constructor for loading an existing SaleItem from the Database
     */
    public SaleItem(Integer id, int saleId, int itemId, int quantity, double unitPrice, double totalPrice) {
        this.saItId = id;
        this.saleId = saleId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.saItId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            // Matches 'sale_items' table and 'sa_it_id' primary key from SQL schema
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

        Map<String, Object> data = new HashMap<>();
        data.put("sale_id", this.saleId);
        data.put("item_id", this.itemId);
        data.put("quantity", this.quantity);
        data.put("unit_price", this.unitPrice);
        data.put("total_price", this.totalPrice);

        boolean success = DBConnection.getInstance().insertIntoTable("sale_items", data);

        if (success) {
            try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("LAST_INSERT_ID()", "sale_items", "1", 1)) {
                if (rs != null && rs.next()) {
                    this.saItId = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public void refresh() {
        if (this.saItId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "sale_items", "sa_it_id", this.saItId)) {
            if (rs != null && rs.next()) {
                this.saleId = rs.getInt("sale_id");
                this.itemId = rs.getInt("item_id");
                this.quantity = rs.getInt("quantity");
                this.unitPrice = rs.getDouble("unit_price");
                this.totalPrice = rs.getDouble("total_price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return String.format("SaleItem[ID=%d, SaleID=%d, ItemID=%d, Qty=%d, Total=%.2f]",
                saItId, saleId, itemId, quantity, totalPrice);
    }
}