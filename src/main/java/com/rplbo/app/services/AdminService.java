package com.rplbo.app.services;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.*;

public class AdminService {

    /**
     * Requirement: resetUserPassword
     */
    public boolean forceResetPassword(User staff, String newPlainTextPassword) {
        if (!UserSession.getInstance().isAdmin()) return false;

        String newHash = BCrypt.hashpw(newPlainTextPassword, BCrypt.gensalt());
        staff.setUserPasswdHash(newHash); // ActiveRecord updates DB instantly
        return true;
    }

    /**
     * Requirement: viewProfitLossReport
     * Calculation: Total Sales - (Total Purchase Cost of items sold) - Total Expenses
     */
    public Map<String, Double> getFinancialSummary() {
        if (!UserSession.getInstance().isAdmin()) return null;

        DBConnection db = DBConnection.getInstance();
        Map<String, Double> stats = new HashMap<>();

        // 1. Total Revenue
        double revenue = queryDouble("SELECT SUM(total_amount) FROM sales WHERE is_cancelled = 0");

        // 2. Total COGS (Cost of Goods Sold)
        double cogs = queryDouble("SELECT SUM(si.quantity * i.purchase_price) FROM sale_items si JOIN items i ON si.item_id = i.id");

        // 3. Total Overhead Expenses
        double expenses = queryDouble("SELECT SUM(amount) FROM kas_transactions WHERE type = 'EXPENSE'");

        stats.put("Revenue", revenue);
        stats.put("GrossProfit", revenue - cogs);
        stats.put("Expenses", expenses);
        stats.put("NetProfit", (revenue - cogs) - expenses);

        return stats;
    }

    private double queryDouble(String sql) {
        List<Map<String, Object>> res = DBConnection.getInstance().selectAllCustom(sql);
        if (res != null && !res.isEmpty()) {
            Object val = res.get(0).values().iterator().next();
            return (val != null) ? ((Number) val).doubleValue() : 0.0;
        }
        return 0.0;
    }
}