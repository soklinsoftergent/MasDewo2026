package com.rplbo.app;
import java.time.LocalDateTime;       // For Date + Time (Sales, Logs)
import java.time.LocalDate;           // For Date only (Birthdays, Daily Reports)
import java.time.format.DateTimeFormatter; // For formatting (e.g., "17 March 2026")

public class Customer {

    private Integer custId;
    private String custName;
    private String custPhone;
    private String custEmail;
    private String custAddress;
    private LocalDateTime createdAt;

    public Customer(Integer custId, String custName, String custPhone, LocalDateTime createdAt){
        this.custId = custId;
        this.custName = custName;
        this.custPhone = custPhone;
        this.custEmail = "";
        this.custAddress = "";
        this.createdAt = createdAt;
    }

    public Customer(Integer custId, String custName, String custPhone, String custEmail, String custAddress, LocalDateTime createdAt){
        this.custId = custId;
        this.custName = custName;
        this.custPhone = custPhone;
        this.custEmail = custEmail;
        this.custAddress = custAddress;
        this.createdAt = createdAt;
    }

    public Customer(){
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
    }

    public String getCustPhone() {
        return custPhone;
    }

    public void setCustPhone(String custPhone) {
        this.custPhone = custPhone;
    }

    public String getCustEmail() {
        return custEmail;
    }

    public void setCustEmail(String custEmail) {
        this.custEmail = custEmail;
    }

    public String getCustAddress() {
        return custAddress;
    }

    public void setCustAddress(String custAddress) {
        this.custAddress = custAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
