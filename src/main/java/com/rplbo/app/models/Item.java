package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Item {
    private Integer id;
    private String sku;
    private String name;
    private String brand;
    private String model;
    private String image;
    private int stock;
    private int itemTypeId;
    private Integer supplierId;
    private double purchasePrice;
    private double sellingPrice;

    /** Constructor for a NEW Item (not yet in DB) */
    public Item(String name, String brand, String model, int stock, int itemTypeId, Integer supplierId, double pPrice, double sPrice) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.stock = stock;
        this.itemTypeId = itemTypeId;
        this.supplierId = supplierId;
        this.purchasePrice = pPrice;
        this.sellingPrice = sPrice;
    }

    /**
     * Constructor for loading from Database Map
     * This makes ItemDAO.getAllItems() a simple one-liner loop!
     */
    public Item(Map<String, Object> data) {
        this.id = (Integer) data.get("id");
        this.sku = (String) data.get("sku");
        this.name = (String) data.get("name");
        this.brand = (String) data.get("brand");
        this.model = (String) data.get("model");
        this.image = (String) data.get("image");
        this.stock = ((Number) data.get("stock")).intValue();
        this.itemTypeId = ((Number) data.get("it_ty_id")).intValue();
        this.supplierId = ((Number) data.get("supplier_id")).intValue();
        this.purchasePrice = ((Number) data.get("purchase_price")).doubleValue();
        this.sellingPrice = ((Number) data.get("selling_price")).doubleValue();
    }


    public Item(Integer id, String sku, String name, String brand, String model, Integer stock, Integer itTyId, Integer supplierId, double purchasePrice, double sellingPrice) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.stock = stock;
        this.itemTypeId = itTyId;
        this.supplierId = supplierId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.id != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("items", "id", this.id, updates);
        }
    }

    // --- Business Logic Methods (From Python) ---

    public boolean isSoldOut() { return this.stock <= 0; }

    public void addItemStock(int amount) {
        this.stock += amount;
        executeUpdate("stock", this.stock);
    }

    // --- Setters (Triggers immediate DB update) ---

    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; executeUpdate("supplier_id", supplierId); }
    public void setBrand(String brand) { this.brand = brand; executeUpdate("brand", brand); }
    public void setModel(String model) { this.model = model; executeUpdate("model", model); }
    public void setName(String name) { this.name = name; executeUpdate("name", name); }
    public void setImage(String image) { this.image = image; executeUpdate("image", image); }
    public void setStock(int stock) { this.stock = stock; executeUpdate("stock", stock); }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; executeUpdate("purchase_price", purchasePrice); }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; executeUpdate("selling_price", sellingPrice); }

    // --- Getters ---

    public String getSku() { return this.sku; }
    public Integer getSupplierId() { return this.supplierId; }
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public int getStock() { return stock; }
    public int getItemTypeId() { return itemTypeId; }
    public double getPurchasePrice() { return purchasePrice; }
    public double getSellingPrice() { return sellingPrice; }

    // --- Database Operations ---

    /**
     * Saves a new Item and triggers the 2-step SKU generation logic.
     * @param typeName Category name (e.g. "Adapter") to build the SKU prefix.
     */
    public boolean save(String typeName) {
        if (this.id != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", this.name);
        data.put("brand", this.brand);
        data.put("model", this.model);
        data.put("image", this.image);
        data.put("stock", this.stock);
        data.put("it_ty_id", this.itemTypeId);
        data.put("supplier_id", this.supplierId);
        data.put("purchase_price", this.purchasePrice);
        data.put("selling_price", this.sellingPrice);

        // Satisfy the UNIQUE NOT NULL constraint with a temp value
        data.put("sku", "TMP-" + System.currentTimeMillis());

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("items", data);

        if (newId != null) {
            this.id = newId;
            this.sku = generateAutoSku(typeName); // Generate real SKU based on ID

            Map<String, Object> skuUpdate = new LinkedHashMap<>();
            skuUpdate.put("sku", this.sku);
            return DBConnection.getInstance().updateField("items", "id", this.id, skuUpdate);
        }
        return false;
    }

    public void refresh() {
        if (this.id == null) return;
        Map<String, Object> data = DBConnection.getInstance().fetchRow("items", "id", this.id);
        if (data != null) {
            this.sku = (String) data.get("sku");
            this.name = (String) data.get("name");
            this.stock = ((Number) data.get("stock")).intValue();
            this.purchasePrice = ((Number) data.get("purchase_price")).doubleValue();
            this.sellingPrice = ((Number) data.get("selling_price")).doubleValue();
        }
    }

    private String generateAutoSku(String typeName) {
        String typePart = typeName.replaceAll("[ a-z]", "").toUpperCase();
        if (typePart.length() < 3) typePart = typeName.substring(0, 3).toUpperCase();
        String brandPart = (brand != null && brand.length() >= 3) ? brand.substring(0, 3).toUpperCase() : "GEN";
        String modelPart = (model != null && model.length() >= 3) ? model.substring(0, 3).toUpperCase() : "MDL";
        return String.format("%s-%s-%s-%03d", typePart, brandPart, modelPart, id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (Stock: %d)", sku, name, stock);
    }

    public static void main(String[] args) {
        DBConnection.initialize("localhost", "root", "", "masdewotrue");
        Item item = new Item("Kamera", "Nikon", "AX10", 10, 1, 1,10000, 20000);
        System.out.println(item);
        String sku = item.generateAutoSku("Kamera");
        System.out.println(sku);
        item.save("Kamera");
    }
}
