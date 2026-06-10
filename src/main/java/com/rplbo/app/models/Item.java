package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;

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

        // --- USE SAFE CASTING TO PREVENT THE CRASH ---
        this.stock = safeInt(data.get("stock"));
        this.itTyId = safeInt(data.get("it_ty_id"));
        this.version = safeInt(data.get("version"));

        Object supp = data.get("supplier_id");
        this.supplierId = (supp != null) ? ((Number) supp).intValue() : null;

        this.purchasePrice = safeDouble(data.get("purchase_price"));
        this.sellingPrice = safeDouble(data.get("selling_price"));
    }

    // Add these private helpers at the bottom of Item.java
    private int safeInt(Object obj) {
        if (obj == null) return 0;
        return ((Number) obj).intValue();
    }

    private double safeDouble(Object obj) {
        if (obj == null) return 0.0;
        return ((Number) obj).doubleValue();
    }

    @Override
    public boolean save() {
        if (this.id != null) return false;

        // 1. Siapkan data untuk insert awal
        Map<String, Object> data = toMap();

        // Karena kolom SKU NOT NULL, beri nilai sementara yang unik
        data.put("sku", "PENDING-" + System.nanoTime());
        data.put("version", 1);

        // 2. Insert ke DB dan ambil ID yang baru saja dibuat
        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId(tableName(), data);

        if (newId != null) {
            this.id = newId;

            // 3. PANGGIL SMART UTIL UNTUK GENERATE SKU
            // Tidak perlu typename dari UI lagi!
            this.sku = com.rplbo.app.util.SkuGenerator.generate(this.id, this.itTyId, this.brand, this.model);

            // 4. Update SKU yang asli ke Database
            executeUpdate("sku", this.sku);
            return true;
        }
        return false;
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

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
        executeUpdate("selling_price", sellingPrice);
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