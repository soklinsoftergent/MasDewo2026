package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseItem {
    private Integer exItId;      // Primary Key (ex_it_id)
    private int expenseId;       // Foreign Key to expenses
    private int itemId;          // Foreign Key to items
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    /**
     * Constructor for creating a NEW ExpenseItem
     */
    public ExpenseItem(int expenseId, int itemId, int quantity, double unitPrice, double totalPrice) {
        this.expenseId = expenseId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    /**
     * Constructor for loading from the Database
     */
    public ExpenseItem(Integer id, int expenseId, int itemId, int quantity, double unitPrice, double totalPrice) {
        this.exItId = id;
        this.expenseId = expenseId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.exItId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            // Uses 'ex_it_id' from our SQL schema
            DBConnection.getInstance().updateField("expense_items", "ex_it_id", this.exItId, updates);
        }
    }

    // --- Getters ---

    public Integer getExItId() { return exItId; }
    public int getExpenseId() { return expenseId; }
    public int getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }

    // --- Setters (Updates DB immediately + Includes your Python validation) ---

    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity = quantity;
        executeUpdate("quantity", quantity);
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice <= 0) throw new IllegalArgumentException("Unit Price must be positive");
        this.unitPrice = unitPrice;
        executeUpdate("unit_price", unitPrice);
    }

    public void setTotalPrice(double totalPrice) {
        if (totalPrice <= 0) throw new IllegalArgumentException("Total Price must be positive");
        this.totalPrice = totalPrice;
        executeUpdate("total_price", totalPrice);
    }

    // --- Persistence & Logic ---

    public boolean save() {
        if (this.exItId != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("expense_id", this.expenseId);
        data.put("item_id", this.itemId);
        data.put("quantity", this.quantity);
        data.put("unit_price", this.unitPrice);
        data.put("total_price", this.totalPrice);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("expense_items", data);
        if (newId != null) {
            this.exItId = newId;
            return true;
        }
        return false;
    }

    public void refresh() {
        if (this.exItId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "expense_items", "ex_it_id", this.exItId)) {
            if (rs != null && rs.next()) {
                this.expenseId = rs.getInt("expense_id");
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
        return String.format("ExpenseItem[ID=%d, ExpenseID=%d, ItemID=%d, Qty=%d, Total=%.2f]",
                exItId, expenseId, itemId, quantity, totalPrice);
    }
}
