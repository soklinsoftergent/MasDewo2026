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
     * Constructor for loading an existing Role from the Database
     */
    public Role(Integer roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    // --- Active Record Helper ---
    private void executeUpdate(String field, Object value) {
        if (this.roleId != null) {
            Map<String, Object> updates = new LinkedHashMap<>();
            updates.put(field, value);
            // Matches 'roles' table and 'role_id' primary key from our SQL schema
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

    /**
     * Replicates save() logic found in other models.
     * Inserts the role and retrieves the generated ID.
     */
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

    /**
     * Replicates refresh() logic.
     */
    public void refresh() {
        if (this.roleId == null) return;

        try (ResultSet rs = DBConnection.getInstance().fetchOneByKeyColumn("*", "roles", "role_id", this.roleId)) {
            if (rs != null && rs.next()) {
                this.roleName = rs.getString("role_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return (roleId != null) ?
                String.format("Role(ID: %d, Name: %s)", roleId, roleName) :
                String.format("Role(Name: %s)", roleName);
    }
}
