package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
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
     * Constructor for loading from Database
     */
    public ECommerce(Integer ecomId, String ecomName, double ecomPlatformFee, String platformURL) {
        this.ecomId = ecomId;
        this.ecomName = ecomName;
        this.ecomPlatformFee = ecomPlatformFee;
        this.platformURL = platformURL;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.ecomId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(field, value);
            // Replicates: self.getDbConn.updateField("ecommerces", "id", self.getEcomId, ...)
            DBConnection.getInstance().updateField("ecommerces", "ecom_id", this.ecomId, updates);
        }
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

    /**
     * Replicates your refresh() logic
     */
    public void refresh() {
        if (this.ecomId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "ecommerces", "ecom_id", this.ecomId)) {
            if (rs != null && rs.next()) {
                this.ecomName = rs.getString("ecom_name");
                this.ecomPlatformFee = rs.getDouble("ecom_platform_fee");
                this.platformURL = rs.getString("platform_url");
            }
        } catch (SQLException e) {
            System.err.println("Error refreshing ECommerce: " + e.getMessage());
        }
    }

    /**
     * Replicates your getDetails() dict
     */
    public Map<String, Object> getDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("ID", ecomId);
        details.put("Name", ecomName);
        details.put("Platform Fee", ecomPlatformFee);
        details.put("URL", platformURL);
        return details;
    }

    /**
     * Saves a new ECommerce record to the DB and handles result
     */
    public boolean save() {
        if (this.ecomId != null) return false;

        Map<String, Object> data = new HashMap<>();
        data.put("ecom_name", ecomName);
        data.put("ecom_platform_fee", ecomPlatformFee);
        data.put("platform_url", platformURL);

        return DBConnection.getInstance().insertIntoTable("ecommerces", data);
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