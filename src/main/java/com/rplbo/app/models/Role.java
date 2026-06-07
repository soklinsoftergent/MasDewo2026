package com.rplbo.app.models;

import java.util.LinkedHashMap;
import java.util.Map;

public class Role extends ActiveRecord {

    private Integer roleId;
    private String roleName;

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public Role(Map<String, Object> data) {
        fromMap(data);
    }

    // ==========================================
    // ActiveRecord Implementation
    // ==========================================

    @Override
    protected String tableName() {
        return "roles";
    }

    @Override
    protected String primaryKeyColumn() {
        return "role_id";
    }

    @Override
    protected Integer getId() {
        return roleId;
    }

    @Override
    protected void setId(Integer id) {
        this.roleId = id;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role_name", roleName);
        return data;
    }

    @Override
    public void fromMap(Map<String, Object> data) {
        this.roleId = (Integer) data.get("role_id");
        this.roleName = (String) data.get("role_name");
    }

    // ==========================================
    // Getters
    // ==========================================

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    // ==========================================
    // Setters
    // ==========================================

    public void setRoleName(String roleName) {
        this.roleName = roleName;
        executeUpdate("role_name", roleName);
    }

    @Override
    public String toString() {
        return roleName;
    }
}