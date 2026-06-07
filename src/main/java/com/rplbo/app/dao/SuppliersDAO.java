package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuppliersDAO {
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        List<Map<String, Object>> data = DBConnection.getInstance().selectAll("suppliers");
        for (Map<String, Object> row : data) {
            suppliers.add(new Supplier(row));
        }
        return suppliers;
    }

    public boolean deleteSupplier(int id) {
        // Uses your generic DBConnection helper
        return DBConnection.getInstance().deleteByKeyColumn("suppliers", "supplier_id", id);
    }
}