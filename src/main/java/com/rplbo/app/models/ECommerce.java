package com.rplbo.app.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class ECommerce extends ActiveRecord {

    private Integer ecomId;
    private String ecomName;
    private double ecomPlatformFee;
    private String platformURL;

    /**
     * Constructor for NEW platform
     */
    public ECommerce(String ecomName, double ecomPlatformFee, String platformURL) {
        this.ecomName = ecomName;
        this.ecomPlatformFee = ecomPlatformFee;
        this.platformURL = platformURL;
    }

    /**
     * Constructor from DB data
     */
    public ECommerce(Map<String, Object> data) {
        fromMap(data);
    }

    // =====================================================
    // ActiveRecord Implementation
    // =====================================================

    @Override
    protected String tableName() {
        return "ecommerces";
    }

    @Override
    protected String primaryKeyColumn() {
        return "ecom_id";
    }

    @Override
    protected Integer getId() {
        return ecomId;
    }

    @Override
    protected void setId(Integer id) {
        this.ecomId = id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("ecom_name", ecomName);
        data.put("ecom_platform_fee", ecomPlatformFee);
        data.put("platform_url", platformURL);

        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.ecomId = (Integer) data.get("ecom_id");
        this.ecomName = (String) data.get("ecom_name");

        Object fee = data.get("ecom_platform_fee");
        this.ecomPlatformFee = fee == null
                ? 0.0
                : ((Number) fee).doubleValue();

        this.platformURL = (String) data.get("platform_url");
    }

    // =====================================================
    // Setters (Auto Update DB)
    // =====================================================

    public void setEcomName(String ecomName) {
        this.ecomName = ecomName;
        executeUpdate("ecom_name", ecomName);
    }

    public void setEcomPlatformFee(double ecomPlatformFee) {
        this.ecomPlatformFee = ecomPlatformFee;
        executeUpdate("ecom_platform_fee", ecomPlatformFee);
    }

    public void setPlatformURL(String platformURL) {
        this.platformURL = platformURL;
        executeUpdate("platform_url", platformURL);
    }

    // =====================================================
    // Getters
    // =====================================================

    public Integer getEcomId() {
        return ecomId;
    }

    public String getEcomName() {
        return ecomName;
    }

    public double getEcomPlatformFee() {
        return ecomPlatformFee;
    }

    public String getPlatformURL() {
        return platformURL;
    }

    // =====================================================
    // Utility Methods
    // =====================================================

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