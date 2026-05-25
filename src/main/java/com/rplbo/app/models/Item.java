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
    private int itemTypeId;
    private Integer supplierId;
    private double purchasePrice;
    private double sellingPrice;

    // =========================
    // Constructors
    // =========================

    public Item(
            String name,
            String brand,
            String model,
            int stock,
            int itemTypeId,
            Integer supplierId,
            double pPrice,
            double sPrice
    ) {
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.stock = stock;
        this.itemTypeId = itemTypeId;
        this.supplierId = supplierId;
        this.purchasePrice = pPrice;
        this.sellingPrice = sPrice;
    }

    public Item(
            String sku,
            String name,
            String brand,
            String model,
            int stock,
            int itemTypeId,
            Integer supplierId,
            double purchasePrice,
            double sellingPrice
    ) {
        this.sku = sku;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.stock = stock;
        this.itemTypeId = itemTypeId;
        this.supplierId = supplierId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    public Item(Map<String, Object> data) {
        fromMap(data);
    }

    // =========================
    // ActiveRecord Implementation
    // =========================

    @Override
    protected String tableName() {
        return "items";
    }

    @Override
    protected String primaryKeyColumn() {
        return "id";
    }

    public Integer getId() {
        return this.id;
    }

    @Override
    protected void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("sku", this.sku);
        data.put("name", this.name);
        data.put("brand", this.brand);
        data.put("model", this.model);
        data.put("image", this.image);
        data.put("stock", this.stock);
        data.put("it_ty_id", this.itemTypeId);
        data.put("supplier_id", this.supplierId);
        data.put("purchase_price", this.purchasePrice);
        data.put("selling_price", this.sellingPrice);

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

        this.stock = data.get("stock") != null
                ? ((Number) data.get("stock")).intValue()
                : 0;

        this.itemTypeId = data.get("it_ty_id") != null
                ? ((Number) data.get("it_ty_id")).intValue()
                : 0;

        this.supplierId = data.get("supplier_id") != null
                ? ((Number) data.get("supplier_id")).intValue()
                : null;

        this.purchasePrice = data.get("purchase_price") != null
                ? ((Number) data.get("purchase_price")).doubleValue()
                : 0;

        this.sellingPrice = data.get("selling_price") != null
                ? ((Number) data.get("selling_price")).doubleValue()
                : 0;
    }

    // =========================
    // Custom Save Logic
    // =========================

    /**
     * Item overrides save because it has SKU auto-generation logic.
     */
    public boolean save(String typeName) {

        if (this.id != null) return false;

        // Temporary SKU
        if (this.sku == null) {
            this.sku = "TMP-" + System.currentTimeMillis();
        }

        Integer newId = DBConnection.getInstance()
                .insertIntoTableAndGetId(tableName(), toMap());

        if (newId != null) {

            this.id = newId;

            // Auto-generate only if imported SKU does not exist
            if (this.sku.startsWith("TMP-")) {

                this.sku = generateAutoSku(typeName);

                Map<String, Object> updates = new LinkedHashMap<>();
                updates.put("sku", this.sku);

                DBConnection.getInstance().updateField(
                        tableName(),
                        primaryKeyColumn(),
                        this.id,
                        updates
                );
            }

            return true;
        }

        return false;
    }

    // =========================
    // Business Logic
    // =========================

    public boolean isSoldOut() {
        return this.stock <= 0;
    }

    public void addItemStock(int amount) {
        this.stock += amount;
        executeUpdate("stock", this.stock);
    }

    private String generateAutoSku(String typeName) {

        String typePart = typeName
                .replaceAll("[ a-z]", "")
                .toUpperCase();

        if (typePart.length() < 3) {
            typePart = typeName.substring(0, 3).toUpperCase();
        }

        String brandPart =
                (brand != null && brand.length() >= 3)
                        ? brand.substring(0, 3).toUpperCase()
                        : "GEN";

        String modelPart =
                (model != null && model.length() >= 3)
                        ? model.substring(0, 3).toUpperCase()
                        : "MDL";

        return String.format(
                "%s-%s-%s-%03d",
                typePart,
                brandPart,
                modelPart,
                id
        );
    }

    // =========================
    // Setters
    // =========================

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
        executeUpdate("supplier_id", supplierId);
    }

    public void setBrand(String brand) {
        this.brand = brand;
        executeUpdate("brand", brand);
    }

    public void setModel(String model) {
        this.model = model;
        executeUpdate("model", model);
    }

    public void setName(String name) {
        this.name = name;
        executeUpdate("name", name);
    }

    public void setImage(String image) {
        this.image = image;
        executeUpdate("image", image);
    }

    public void setStock(int stock) {
        this.stock = stock;
        executeUpdate("stock", stock);
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
        executeUpdate("purchase_price", purchasePrice);
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
        executeUpdate("selling_price", sellingPrice);
    }

    // =========================
    // Getters
    // =========================

    public String getSku() {
        return sku;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public int getStock() {
        return stock;
    }

    public int getItemTypeId() {
        return itemTypeId;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    // =========================
    // Debug
    // =========================

    @Override
    public String toString() {
        return String.format(
                "[%s] %s (Stock: %d)",
                sku,
                name,
                stock
        );
    }
}