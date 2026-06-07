package com.rplbo.app.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class KasBalance extends ActiveRecord {
    private double balance;

    // --- Constructors ---

    /** Constructor for brand new Kas entry (Rarely used) */
    public KasBalance(double initialBalance) {
        this.balance = initialBalance;
    }

    /** Constructor for loading from Map */
    public KasBalance(Map<String, Object> data) {
        fromMap(data);
    }

    // --- ActiveRecord Implementation ---

    @Override protected String tableName() { return "kas"; }
    @Override protected String primaryKeyColumn() { return "id"; }
    @Override protected Integer getId() { return 1; } // Always ID 1 for Kas
    @Override protected void setId(Integer id) { /* Fixed ID, do nothing */ }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", 1);
        data.put("balance", balance);
        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        // Safe casting for numeric balance
        this.balance = ((Number) data.get("balance")).doubleValue();
    }

    // --- Business Logic ---

    public void incrementBalance(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        this.balance += amount;
        executeUpdate("balance", this.balance);
    }

    public void decreaseBalance(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (amount > this.balance) throw new IllegalStateException("Insufficient funds in Kas!");
        this.balance -= amount;
        executeUpdate("balance", this.balance);
    }

    // --- Getters & Setters ---

    public double getBalance() { return balance; }

    public void setBalance(double balance) {
        this.balance = balance;
        executeUpdate("balance", balance);
    }

    @Override
    public String toString() {
        return String.format("Current Balance: Rp. %,.2f", balance);
    }
}