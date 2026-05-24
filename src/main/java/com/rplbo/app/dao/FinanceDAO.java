package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.KasTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FinanceDAO {
    private final DBConnection db = DBConnection.getInstance();

    public double getKasBalance() {
        Object res = db.fetchOneByKeyColumn("balance", "kas", "id", 1);
        return res != null ? ((Number) res).doubleValue() : 0.0;
    }

    public List<Map<String, Object>> getRecentTransactions() {
        return db.selectAll("kas_transactions");
    }

    public List<KasTransaction> getHistory() {
        List<KasTransaction> history = new ArrayList<>();
        for (Map<String, Object> row : db.selectAll("kas_transactions")) {
            history.add(new KasTransaction(row)); // Constructor handles everything
        }
        return history;
    }
}