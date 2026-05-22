package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ActiveRecord {
    // These methods MUST be implemented by User, Item, etc.
    protected abstract String tableName();
    protected abstract String primaryKeyColumn();
    protected abstract Integer getId();
    protected abstract void setId(Integer id);
    public abstract Map<String, Object> toMap();
    public abstract void fromMap(Map<String, Object> data);

    /**
     * Generic Save: Works for any model!
     */
    public boolean save() {
        if (getId() != null) return false; // Already exists

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId(tableName(), toMap());
        if (newId != null) {
            setId(newId);
            return true;
        }
        return false;
    }

    /**
     * Generic Refresh: Works for any model!
     */
    public void refresh() {
        if (getId() == null) return;
        Map<String, Object> data = DBConnection.getInstance().fetchRow(tableName(), primaryKeyColumn(), getId());
        if (data != null) {
            fromMap(data);
        }
    }

    /**
     * Generic single-field update
     */
    protected void executeUpdate(String field, Object value) {
        if (getId() != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField(tableName(), primaryKeyColumn(), getId(), updates);
        }
    }

    

}