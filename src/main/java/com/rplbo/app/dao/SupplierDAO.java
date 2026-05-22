package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Supplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SupplierDAO {
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        List<Map<String, Object>> data = DBConnection.getInstance().selectAll("suppliers");
        for (Map<String, Object> row : data) {
            suppliers.add(new Supplier(row));
        }
        return suppliers;
    }
}