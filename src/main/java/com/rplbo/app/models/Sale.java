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
     * Constructor for loading from Database Map
     * Matches the output of db.selectAll("sales")
     */
    public Sale(Map<String, Object> data) {
        this.saleId = (Integer) data.get("sale_id");
        this.customerId = ((Number) data.get("cust_id")).intValue();
        this.userId = ((Number) data.get("user_id")).intValue();

        // Handle optional ecom_id safely
        Object ecom = data.get("ecom_id");
        this.ecommerceId = (ecom != null) ? ((Number) ecom).intValue() : 0;

        this.totalAmount = ((Number) data.get("total_amount")).doubleValue();
        this.profit = ((Number) data.get("profit")).doubleValue();
        this.logisticsFee = ((Number) data.get("logistics_fee")).doubleValue();

        // MySQL TINYINT(1) maps to Boolean in most drivers
        this.isPaid = (Boolean) data.get("is_paid");
        this.isCancelled = (Boolean) data.get("is_cancelled");
        this.paymentMethod = (String) data.get("payment_method");

        Object createdAt = data.get("created_at");
        this.date = (createdAt instanceof LocalDateTime) ? (LocalDateTime) createdAt : LocalDateTime.now();
    }

    // --- Internal Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.saleId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("sales", "sale_id", this.saleId, updates);
        }
    }
    // --- Setters (Updates DB immediately) ---

    public void setEcommerceId(int ecommerceId) {this.ecommerceId = ecommerceId;executeUpdate("ecom_id", ecommerceId);}
    public void setCustomerId(int customerId) {this.customerId = customerId;executeUpdate("cust_id", customerId);}
    public void setUserId(int userId) {this.userId = userId;executeUpdate("user_id", userId);}
    public void setDate(LocalDateTime date) {this.date = date;executeUpdate("created_at", date);}
    public void setTotalAmount(double totalAmount) {this.totalAmount = totalAmount;executeUpdate("total_amount", totalAmount);}
    public void setPaid(boolean paid) {this.isPaid = paid;executeUpdate("is_paid", paid);}
    public void setCancelled(boolean cancelled) {this.isCancelled = cancelled;executeUpdate("is_cancelled", cancelled);}
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod;executeUpdate("payment_method", paymentMethod);}
    public void setProfit(double profit) { this.profit = profit; executeUpdate("profit", profit); }


    // --- Getters ---

    public Integer getSaleId() { return saleId; }
    public int getCustomerId() { return customerId; }
    public double getTotalAmount() { return totalAmount; }
    public boolean isPaid() { return isPaid; }
    public boolean isCancelled() { return isCancelled; }
    public LocalDateTime getDate() { return date; }
    public double getProfit() { return profit; }
    public int getUserId() { return userId; }


    // --- Database Operations ---

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

    public void refresh() {
        if (this.saleId == null) return;
        Map<String, Object> data = DBConnection.getInstance().fetchRow("sales", "sale_id", this.saleId);
        if (data != null) {
            this.totalAmount = ((Number) data.get("total_amount")).doubleValue();
            this.isPaid = (Boolean) data.get("is_paid");
            this.isCancelled = (Boolean) data.get("is_cancelled");
            this.profit = ((Number) data.get("profit")).doubleValue();
        }
    }

    @Override
    public String toString() {
        return String.format("Sale ID: %d, Customer: %d, Total: %.2f, Paid: %b, Profit: %.2f",
                saleId, customerId, totalAmount, isPaid, profit);
    }

//    public static void main(String[] args) {
//        Sale sale
//    }
}
