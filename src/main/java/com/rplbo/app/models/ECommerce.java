package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ECommerce {
    private Integer ecomId; // Nullable for unsaved platforms
    private String ecomName;
    private double ecomPlatformFee;
    private String platformURL;

    /**
     * Constructor for creating a NEW ECommerce platform
     */
    public ECommerce(String ecomName, double ecomPlatformFee, String platformURL) {
        this.ecomName = ecomName;
        this.ecomPlatformFee = ecomPlatformFee;
        this.platformURL = platformURL;
    }

    /**
     * Constructor for loading from Database Map
     * Matches the output of db.selectAll() or db.fetchRow()
     */
    public ECommerce(Map<String, Object> data) {
        this.ecomId = (Integer) data.get("ecom_id");
        this.ecomName = (String) data.get("ecom_name");
        // SQL Decimals come back as Number/Double/Float; cast safely
        this.ecomPlatformFee = ((Number) data.get("ecom_platform_fee")).doubleValue();
        this.platformURL = (String) data.get("platform_url");
    }
    
    // --- Active Record Logic ---
    public boolean save() {
        if (this.ecomId != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ecom_name", this.ecomName);
        data.put("ecom_platform_fee", this.ecomPlatformFee);
        data.put("platform_url", this.platformURL);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("ecommerces", data);
        if (newId != null) {
            this.ecomId = newId;
            return true;
        }
        return false;
    }

    // --- Setters (Triggers DB Update) ---

    public void setEcomName(String newEcomName) {
        this.ecomName = newEcomName;
        executeUpdate("ecom_name", newEcomName);
    }

    public void setEcomPlatformFee(double newPlatformFee) {
        this.ecomPlatformFee = newPlatformFee;
        executeUpdate("ecom_platform_fee", newPlatformFee);
    }

    public void setPlatformURL(String platformURL) {
        this.platformURL = platformURL;
        executeUpdate("platform_url", platformURL);
    }

    // --- Getters ---

    public Integer getEcomId() { return ecomId; }
    public String getEcomName() { return ecomName; }
    public double getEcomPlatformFee() { return ecomPlatformFee; }
    public String getPlatformURL() { return platformURL; }

    // --- Logic Methods ---

    public void refresh() {
        if (this.ecomId == null) return;

        // Uses the generic fetchRow helper (as discussed in Customer refactor)
        Map<String, Object> data = DBConnection.getInstance().fetchRow("ecommerces", "ecom_id", this.ecomId);

        if (data != null) {
            this.ecomName = (String) data.get("ecom_name");
            this.ecomPlatformFee = ((Number) data.get("ecom_platform_fee")).doubleValue();
            this.platformURL = (String) data.get("platform_url");
        }
    }

    private void executeUpdate(String field, Object value) {
        if (this.ecomId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("ecommerces", "ecom_id", this.ecomId, updates);
        }
    }

    /**
     * Replicates your getDetails() dict
     */
    public Map<String, Object> getDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("ID", ecomId);
        details.put("Name", ecomName);
        details.put("Platform Fee", ecomPlatformFee);
        details.put("URL", platformURL);
        return details;
    }

    @Override
    public String toString() {
        return "ECommerce{" +
                "ID=" + ecomId +
                ", Name='" + ecomName + '\'' +
                ", Fee=" + ecomPlatformFee + "%" +
                '}';
    }
}
