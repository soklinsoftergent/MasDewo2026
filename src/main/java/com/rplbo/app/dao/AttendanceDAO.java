package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Attendance;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class AttendanceDAO {

    public Attendance findActiveShift(int userId) {
        // Gunakan selectAllCustom agar bisa mencari user_id AND clock_out IS NULL
        String sql = "SELECT * FROM attendance WHERE user_id = ? AND clock_out IS NULL LIMIT 1";
        List<Map<String, Object>> results = DBConnection.getInstance().selectAllCustom(sql, userId);

        // --- FIX: Cek apakah results null atau kosong sebelum diakses ---
        if (results != null && !results.isEmpty()) {
            return new Attendance(results.get(0));
        }

        return null; // Tidak ada shift aktif
    }

    public boolean isInShift(int userId) {
        return findActiveShift(userId) != null;
    }

    public boolean clockIn(int userId) {
        Attendance attendance = new Attendance(userId);
        return attendance.save();
    }

    public boolean clockOut(int userId) {
        Attendance activeShift = findActiveShift(userId);
        if (activeShift != null) {
            activeShift.doClockOut();
            return true;
        }
        return false;
    }
}
