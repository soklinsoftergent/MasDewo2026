package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class KasTransaction extends ActiveRecord {
    private Integer kasTransId;
    private Integer userId;
    private String type; // "INCOME" or "EXPENSE"
    private double amount;
    private String description;
    private LocalDateTime transactionDate;

    // --- Constructors ---

    /** Constructor for creating a NEW transaction from the UI */
    public KasTransaction(Integer userId, String type, double amount, String description) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
    }

    /** Constructor for loading from Map (DAO Lead logic) */
    public KasTransaction(Map<String, Object> data) {
        fromMap(data);
    }

    // --- ActiveRecord Implementation ---

    @Override protected String tableName() { return "kas_transactions"; }
    @Override protected String primaryKeyColumn() { return "kas_trans_id"; }
    @Override protected Integer getId() { return kasTransId; }
    @Override protected void setId(Integer id) { this.kasTransId = id; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("user_id", userId);
        data.put("type", type);
        data.put("amount", amount);
        data.put("description", description);
        data.put("transaction_date", transactionDate);
        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.kasTransId = (Integer) data.get("kas_trans_id");
        this.userId = (Integer) data.get("user_id");
        this.type = (String) data.get("type");
        this.amount = ((Number) data.get("amount")).doubleValue();
        this.description = (String) data.get("description");

        Object dateObj = safeDateTime(data.get("transaction_date"));
        this.transactionDate = (dateObj instanceof LocalDateTime) ?
                (LocalDateTime) dateObj : LocalDateTime.now();
    }

    // --- Setters (Updates DB immediately) ---

    public void setAmount(double amount) {
        this.amount = amount;
        executeUpdate("amount", amount);
    }

    public void setDescription(String description) {
        this.description = description;
        executeUpdate("description", description);
    }

    // --- Getters ---

    public Integer getKasTransId() { return kasTransId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public Integer getUserId() { return userId; }

    @Override
    public String toString() {
        return String.format("[%s] %s: Rp. %,.2f", type, description, amount);
    }
}