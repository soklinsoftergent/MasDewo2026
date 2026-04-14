package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class KasBalance {
    private double balance;

    /**
     * Constructor for KasBalance
     */
    public KasBalance(double initialBalance) {
        this.balance = initialBalance;
    }

    /**
     * Replicates setManualBalance. Updates the DB row where id=1.
     */
    public void setManualBalance(double newBalance) {
        this.balance = newBalance;
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("balance", this.balance);

            // Replicates: self.dbConn.updateField("kas", "id", 1, balance=...)
            boolean success = DBConnection.getInstance().updateField("kas", "id", 1, updates);

            if (!success) {
                throw new RuntimeException("Failed to update balance in database.");
            }
        } catch (Exception e) {
            System.err.println("Error updating balance: " + e.getMessage());
        }
    }

    /**
     * Increments balance and persists to DB
     */
    public void incrementBalance(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive number");
        }
        setManualBalance(this.balance + amount);
    }

    /**
     * Decreases balance and persists to DB (includes Insufficient Funds check)
     */
    public void decreaseBalance(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be a positive number");
        }
        if (amount > this.balance) {
            throw new IllegalStateException("Insufficient funds in Kas!");
        }
        setManualBalance(this.balance - amount);
    }

    /**
     * Fetches the latest balance from the database
     */
    public void refresh() {
        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("balance", "kas", "id", 1)) {
            if (rs != null && rs.next()) {
                this.balance = rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return String.format("Kas Balance:\nCurrent Balance: %,.2f", balance);
    }
}