package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class StockMovementLog extends ActiveRecord {
    private Integer logId;
    private int itemId;
    private int userId;
    private int quantityChanged;
    private String reason;
    private LocalDateTime createdAt;

    public StockMovementLog(Map<String, Object> data) { fromMap(data); }

    @Override protected String tableName() { return "stock_movement_logs"; }
    @Override protected String primaryKeyColumn() { return "log_id"; }
    @Override protected Integer getId() { return logId; }
    @Override protected void setId(Integer id) { this.logId = id; }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.logId = (Integer) data.get("log_id");
        this.itemId = (Integer) data.get("item_id");
        this.userId = (Integer) data.get("user_id");
        this.quantityChanged = ((Number) data.get("quantity_changed")).intValue();
        this.reason = (String) data.get("reason");
        this.createdAt = (LocalDateTime) data.get("created_at");
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("item_id", itemId);
        data.put("user_id", userId);
        data.put("quantity_changed", quantityChanged);
        data.put("reason", reason);
        return data;
    }

    // Getters for TableView...
    public String getReason() { return reason; }
    public int getChange() { return quantityChanged; }
    public LocalDateTime getTimestamp() { return createdAt; }
}