package com.rplbo.app.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExpenseItem extends ActiveRecord {

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
     * Constructor for loading from Database Map
     */
    public ExpenseItem(Map<String, Object> data) {
        fromMap(data);
    }

    // ==================================================
    // ActiveRecord Implementation
    // ==================================================

    @Override
    protected String tableName() {
        return "expense_items";
    }

    @Override
    protected String primaryKeyColumn() {
        return "ex_it_id";
    }

    @Override
    protected Integer getId() {
        return exItId;
    }

    @Override
    protected void setId(Integer id) {
        this.exItId = id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("expense_id", expenseId);
        data.put("item_id", itemId);
        data.put("quantity", quantity);
        data.put("unit_price", unitPrice);
        data.put("total_price", totalPrice);

        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.exItId = (Integer) data.get("ex_it_id");
        this.expenseId = ((Number) data.get("expense_id")).intValue();
        this.itemId = ((Number) data.get("item_id")).intValue();
        this.quantity = ((Number) data.get("quantity")).intValue();
        this.unitPrice = ((Number) data.get("unit_price")).doubleValue();
        this.totalPrice = ((Number) data.get("total_price")).doubleValue();
    }

    // ==================================================
    // Getters
    // ==================================================

    public Integer getExItId() {
        return exItId;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    // ==================================================
    // Setters (Auto-update DB)
    // ==================================================

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
        executeUpdate("expense_id", expenseId);
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
        executeUpdate("item_id", itemId);
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive");

        this.quantity = quantity;
        executeUpdate("quantity", quantity);
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice <= 0)
            throw new IllegalArgumentException("Unit Price must be positive");

        this.unitPrice = unitPrice;
        executeUpdate("unit_price", unitPrice);
    }

    public void setTotalPrice(double totalPrice) {
        if (totalPrice <= 0)
            throw new IllegalArgumentException("Total Price must be positive");

        this.totalPrice = totalPrice;
        executeUpdate("total_price", totalPrice);
    }

    // ==================================================
    // Utility
    // ==================================================

    @Override
    public String toString() {
        return String.format(
                "ExpenseItem[ID=%d, ExpenseID=%d, ItemID=%d, Qty=%d, Total=%.2f]",
                exItId,
                expenseId,
                itemId,
                quantity,
                totalPrice
        );
    }
}