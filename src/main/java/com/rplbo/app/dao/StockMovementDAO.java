package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import java.util.HashMap;
import java.util.Map;

public class StockMovementDAO {

    public void logChange(int itemId, int userId, int change, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("item_id", itemId);
        data.put("user_id", userId);
        data.put("quantity_changed", change);
        data.put("reason", reason);

        DBConnection.getInstance().insertIntoTable("stock_movement_logs", data);
        System.out.println("📝 Audit Log Created: " + reason + " (" + change + ")");
    }
}