package com.rplbo.app;

import com.rplbo.app.dao.*;
import com.rplbo.app.models.*;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.util.CSVExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    // DAOs
    private final ItemDAO itemDAO = new ItemDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FinanceDAO financeDAO = new FinanceDAO();

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

    @FXML public void showSales() {
        Node node = loadView("/com/rplbo/app/pages/SalesPage.fxml");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Node node = loader.load();
            viewCache.put(path, node);
            return node;
        } catch (IOException e) { throw new RuntimeException("FXML error: " + path, e); }
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
        System.out.println("Menyiapkan data laporan...");

        // 1. Ambil data dari DAO
        List<Map<String, Object>> reportData = saleDAO.getDetailedSalesReport();

        if (reportData == null || reportData.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Tidak ada data penjualan untuk diekspor.").show();
            return;
        }

        // 2. Jalankan Ekspor
        String fileName = "Laporan_Penjualan_" + System.currentTimeMillis();
        CSVExporter.exportSales(reportData, fileName);

        // 3. Beri feedback ke user
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Ekspor Berhasil");
        success.setHeaderText(null);
        success.setContentText("Laporan '" + fileName + ".csv' telah disimpan di folder Downloads Anda.");
        success.show();
    }
}