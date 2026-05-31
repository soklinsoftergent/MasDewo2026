package com.rplbo.app;

import com.rplbo.app.dao.*;
import com.rplbo.app.models.*;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.util.CSVExporter;
import com.rplbo.app.util.FormatterUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class MainDashboardController {
    // UI References from MainDashboard.fxml
    @FXML private StackPane contentArea;
    @FXML private Label pageTitleLabel, criticalStockBadge, adminRoleBadge;
    @FXML private Button dashboardButton, inventoryButton, salesButton, financeButton, employeeButton;
    @FXML private Button btnAttendance;
    @FXML private Label shiftTimerLabel;

    // DAOs
    private final ItemDAO itemDAO = new ItemDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FinanceDAO financeDAO = new FinanceDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    // Caching UI Nodes for speed
    private final Map<String, Node> viewCache = new HashMap<>();
    private final DecimalFormat idr = new DecimalFormat("Rp #,###");

    // We store these so the "Badge" can interact with the Inventory page
    private TextField globalInventorySearchField;

    @FXML
    public void initialize() {
        setupAccessControl();

        // Set up the "Jump to Inventory" logic from the header badge
        criticalStockBadge.setOnMouseClicked(event -> {
            showInventory();
            if (globalInventorySearchField != null) {
                globalInventorySearchField.setText("Kritis");
                // This triggers the listener automatically
            }
        });

        showDashboard(); // Default startup view
        refreshAttendanceStatus();
    }

    private void setupAccessControl() {
        User current = UserSession.getInstance().getCurrentUser();
        boolean isAdmin = (current != null && current.isAdmin());
        adminRoleBadge.setText(isAdmin ? "Admin" : "Staff");

        financeButton.setVisible(isAdmin);
        financeButton.setManaged(isAdmin);
        employeeButton.setVisible(isAdmin);
        employeeButton.setManaged(isAdmin);
    }

    // --- PAGE NAVIGATION ---

    @FXML public void showDashboard() {
        Node node = loadView("/com/rplbo/app/pages/DashboardPage.fxml");
        renderView(node, "Dashboard", dashboardButton);
    }

    @FXML public void showInventory() {
        Node node = loadView("/com/rplbo/app/pages/InventoryPage.fxml");
        renderView(node, "Inventory", inventoryButton);
    }

    @FXML
    public void showSales() {
        // Pastikan folder 'pages' memang ada di dalam resources/com/rplbo/app/
        Node node = loadView("/com/rplbo/app/pages/SalesPage.fxml");

        // Pastikan pencarian ID dilakukan SETELAH node dipastikan tidak null
        if (node instanceof VBox) {
            TableView<Map<String, Object>> table = (TableView<Map<String, Object>>) node.lookup("#salesTable");
            if (table != null) {
                table.setItems(FXCollections.observableArrayList(saleDAO.getAllSalesDetailed()));
            }
        }

        renderView(node, "Penjualan", salesButton);
    }



    @FXML public void showFinance() {
        Node node = loadView("/com/rplbo/app/pages/FinancePage.fxml");
        renderView(node, "Finance & Kas", financeButton);
    }

    @FXML public void showEmployees() {
        Node node = loadView("/com/rplbo/app/pages/EmployeesPage.fxml");
        renderView(node, "Karyawan", employeeButton);
    }

    // --- DATA PLUMBING (DAO -> FXML) ---
    // --- UI HELPERS ---

    private Node loadView(String path) {
        if (viewCache.containsKey(path)) return viewCache.get(path);

        try {
            // 1. Ambil URL Resource
            java.net.URL resource = getClass().getResource(path);

            // 2. Cek apakah null?
            if (resource == null) {
                System.err.println("❌ ERROR: File FXML tidak ditemukan di path: " + path);
                return new Label("File tidak ditemukan: " + path);
            }

            // 3. Muat FXML
            FXMLLoader loader = new FXMLLoader(resource);
            Node node = loader.load();

            viewCache.put(path, node);
            return node;
        } catch (IOException e) {
            System.err.println("❌ ERROR: Gagal memuat file FXML!");
            e.printStackTrace();
            return new Label("Error loading " + path);
        }
    }

    private void renderView(Node node, String title, Button activeBtn) {
        contentArea.getChildren().setAll(node);
        pageTitleLabel.setText(title);
        resetSidebarStyles();
        if (activeBtn != null) activeBtn.setStyle("-fx-background-color: #49647c; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void resetSidebarStyles() {
        List.of(dashboardButton, inventoryButton, salesButton, financeButton, employeeButton)
                .forEach(b -> b.setStyle("-fx-background-color: transparent; -fx-text-fill: #627181;"));
    }
//
//    @FXML
//    private void handleExportReport() {
//        List<Map<String, Object>> data = saleDAO.getMonthlyReportData();
//        boolean success = CSVExporter.export(data, "Laporan_Bulanan");
//        if (success) {
//            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Laporan berhasil diunduh ke folder Downloads!");
//            alert.show();
//        } else {
//            Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal mengekspor laporan.");
//            alert.show();
//        }
//    }

    @FXML
    private void handleExportReport() {
        // 1. Get the data from your DAO
        // Ensure this method returns columns with the labels you want in Excel
        List<Map<String, Object>> reportData = saleDAO.getDetailedSalesReport();

        if (reportData != null && !reportData.isEmpty()) {
            // 2. Generate a meaningful filename
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            String fileName = "Laporan_Penjualan_" + timestamp;

            // 3. CALL THE EXPORTER
            CSVExporter.exportData(reportData, fileName);

            // 4. Show success popup
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ekspor Berhasil");
            alert.setHeaderText(null);
            alert.setContentText("Laporan berhasil disimpan di folder Downloads:\n" + fileName + ".csv");
            alert.show();
        } else {
            new Alert(Alert.AlertType.WARNING, "Tidak ada data untuk diekspor!").show();
        }
    }

    private void refreshAttendanceStatus() {
        User current = UserSession.getInstance().getCurrentUser();

        // Safety check jika user session kosong
        if (current == null) return;

        Attendance activeShift = attendanceDAO.findActiveShift(current.getUserId());

        if (activeShift != null) {
            btnAttendance.setText("PULANG / SELESAI");
            btnAttendance.setStyle("-fx-background-color: #ff8e8e; -fx-text-fill: white;");
            shiftTimerLabel.setText("Masuk: " + com.rplbo.app.util.FormatterUtil.formatDate(activeShift.getClockIn()));
        } else {
            btnAttendance.setText("MASUK KERJA");
            btnAttendance.setStyle("-fx-background-color: #79e07c; -fx-text-fill: #1f2937;");
            shiftTimerLabel.setText("Belum absen masuk");
        }
    }

    @FXML
    private void handleAttendanceAction() {
        User current = UserSession.getInstance().getCurrentUser();
        Attendance activeShift = attendanceDAO.findActiveShift(current.getUserId());

        if (activeShift == null) {
            // PROSES CLOCK-IN
            if (attendanceDAO.clockIn(current.getUserId())) {
                new Alert(Alert.AlertType.INFORMATION, "Selamat bekerja, " + current.getUsername() + "!").show();
            }
        } else {
            // PROSES CLOCK-OUT (Closing Shift Report)
            handleClosingShift(activeShift);
        }
        refreshAttendanceStatus();
    }

    private void handleClosingShift(Attendance shift) {
        User current = UserSession.getInstance().getCurrentUser();

        // 1. Hitung total penjualan selama shift ini (Requirement: Closing Shift Report)
        double totalSales = saleDAO.getTotalSalesInShift(current.getUserId(), shift.getClockIn(), java.time.LocalDateTime.now());

        // 2. Tampilkan laporan singkat ke karyawan
        String reportMsg = String.format(
                "Shift Berakhir.\n\nTotal Penjualan Anda: %s\nSilakan setorkan uang ke kasir Admin.",
                FormatterUtil.formatCurrency(totalSales)
        );

        Alert report = new Alert(Alert.AlertType.INFORMATION, reportMsg, ButtonType.OK);
        report.setHeaderText("Laporan Penutupan Shift");
        report.showAndWait();

        // 3. Simpan waktu keluar ke DB
        shift.doClockOut();
    }
}