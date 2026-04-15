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
    private LocalDateTime createdAt;

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

    public Customer() {
        this.createdAt = LocalDateTime.now();
    }

    private void executeUpdate(String field, Object value) {
        if (this.custId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("customers", "cust_id", this.custId, updates);
        }
    }

    public boolean save() {
        if (this.custId != null) {
            return false;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", this.custName);
        data.put("phone_number", this.custPhone);
        data.put("email", this.custEmail);
        data.put("address", this.custAddress);
        data.put("created_at", this.createdAt);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("customers", data);
        if (newId != null) {
            this.custId = newId;
            return true;
        }
        return false;
    }

    public void refresh() {
        if (this.custId == null) {
            return;
        }

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "customers", "cust_id", this.custId)) {
            if (rs != null && rs.next()) {
                this.custName = rs.getString("name");
                this.custPhone = rs.getString("phone_number");
                this.custEmail = rs.getString("email");
                this.custAddress = rs.getString("address");
                this.createdAt = rs.getObject("created_at", LocalDateTime.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Integer getCustId() {
        return custId;
    }

    public void setCustId(Integer custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
        executeUpdate("name", custName);
    }

    public String getCustPhone() {
        return custPhone;
    }

    public void setCustPhone(String custPhone) {
        this.custPhone = custPhone;
        executeUpdate("phone_number", custPhone);
    }

    public String getCustEmail() {
        return custEmail;
    }

    public void setCustEmail(String custEmail) {
        this.custEmail = custEmail;
        executeUpdate("email", custEmail);
    }

    public String getCustAddress() {
        return custAddress;
    }

    public void setCustAddress(String custAddress) {
        this.custAddress = custAddress;
        executeUpdate("address", custAddress);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cust_id", custId);
        data.put("name", custName);
        data.put("phone_number", custPhone);
        data.put("email", custEmail);
        data.put("address", custAddress);
        data.put("created_at", createdAt);
        return data;
    }

    @Override
    public String toString() {
        return String.format("Customer[ID=%d, Name=%s, Phone=%s, Email=%s]",
                custId, custName, custPhone, custEmail);
    }
}
