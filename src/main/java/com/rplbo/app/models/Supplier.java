package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Supplier extends ActiveRecord {
    private Integer supplierId;
    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private LocalDateTime createdAt;

    public Supplier(String name, String phoneNumber, String email, String address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.createdAt = LocalDateTime.now();
    }

    public Supplier(Map<String, Object> map) { fromMap(map); }

    public Integer getSupplierId() { return supplierId; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; executeUpdate("name", getName()); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; executeUpdate("phone_number", getPhoneNumber()); }
    public void setEmail(String email) { this.email = email; executeUpdate("email", getEmail()); }
    public void setAddress(String address) { this.address = address; executeUpdate("address", getAddress()); }

    // implementasi ActiveRecord
    @Override protected String tableName() {return "suppliers";}
    @Override protected String primaryKeyColumn() {return "supplier_id";}
    @Override protected Integer getId() { return supplierId; }
    @Override protected void setId(Integer id) { this.supplierId = id; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("phone_number", phoneNumber);
        map.put("email", email);
        map.put("address", address);
        return map;
    }

    @Override
    public void fromMap(Map<String, Object> map) {
        this.supplierId = (Integer) map.get("supplier_id");
        this.name = (String) map.get("name");
        this.phoneNumber = (String) map.get("phone_number");
        this.email = (String) map.get("email");
        this.address = (String) map.get("address");
        this.createdAt = safeDateTime(map.get("created_at"));
    }

    @Override
    public String toString() {
        return this.name; // This is what shows up in the ComboBox
    }
}
