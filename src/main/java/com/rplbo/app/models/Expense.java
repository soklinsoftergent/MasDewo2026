package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Expense {
    private Integer expenseId;
    private int userId;
    private double total;
    private String description;
    private LocalDateTime createdAt;

    /**
     * Constructor for creating a NEW Expense
     */
    public Expense(int userId, double total, String description) {
        this.userId = userId;
        this.total = total;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor for loading from the Database
     */
    public Expense(Integer id, int userId, double total, String description, LocalDateTime createdAt) {
        this.expenseId = id;
        this.userId = userId;
        this.total = total;
        this.description = description;
        this.createdAt = createdAt;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.expenseId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            // Uses 'expense_id' from our SQL schema
            DBConnection.getInstance().updateField("expenses", "expense_id", this.expenseId, updates);
        }
    }

    // --- Getters ---

    public Integer getExpenseId() { return expenseId; }
    public int getUserId() { return userId; }
    public double getTotal() { return total; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters (Updates DB immediately) ---

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

    // --- Persistence & Logic ---

    public boolean save() {
        if (this.expenseId != null) {
            throw new IllegalStateException("Expense already saved with ID: " + this.expenseId);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user_id", this.userId);
        data.put("total", this.total);
        data.put("description", this.description);
        data.put("created_at", this.createdAt);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("expenses", data);
        if (newId != null) {
            this.expenseId = newId;
            return true;
        }
        return false;
    }

    public void refresh() {
        if (this.expenseId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "expenses", "expense_id", this.expenseId)) {
            if (rs != null && rs.next()) {
                this.userId = rs.getInt("user_id");
                this.total = rs.getDouble("total");
                this.description = rs.getString("description");
                this.createdAt = rs.getObject("created_at", LocalDateTime.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replicates your to_dict() method
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", expenseId);
        map.put("user_id", userId);
        map.put("total", total);
        map.put("description", description);
        map.put("created_at", createdAt);
        return map;
    }

    @Override
    public String toString() {
        return String.format("Expense[ID=%d, User=%d, Total=%.2f, Desc=%s]",
                expenseId, userId, total, description);
    }
}
