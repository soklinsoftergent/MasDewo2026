package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Sale extends ActiveRecord {
    private Integer saleId;
    private int customerId;
    private int userId;
    private int ecommerceId;
    private double totalAmount;
    private double profit;
    private double logisticsFee;
    private boolean isPaid;
    private boolean isCancelled;
    private String paymentMethod;
    private LocalDateTime createdAt;

    // --- Constructors ---

    /** Constructor untuk transaksi BARU */
    public Sale(int customerId, int userId, int ecommerceId, double totalAmount) {
        this.customerId = customerId;
        this.userId = userId;
        this.ecommerceId = ecommerceId;
        this.totalAmount = totalAmount;
        this.createdAt = LocalDateTime.now();
        this.isPaid = false;
        this.isCancelled = false;
        this.profit = 0.0;
        this.logisticsFee = 0.0;
    }

    /** Constructor untuk memuat data dari Map (DB) */
    public Sale(Map<String, Object> data) {
        fromMap(data);
    }

    // --- ActiveRecord Implementation ---

    @Override protected String tableName() { return "sales"; }
    @Override protected String primaryKeyColumn() { return "sale_id"; }
    @Override protected Integer getId() { return saleId; }
    @Override protected void setId(Integer id) { this.saleId = id; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cust_id", customerId);
        data.put("user_id", userId);
        data.put("ecom_id", ecommerceId);
        data.put("total_amount", totalAmount);
        data.put("profit", profit);
        data.put("logistics_fee", logisticsFee);
        data.put("is_paid", isPaid);
        data.put("is_cancelled", isCancelled);
        data.put("payment_method", paymentMethod);
        data.put("created_at", createdAt);
        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.saleId = (Integer) data.get("sale_id");

        // Gunakan helper method untuk menghindari NullPointerException
        this.customerId = safeInt(data.get("cust_id"));
        this.userId = safeInt(data.get("user_id"));

        Object ecom = data.get("ecom_id");
        this.ecommerceId = (ecom != null) ? ((Number) ecom).intValue() : 0;

        this.totalAmount = safeDouble(data.get("total_amount"));
        this.profit = safeDouble(data.get("profit"));
        this.logisticsFee = safeDouble(data.get("logistics_fee"));

        // Handle Boolean safely
        this.isPaid = safeBool(data.get("is_paid"));
        this.isCancelled = safeBool(data.get("is_cancelled"));

        this.paymentMethod = (String) data.get("payment_method");

        Object createdAtObj = data.get("created_at");
        this.createdAt = (createdAtObj instanceof LocalDateTime) ?
                (LocalDateTime) createdAtObj : LocalDateTime.now();
    }

// --- Tambahkan Helper Methods di bawah agar kode bersih ---

    private int safeInt(Object obj) {
        if (obj == null) return 0;
        return ((Number) obj).intValue();
    }

    private double safeDouble(Object obj) {
        if (obj == null) return 0.0;
        return ((Number) obj).doubleValue();
    }

    private boolean safeBool(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (Boolean) obj;
        return ((Number) obj).intValue() == 1;
    }

    // --- Setters (Otomatis update ke Database) ---

    public void setTotalAmount(double amount) { this.totalAmount = amount; executeUpdate("total_amount", amount); }
    public void setProfit(double profit) { this.profit = profit; executeUpdate("profit", profit); }
    public void setPaid(boolean paid) { this.isPaid = paid; executeUpdate("is_paid", paid); }
    public void setCancelled(boolean cancelled) { this.isCancelled = cancelled; executeUpdate("is_cancelled", cancelled); }
    public void setPaymentMethod(String method) { this.paymentMethod = method; executeUpdate("payment_method", method); }

    // --- Getters ---

    public Integer getSaleId() { return saleId; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("Sale #%d | Total: %.2f | Profit: %.2f", saleId, totalAmount, profit);
    }
}