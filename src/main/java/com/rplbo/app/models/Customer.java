package com.rplbo.app.models;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Customer extends ActiveRecord {

    private Integer custId;
    private String custName;
    private String custPhone;
    private String custEmail;
    private String custAddress;
    private final LocalDateTime createdAt;

    // =========================
    // Constructors
    // =========================

    public Customer(String custName, String custPhone,
                    String custEmail, String custAddress) {

        this(null, custName, custPhone,
                custEmail, custAddress,
                LocalDateTime.now());
    }

    public Customer(Integer custId,
                    String custName,
                    String custPhone,
                    LocalDateTime createdAt) {

        this(custId, custName, custPhone,
                "", "", createdAt);
    }

    public Customer(Integer custId,
                    String custName,
                    String custPhone,
                    String custEmail,
                    String custAddress,
                    LocalDateTime createdAt) {

        this.custId = custId;
        this.custName = custName;
        this.custPhone = custPhone;
        this.custEmail = custEmail;
        this.custAddress = custAddress;
        this.createdAt = createdAt;
    }

    public Customer(Map<String, Object> data) {
        this.createdAt = LocalDateTime.now();
        fromMap(data);
    }

    public Customer() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // ActiveRecord Implementation
    // =========================

    @Override
    protected String tableName() {
        return "customers";
    }

    @Override
    protected String primaryKeyColumn() {
        return "cust_id";
    }

    @Override
    protected Integer getId() {
        return custId;
    }

    @Override
    protected void setId(Integer id) {
        this.custId = id;
    }

    @Override
    public void fromMap(Map<String, Object> data) {

        this.custId = (Integer) data.get("cust_id");
        this.custName = (String) data.get("name");
        this.custPhone = (String) data.get("phone_number");
        this.custEmail = (String) data.get("email");
        this.custAddress = (String) data.get("address");

        // createdAt is final, so only set when constructor is called
    }

    @Override
    public Map<String, Object> toMap() {

        Map<String, Object> data = new LinkedHashMap<>();

        data.put("name", custName);
        data.put("phone_number", custPhone);
        data.put("email", custEmail);
        data.put("address", custAddress);
        data.put("created_at", createdAt);

        return data;
    }

    // =========================
    // ActiveRecord Setters
    // =========================

    public void setName(String name) {
        this.custName = name;
        executeUpdate("name", name);
    }

    public void setPhoneNumber(String phone) {
        this.custPhone = phone;
        executeUpdate("phone_number", phone);
    }

    public void setEmail(String email) {
        this.custEmail = email;
        executeUpdate("email", email);
    }

    public void setAddress(String address) {
        this.custAddress = address;
        executeUpdate("address", address);
    }

    // =========================
    // Getters
    // =========================

    public Integer getCustId() {
        return custId;
    }

    public String getName() {
        return custName;
    }

    public String getPhoneNumber() {
        return custPhone;
    }

    public String getCustName() {
        return custName;
    }

    public String getCustPhone() {
        return custPhone;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public String getCustAddress() {
        return custAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // =========================
    // Convenience setters
    // =========================

    public void setCustId(Integer custId) {
        this.custId = custId;
    }

    public void setCustName(String custName) {
        setName(custName);
    }

    public void setCustPhone(String custPhone) {
        setPhoneNumber(custPhone);
    }

    public void setCustEmail(String custEmail) {
        setEmail(custEmail);
    }

    public void setCustAddress(String custAddress) {
        setAddress(custAddress);
    }

    @Override
    public String toString() {
        return String.format(
                "Customer[ID=%d, Name=%s, Phone=%s, Email=%s]",
                custId,
                custName,
                custPhone,
                custEmail
        );
    }
}