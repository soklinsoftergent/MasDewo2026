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
        try {
            this.id = (Integer) data.get("id");
            this.sku = (String) data.get("sku");
            this.name = (String) data.get("name");
            this.brand = (String) data.get("brand");
            this.model = (String) data.get("model");
            this.image = (String) data.get("image");

            // --- PERBAIKAN: Gunakan helper agar tidak crash jika NULL ---
            this.stock = safeInt(data.get("stock"), 0);
            this.itTyId = safeInt(data.get("it_ty_id"), 1); // Default ke kategori 1

            // Supplier ID boleh null, jadi kita tidak paksa ke int 0
            Object supplierObj = data.get("supplier_id");
            this.supplierId = (supplierObj != null) ? ((Number) supplierObj).intValue() : null;

            this.purchasePrice = safeDouble(data.get("purchase_price"), 0.0);
            this.sellingPrice = safeDouble(data.get("selling_price"), 0.0);

            this.version = safeInt(data.get("version"), 1);

            Object extIdObj = data.get("external_id");
            this.externalId = (extIdObj != null) ? ((Number) extIdObj).intValue() : null;
            // -----------------------------------------------------------

        } catch (Exception e) {
            System.err.println("❌ Error mapping Item data: " + data);
            e.printStackTrace();
        }
    }

// --- Tambahkan 2 Method Pembantu ini di bawah fromMap ---

    private int safeInt(Object obj, int defaultValue) {
        if (obj == null) return defaultValue;
        return ((Number) obj).intValue();
    }

    private double safeDouble(Object obj, double defaultValue) {
        if (obj == null) return defaultValue;
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