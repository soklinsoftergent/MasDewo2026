package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemType {
    private Integer itTyId; // Primary Key
    private String itemTypeName;
    private String description;

    /**
     * Constructor for creating a NEW ItemType
     */
    public ItemType(String itemTypeName, String description) {
        this.itemTypeName = itemTypeName;
        this.description = description;
    }

    /**
     * Constructor for loading from the Database
     */
    public ItemType(Integer id, String name, String description) {
        this.itTyId = id;
        this.itemTypeName = name;
        this.description = description;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.itTyId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            // Matches 'item_types' table and 'it_ty_id' PK from our SQL schema
            DBConnection.getInstance().updateField("item_types", "it_ty_id", this.itTyId, updates);
        }
    }

    // --- Setters (Triggers DB Update) ---

    public void setItemTypeName(String name) {
        this.itemTypeName = name;
        executeUpdate("name", name);
    }

    public void setDescription(String description) {
        this.description = description;
        executeUpdate("description", description);
    }

    // --- Getters ---

    public Integer getItTyId() { return itTyId; }
    public String getItemTypeName() { return itemTypeName; }
    public String getDescription() { return description; }

    // --- Database Operations ---

    public boolean save() {
        if (this.itTyId != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", this.itemTypeName);
        data.put("description", this.description);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("item_types", data);
        if (newId != null) {
            this.itTyId = newId;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Name: %s, Description: %s",
                itTyId, itemTypeName, (description != null ? description : "No description available"));
    }
}
