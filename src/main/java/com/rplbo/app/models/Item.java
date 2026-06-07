package com.rplbo.app.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Item extends ActiveRecord {
    private Integer id;
    private String sku;
    private String name;
    private String brand;
    private String model;
    private String image;
    private int stock;
    private int itTyId;
    private Integer supplierId;
    private double purchasePrice;
    private double sellingPrice;
    private int version;
    private Integer externalId;

    // --- Constructors ---
    public Item(String name, String brand, String model, int stock, int itTyId, double buy, double sell) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.stock = stock;
        this.itTyId = itTyId;
        this.purchasePrice = buy;
        this.sellingPrice = sell;
        this.version = 1; // Default version untuk barang baru
        this.externalId = null;
    }

    public Item(Map<String, Object> data) { fromMap(data); }

    // --- ActiveRecord Implementation ---
    @Override protected String tableName() { return "items"; }
    @Override protected String primaryKeyColumn() { return "id"; }
    @Override
    public Integer getId() { return id; }
    @Override protected void setId(Integer id) { this.id = id; }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sku", sku);
        data.put("name", name);
        data.put("brand", brand);
        data.put("model", model);
        data.put("image", image);
        data.put("stock", stock);
        data.put("it_ty_id", itTyId);
        data.put("supplier_id", supplierId);
        data.put("purchase_price", purchasePrice);
        data.put("selling_price", sellingPrice);
        data.put("version", version);
        data.put("external_id", externalId);
        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.id = (Integer) data.get("id");
        this.sku = (String) data.get("sku");
        this.name = (String) data.get("name");
        this.brand = (String) data.get("brand");
        this.model = (String) data.get("model");
        this.image = (String) data.get("image");
        this.stock = ((Number) data.get("stock")).intValue();
        this.itTyId = ((Number) data.get("it_ty_id")).intValue();
        this.supplierId = (data.get("supplier_id") != null) ? ((Number) data.get("supplier_id")).intValue() : null;
        this.purchasePrice = ((Number) data.get("purchase_price")).doubleValue();
        this.sellingPrice = ((Number) data.get("selling_price")).doubleValue();
        this.version = ((Number) data.get("version")).intValue();
        this.externalId = (data.get("external_id") != null) ? ((Number) data.get("external_id")).intValue() : null;
    }

    // --- Setters (Auto-Sync) ---
    public void setStock(int stock) {
        this.stock = stock;
        executeUpdate("stock", stock);
    }

    public void setImage(String path) {
        this.image = path;
        executeUpdate("image", path);
    }

    public void setExternalId(Integer externalId) {
        this.externalId = externalId;
        // Sekarang kita bisa memanggil executeUpdate karena kita berada di dalam kelas Item
        executeUpdate("external_id", externalId);
    }

    // --- Getters ---
    public String getSku() { return sku; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public int getStock() { return stock; }
    public double getSellingPrice() { return sellingPrice; }
    public double getPurchasePrice() { return purchasePrice; }
    public int getVersion() { return version; }
    public int getItTyId() { return itTyId; }
    public Integer getExternalId() { return externalId; }
}