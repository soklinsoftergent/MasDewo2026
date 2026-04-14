package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class KasTransaction {
    private Integer kasTransId;
    private LocalDateTime transactionDate;
    private String type; // "INCOME" or "EXPENSE"
    private String description;
    private double amount;
    private Integer userId; // Track which employee made the entry

    /**
     * Constructor for creating a NEW transaction
     */
    public KasTransaction(String type, String description, double amount, Integer userId) {
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.userId = userId;
        this.transactionDate = LocalDateTime.now();
    }

    /**
     * Constructor for loading from the Database
     */
    public KasTransaction(Integer id, LocalDateTime date, String type, String description, double amount, Integer userId) {
        this.kasTransId = id;
        this.transactionDate = date;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.userId = userId;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.kasTransId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            // Matches 'kas_transactions' table and 'kas_trans_id' from SQL schema
            DBConnection.getInstance().updateField("kas_transactions", "kas_trans_id", this.kasTransId, updates);
        }
    }

    // --- Setters (Updates DB immediately) ---

    public void setDescription(String description) {
        this.description = description;
        executeUpdate("description", description);
    }

    public void setAmount(double amount) {
        this.amount = amount;
        executeUpdate("amount", amount);
    }

    public void setType(String type) {
        this.type = type;
        executeUpdate("type", type);
    }

    public void setTransactionDate(LocalDateTime date) {
        this.transactionDate = date;
        executeUpdate("transaction_date", date);
    }

    // --- Getters ---

    public Integer getKasTransId() { return kasTransId; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }

    // --- Database Operations ---

    public boolean save() {
        if (this.kasTransId != null) return false;

        Map<String, Object> data = new HashMap<>();
        data.put("type", this.type);
        data.put("description", this.description);
        data.put("amount", this.amount);
        data.put("transaction_date", this.transactionDate);
        data.put("user_id", this.userId);

        return DBConnection.getInstance().insertIntoTable("kas_transactions", data);
    }

    public void refresh() {
        if (this.kasTransId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "kas_transactions", "kas_trans_id", this.kasTransId)) {
            if (rs != null && rs.next()) {
                this.type = rs.getString("type");
                this.description = rs.getString("description");
                this.amount = rs.getDouble("amount");
                this.transactionDate = rs.getObject("transaction_date", LocalDateTime.class);
                this.userId = rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return String.format("KasTransaction[ID: %d, Date: %s, Type: %s, Amount: %.2f, Desc: %s]",
                kasTransId,
                transactionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                type, amount, description != null ? description : "N/A");
    }
}