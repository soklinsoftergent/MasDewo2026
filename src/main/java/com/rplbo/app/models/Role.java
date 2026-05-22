package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class Role {
    private Integer roleId;
    private String roleName;

    /**
     * Constructor for creating a NEW Role (not yet in DB)
     */
    public Role(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Constructor for loading from Database Map
     * Matches the output of db.selectAll() or db.fetchRow()
     */
    public Role(Map<String, Object> data) {
        this.roleId = (Integer) data.get("role_id");
        this.roleName = (String) data.get("role_name");
    }

    private void executeUpdate(String field, Object value) {
        if (this.roleId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            DBConnection.getInstance().updateField("roles", "role_id", this.roleId, updates);
        }
    }

    // --- Getters & Setters ---

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    /**
     * Replicates setRoleName. Updates DB immediately if roleId exists.
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
        executeUpdate("role_name", roleName);
    }

    // --- Database Operations ---

    public boolean save() {
        if (this.roleId != null) return false;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role_name", this.roleName);

        Integer newId = DBConnection.getInstance().insertIntoTableAndGetId("roles", data);
        if (newId != null) {
            this.roleId = newId;
            return true;
        }
        return false;
    }

    public void refresh() {
        if (this.roleId == null) return;

        // Use the generic fetchRow helper from DBConnection
        Map<String, Object> data = DBConnection.getInstance().fetchRow("roles", "role_id", this.roleId);

        if (data != null) {
            this.roleName = (String) data.get("role_name");
        }
    }

    @Override
    public String toString() {
        return roleName; // Returns "Admin" or "Staff" for UI display
    }
}
