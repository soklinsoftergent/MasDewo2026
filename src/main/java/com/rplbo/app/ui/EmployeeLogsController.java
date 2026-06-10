package com.rplbo.app.ui;

import com.rplbo.app.dao.UserDAO;
import com.rplbo.app.models.User;
import com.rplbo.app.util.FormatterUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class EmployeeLogsController {
    @FXML private Label headerLabel;
    @FXML private TableView<Map<String, Object>> logTable;
    @FXML private TableColumn<Map<String, Object>, String> colWaktu, colAksi, colReferensi, colNominal;

    public void setEmployeeData(User user) {
        headerLabel.setText("Log Aktivitas: " + user.getUsername());

        UserDAO dao = new UserDAO();
        List<Map<String, Object>> logs = dao.getUnifiedEmployeeLogs(user.getUserId());

        // --- FIX: Cek apakah logs null sebelum memproses ---
        if (logs == null) {
            System.err.println("⚠️ Warning: Gagal mengambil data log atau data kosong.");
            logTable.setItems(FXCollections.observableArrayList()); // Set tabel kosong saja
            return;
        }

        colWaktu.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("waktu"))));
        colAksi.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("aksi"))));
        colReferensi.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("referensi"))));
        colNominal.setCellValueFactory(d -> {
            Object amtObj = d.getValue().get("nominal");
            double amt = (amtObj != null) ? ((Number) amtObj).doubleValue() : 0.0;
            return new SimpleStringProperty(amt == 0 ? "-" : com.rplbo.app.util.FormatterUtil.formatCurrency(amt));
        });

        logTable.setItems(FXCollections.observableArrayList(logs));
    }

    @FXML private void handleClose() {
        ((Stage) headerLabel.getScene().getWindow()).close();
    }
}