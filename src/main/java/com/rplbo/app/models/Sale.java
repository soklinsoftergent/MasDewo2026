package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Sale {
    private Integer saleId;
    private int customerId;
    private int userId;
    private int ecommerceId;
    private LocalDateTime date;
    private double totalAmount;
    private boolean isPaid;
    private boolean isCancelled;
    private String paymentMethod;
    private double logisticsFee;
    private double profit;

    /**
     * Constructor for creating a NEW Sale (ID not yet set)
     */
    public Sale(int customerId, int userId, int ecommerceId, double totalAmount) {
        this.customerId = customerId;
        this.userId = userId;
        this.ecommerceId = ecommerceId;
        this.totalAmount = totalAmount;
        this.date = LocalDateTime.now();
        this.isPaid = false;
        this.isCancelled = false;
        this.logisticsFee = 0.0;
        this.profit = 0.0;
    }

    /**
     * Constructor for loading from the Database (Full attributes)
     */
    public Sale(Integer saleId, int customerId, int userId, int ecommerceId,
                LocalDateTime date, double totalAmount, boolean isPaid,
                boolean isCancelled, String paymentMethod, double logisticsFee, double profit) {
        this.saleId = saleId;
        this.customerId = customerId;
        this.userId = userId;
        this.ecommerceId = ecommerceId;
        this.date = date;
        this.totalAmount = totalAmount;
        this.isPaid = isPaid;
        this.isCancelled = isCancelled;
        this.paymentMethod = paymentMethod;
        this.logisticsFee = logisticsFee;
        this.profit = profit;
    }

    // --- Internal Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.saleId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            // Matches 'sales' table and 'sale_id' primary key from our SQL schema
            DBConnection.getInstance().updateField("sales", "sale_id", this.saleId, updates);
        }
    }

    // --- Setters (Updates DB immediately) ---

    public void setEcommerceId(int ecommerceId) {
        this.ecommerceId = ecommerceId;
        executeUpdate("ecom_id", ecommerceId);
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
        executeUpdate("cust_id", customerId);
    }

    public void setUserId(int userId) {
        this.userId = userId;
        executeUpdate("user_id", userId);
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
        executeUpdate("created_at", date);
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
        executeUpdate("total_amount", totalAmount);
    }

    public void setPaid(boolean paid) {
        this.isPaid = paid;
        executeUpdate("is_paid", paid);
    }

    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
        executeUpdate("is_cancelled", cancelled);
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        executeUpdate("payment_method", paymentMethod);
    }

    // --- Getters ---

    public Integer getSaleId() { return saleId; }
    public int getCustomerId() { return customerId; }
    public double getTotalAmount() { return totalAmount; }
    public boolean isPaid() { return isPaid; }
    public boolean isCancelled() { return isCancelled; }
    public LocalDateTime getDate() { return date; }

    // --- Database Operations ---

    /**
     * Replicates your save() logic
     */
    public boolean save() {
        if (this.saleId != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cust_id", this.customerId);
        data.put("user_id", this.userId);
        data.put("ecom_id", this.ecommerceId);
        data.put("total_amount", this.totalAmount);
        data.put("is_paid", this.isPaid);
        data.put("is_cancelled", this.isCancelled);
        data.put("payment_method", this.paymentMethod);
        data.put("logistics_fee", this.logisticsFee);
        data.put("profit", this.profit);
        data.put("created_at", this.date);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("sales", data);
        if (newId != null) {
            this.saleId = newId;
            return true;
        }
        return false;
    }

    /**
     * Replicates your refresh() logic
     */
    public void refresh() {
        if (this.saleId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "sales", "sale_id", this.saleId)) {
            if (rs != null && rs.next()) {
                this.customerId = rs.getInt("cust_id");
                this.userId = rs.getInt("user_id");
                this.ecommerceId = rs.getInt("ecom_id");
                this.totalAmount = rs.getDouble("total_amount");
                this.isPaid = rs.getBoolean("is_paid");
                this.isCancelled = rs.getBoolean("is_cancelled");
                this.paymentMethod = rs.getString("payment_method");
                this.logisticsFee = rs.getDouble("logistics_fee");
                this.profit = rs.getDouble("profit");
                this.date = rs.getObject("created_at", LocalDateTime.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return String.format("Sale ID: %d, Customer: %d, Total: %.2f, Paid: %b, Profit: %.2f",
                saleId, customerId, totalAmount, isPaid, profit);
    }
}
