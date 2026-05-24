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
     * Constructor for loading from Database Map
     * Matches the output of db.selectAll() or db.fetchRow()
     */
    public ItemType(Map<String, Object> data) {
        this.itTyId = (Integer) data.get("it_ty_id");
        this.itemTypeName = (String) data.get("name");
        this.description = (String) data.get("description");
    }


    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.itTyId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
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

    // --- Active Record Logic ---

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

    public void refresh() {
        if (this.itTyId == null) return;

        // Uses the generic fetchRow helper
        Map<String, Object> data = DBConnection.getInstance().fetchRow("item_types", "it_ty_id", this.itTyId);

        if (data != null) {
            this.itemTypeName = (String) data.get("name");
            this.description = (String) data.get("description");
        }
    }

    @Override
    public String toString() {
        return itemTypeName; // Useful for ComboBox display
    }

//    public static void main(String[] args) {
//        DBConnection.initialize("localhost", "root", "", "masdewotrue");
//        new ItemType("Adapter", "").save();
//        new ItemType("L Plate", "").save();
//        new ItemType("Silicone", "").save();
//        new ItemType("Batre", "").save();
//        new ItemType("Strap", "").save();
//        new ItemType("Memory", "").save();
//        new ItemType("Cleaning Kit", "").save();
//        new ItemType("Studio", "").save();
//        new ItemType("Efek Foto", "").save();
//        new ItemType("Acc", "").save();
//        new ItemType("Kotak Musik", "").save();
//    }
}
