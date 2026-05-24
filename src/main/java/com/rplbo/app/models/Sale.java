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
        this.customerId = ((Number) data.get("cust_id")).intValue();
        this.userId = ((Number) data.get("user_id")).intValue();

        Object ecom = data.get("ecom_id");
        this.ecommerceId = (ecom != null) ? ((Number) ecom).intValue() : 0;

        this.totalAmount = ((Number) data.get("total_amount")).doubleValue();
        this.profit = ((Number) data.get("profit")).doubleValue();
        this.logisticsFee = ((Number) data.get("logistics_fee")).doubleValue();

        // Handle Boolean/TINYINT dari DB
        this.isPaid = (data.get("is_paid") instanceof Boolean) ?
                (Boolean) data.get("is_paid") : ((Number) data.get("is_paid")).intValue() == 1;
        this.isCancelled = (data.get("is_cancelled") instanceof Boolean) ?
                (Boolean) data.get("is_cancelled") : ((Number) data.get("is_cancelled")).intValue() == 1;

        this.paymentMethod = (String) data.get("payment_method");
        this.createdAt = (data.get("created_at") instanceof LocalDateTime) ?
                (LocalDateTime) data.get("created_at") : LocalDateTime.now();
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