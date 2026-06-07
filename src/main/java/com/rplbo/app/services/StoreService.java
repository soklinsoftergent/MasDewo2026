package com.rplbo.app.services;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StoreService {
    public List<Sale> getSalesByUserId(int userId) {
        List<Sale> list = new ArrayList<>();
        // Custom query to filter by the logged-in user
        String sql = "SELECT * FROM sales WHERE user_id = ? ORDER BY created_at DESC";

        // We use a manual loop here because selectAll isn't filtered
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Sale(DBConnection.getInstance().rowToMap(rs)));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
