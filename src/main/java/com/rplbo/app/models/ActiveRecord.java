package com.rplbo.app.models;

import com.rplbo.app.db.DBConnection;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * Helper untuk menangani perbedaan format tanggal antara MySQL dan SQLite.
     */
    protected LocalDateTime safeDateTime(Object obj) {
        if (obj == null) return LocalDateTime.now();

        // Jika sudah berupa LocalDateTime (MySQL)
        if (obj instanceof LocalDateTime) {
            return (LocalDateTime) obj;
        }

        // Jika berupa Timestamp (Beberapa versi MySQL driver)
        if (obj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) obj).toLocalDateTime();
        }

        // Jika berupa String (SQLite)
        if (obj instanceof String) {
            String dateStr = (String) obj;
            // SQLite biasanya pakai format "yyyy-MM-dd HH:mm:ss"
            // Kita ubah spasi menjadi 'T' agar sesuai standar ISO (yyyy-MM-ddTHH:mm:ss)
            try {
                return LocalDateTime.parse(dateStr.replace(" ", "T"));
            } catch (Exception e) {
                // Jika formatnya hanya tanggal saja
                return LocalDateTime.now();
            }
        }

        return LocalDateTime.now();
    }

//    // Di dalam ActiveRecord.java
//    protected LocalDateTime safeDateTime(Object obj) {
//        if (obj == null) return null;
//        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
//        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
//        if (obj instanceof String) {
//            try {
//                return LocalDateTime.parse(((String) obj).replace(" ", "T"));
//            } catch (Exception e) {
//                return null;
//            }
//        }
//        return null;
//    }

    protected LocalDate safeDate(Object obj) {
        if (obj == null) return null;
        if (obj instanceof LocalDate) return (LocalDate) obj;
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate();
        if (obj instanceof String) {
            try {
                // Mengambil 10 karakter pertama (yyyy-MM-dd) jika formatnya panjang
                return LocalDate.parse(((String) obj).substring(0, 10));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}