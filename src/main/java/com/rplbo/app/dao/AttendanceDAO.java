package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Attendance;

import java.sql.PreparedStatement;
import java.util.Map;

public class AttendanceDAO {

    public Attendance findActiveShift(int userId) {
        Map<String, Object> map = DBConnection.getInstance().fetchRow("attendance", "user_id", userId);
        if (map.get("clock_out") != null) {
            return new Attendance(map);
        }
        return null;
    }

    public boolean isInShift(int userId) {
        Map<String, Object> map = DBConnection.getInstance().fetchRow("attendance", "user_id", userId);
        return map.get("clock_out") == null;
    }

    
}
