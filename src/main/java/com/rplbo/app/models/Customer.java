package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Customer {
    private Integer custId;
    private String custName;
    private String custPhone;
    private String custEmail;
    private String custAddress;
    private final LocalDateTime createdAt;

    public void setName(String name) { this.custName = name; executeUpdate("name", name); }
    public void setPhoneNumber(String phone) { this.custPhone = phone; executeUpdate("phone_number", phone); }
    public void setEmail(String email) { this.custEmail = email; executeUpdate("email", email); }
    public void setAddress(String address) { this.custAddress = address; executeUpdate("address", address); }
    public String getName() { return custName; }
    public String getPhoneNumber() { return custPhone; }
    public Integer getCustId() { return custId; }
    public void setCustId(Integer custId) { this.custId = custId; }
    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; executeUpdate("name", custName); }
    public String getCustPhone() { return custPhone; }
    public void setCustPhone(String custPhone) { this.custPhone = custPhone; executeUpdate("phone_number", custPhone); }
    public String getCustEmail() { return custEmail; }
    public void setCustEmail(String custEmail) { this.custEmail = custEmail; executeUpdate("email", custEmail); }
    public String getCustAddress() { return custAddress; }
    public void setCustAddress(String custAddress) { this.custAddress = custAddress; executeUpdate("address", custAddress); }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public Customer(String custName, String custPhone, String custEmail, String custAddress) {
        this(null, custName, custPhone, custEmail, custAddress, LocalDateTime.now());
    }

    public Customer(Integer custId, String custName, String custPhone, LocalDateTime createdAt) {
        this(custId, custName, custPhone, "", "", createdAt);
    }

    public Customer(Integer custId, String custName, String custPhone, String custEmail, String custAddress, LocalDateTime createdAt) {
        this.custId = custId;
        this.custName = custName;
        this.custPhone = custPhone;
        this.custEmail = custEmail;
        this.custAddress = custAddress;
        this.createdAt = createdAt;
    }

    public Customer(Map<String, Object> data) {
        this.custId = (Integer) data.get("cust_id");
        this.custName = (String) data.get("name");
        this.custPhone = (String) data.get("phone_number");
        this.custEmail = (String) data.get("email");
        this.custAddress = (String) data.get("address");
        this.createdAt = (data.get("created_at") instanceof LocalDateTime) ?
                (LocalDateTime) data.get("created_at") :
                LocalDateTime.now(); // Handle date conversion if necessary
    }


    public Customer() {
        this.createdAt = LocalDateTime.now();
    }

    private void executeUpdate(String field, Object value) {
        if (getCustId() != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("customers", "cust_id", this.custId, updates);
        }
    }

    public boolean save() {
        if (getCustId() != null) return false;

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("customers", this.toMap());
        if (newId != null) {
            setCustId(newId);
            return true;
        }
        return false;
    }

    public void refresh() {
        if (getCustId() == null) return;

        // Use the new fetchRow helper! No ResultSets here.
        Map<String, Object> data = DBConnection.getInstance().fetchRow("customers", "cust_id", this.getCustId());

        if (data != null) {
            setCustName((String) data.get("name"));
            setCustPhone((String) data.get("phone_number"));
            setCustEmail((String) data.get("email"));
            setCustAddress((String) data.get("address"));
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", getCustName());
        data.put("phone_number", getCustPhone());
        data.put("email", getCustEmail());
        data.put("address", getCustAddress());
        data.put("created_at", getCreatedAt());
        return data;
    }

    @Override
    public String toString() {
        return String.format("Customer[ID=%d, Name=%s, Phone=%s, Email=%s]",
                getCustId(), getCustName(), getCustPhone(), getCustEmail());
    }
}
