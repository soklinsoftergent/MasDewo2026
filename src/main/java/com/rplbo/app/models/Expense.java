package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Expense extends ActiveRecord {

    private Integer expenseId;
    private int userId;
    private double total;
    private String description;
    private LocalDateTime createdAt;

    /**
     * Constructor for NEW expense
     */
    public Expense(int userId, double total, String description) {
        this.userId = userId;
        this.total = total;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor from database row
     */
    public Expense(Map<String, Object> data) {
        fromMap(data);
    }

    // =====================================================
    // ActiveRecord Implementation
    // =====================================================

    @Override
    protected String tableName() {
        return "expenses";
    }

    @Override
    protected String primaryKeyColumn() {
        return "expense_id";
    }

    @Override
    protected Integer getId() {
        return expenseId;
    }

    @Override
    protected void setId(Integer id) {
        this.expenseId = id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("user_id", userId);
        data.put("total", total);
        data.put("description", description);
        data.put("created_at", createdAt);

        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {

        this.expenseId = (Integer) data.get("expense_id");

        Object userObj = data.get("user_id");
        this.userId = userObj == null
                ? 0
                : ((Number) userObj).intValue();

        Object totalObj = data.get("total");
        this.total = totalObj == null
                ? 0.0
                : ((Number) totalObj).doubleValue();

        this.description = (String) data.get("description");

        LocalDateTime createdObj = safeDateTime(data.get("created_at"));
        this.createdAt = createdObj != null
                ? createdObj
                : LocalDateTime.now();
    }

    // =====================================================
    // Getters
    // =====================================================

    public Integer getExpenseId() {
        return expenseId;
    }

    public int getUserId() {
        return userId;
    }

    public double getTotal() {
        return total;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // =====================================================
    // Setters (Auto DB Update)
    // =====================================================

    public void setUserId(int userId) {
        this.userId = userId;
        executeUpdate("user_id", userId);
    }

    public void setTotal(double total) {
        this.total = total;
        executeUpdate("total", total);
    }

    public void setDescription(String description) {
        this.description = description;
        executeUpdate("description", description);
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        executeUpdate("created_at", createdAt);
    }

    @Override
    public String toString() {
        return String.format(
                "Expense[ID=%d, User=%d, Total=%.2f]",
                expenseId,
                userId,
                total
        );
    }
}