package com.rplbo.app.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemType extends ActiveRecord {

    private Integer itTyId;
    private String itemTypeName;
    private String description;

    public ItemType(String itemTypeName, String description) {
        this.itemTypeName = itemTypeName;
        this.description = description;
    }

    public ItemType(Map<String, Object> data) {
        fromMap(data);
    }

    // ==========================================
    // ActiveRecord Implementation
    // ==========================================

    @Override
    protected String tableName() {
        return "item_types";
    }

    @Override
    protected String primaryKeyColumn() {
        return "it_ty_id";
    }

    @Override
    protected Integer getId() {
        return itTyId;
    }

    @Override
    protected void setId(Integer id) {
        this.itTyId = id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", itemTypeName);
        data.put("description", description);
        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.itTyId = (Integer) data.get("it_ty_id");
        this.itemTypeName = (String) data.get("name");
        this.description = (String) data.get("description");
    }

    // ==========================================
    // Getters
    // ==========================================

    public Integer getItTyId() {
        return itTyId;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public String getDescription() {
        return description;
    }

    // ==========================================
    // Setters
    // ==========================================

    public void setItemTypeName(String name) {
        this.itemTypeName = name;
        executeUpdate("name", name);
    }

    public void setDescription(String description) {
        this.description = description;
        executeUpdate("description", description);
    }

    @Override
    public String toString() {
        return itemTypeName;
    }
}