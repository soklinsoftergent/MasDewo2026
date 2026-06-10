package com.rplbo.app;

import com.rplbo.app.dao.*;
import com.rplbo.app.models.*;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.ui.DashboardController;
import com.rplbo.app.ui.FinanceController;
import com.rplbo.app.ui.InventoryController;
import com.rplbo.app.ui.SalesController;
import com.rplbo.app.util.CSVExporter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class MainDashboardController {
    // UI References from MainDashboard.fxml
    @FXML private StackPane contentArea;
    @FXML private Label pageTitleLabel, criticalStockBadge, adminRoleBadge;
    @FXML private Button dashboardButton, inventoryButton, salesButton, financeButton, employeeButton, btnAttendance, restockButton, supplierButton;
    @FXML private Label shiftTimerLabel;


    // DAOs
    private final ItemDAO itemDAO = new ItemDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FinanceDAO financeDAO = new FinanceDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    // Caching UI Nodes for speed
    private final Map<String, Object> controllerCache = new LinkedHashMap<>();
    private final Map<String, Node> viewCache = new HashMap<>();
    private final DecimalFormat idr = new DecimalFormat("Rp #,###");

    // We store these so the "Badge" can interact with the Inventory page
    private TextField globalInventorySearchField;

    // Shift control
    private boolean isShiftActive = false;

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



    @FXML
    public void showRestock() {
        // Gunakan helper loadView yang sudah Anda miliki
        Node node = loadView("/com/rplbo/app/pages/RestockPage.fxml");

        // Opsional: Jika ingin refresh data supplier setiap kali halaman dibuka
        // RestockController controller = (RestockController) lastLoadedController;
        // controller.refreshSuppliers();

        renderView(node, "Restok Barang", restockButton);
    }

    @FXML
    public void showDashboard() {
        String DASHBOARD_PAGE = "/com/rplbo/app/pages/DashboardPage.fxml";
        Node node = loadView(DASHBOARD_PAGE);
        DashboardController controller = (DashboardController) controllerCache.get(DASHBOARD_PAGE);

        if (com.rplbo.app.util.DataStateSignal.dashboardNeedsRefresh) {
            System.out.println("📊 Data Bisnis berubah. Me-refresh Dashboard...");
            controller.refresh();
            com.rplbo.app.util.DataStateSignal.dashboardNeedsRefresh = false;
        }

        renderView(node, "Dashboard", dashboardButton);
    }

    // Logika Navigasi Baru (Contoh untuk Inventory)
    @FXML
    public void showInventory() {
        Node node = loadView("/com/rplbo/app/pages/InventoryPage.fxml");
        InventoryController controller = (InventoryController) controllerCache.get("/com/rplbo/app/pages/InventoryPage.fxml");

        // CEK APAKAH DATA KOTOR? (Requirement Anda)
        if (com.rplbo.app.util.DataStateSignal.inventoryNeedsRefresh) {
            System.out.println("🔄 Data Inventory berubah. Me-refresh tabel...");
            controller.refresh();
            com.rplbo.app.util.DataStateSignal.inventoryNeedsRefresh = false; // Reset sinyal
        }

        renderView(node, "Inventory", inventoryButton);
    }

    @FXML
    public void showSales() {
        String path = "/com/rplbo/app/pages/SalesPage.fxml";
        Node node = loadView(path);
        SalesController controller = (SalesController) controllerCache.get(path);

        if (com.rplbo.app.util.DataStateSignal.salesNeedsRefresh) {
            controller.loadSalesHistory(); // Pastikan SalesController implements Refreshable
            com.rplbo.app.util.DataStateSignal.salesNeedsRefresh = false;
        }

        renderView(node, "Penjualan", salesButton);
    }

    @FXML
    public void showSuppliers() {
        Node node = loadView("/com/rplbo/app/pages/SuppliersPage.fxml");
        renderView(node, "Manajemen Supplier", supplierButton);
    }

    @FXML
    public void showFinance() {
        String FINANCE_PAGE = "/com/rplbo/app/pages/FinancePage.fxml";
        Node node = loadView(FINANCE_PAGE);
        FinanceController controller = (FinanceController) controllerCache.get(FINANCE_PAGE);

        if (com.rplbo.app.util.DataStateSignal.dashboardNeedsRefresh) {
            System.out.println("📊 Data Bisnis berubah. Me-refresh Dashboard...");
            controller.refresh();
            com.rplbo.app.util.DataStateSignal.dashboardNeedsRefresh = false;
        }

        renderView(node, "Keuangan", financeButton);
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

            // Simpan Node dan Controller-nya
            viewCache.put(path, node);
            controllerCache.put(path, loader.getController());

            return node;
        } catch (IOException e) { e.printStackTrace(); return new Label("Error"); }
    }


    private void renderView(Node node, String title, Button activeBtn) {
        contentArea.getChildren().setAll(node);
        pageTitleLabel.setText(title);

        // 1. Matikan semua warna tombol dulu
        resetSidebarStyles();

        // 2. Nyalakan warna hanya untuk tombol yang diklik
        if (activeBtn != null) {
            activeBtn.setStyle("-fx-background-color: #49647c; " + // Biru Slate aktif
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: 700; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 12 16; " +
                    "-fx-alignment: CENTER_LEFT;");
        }
    }

    private void resetSidebarStyles() {
        // Tambahkan SEMUA tombol ke dalam List ini
        List<Button> allButtons = List.of(
                dashboardButton, inventoryButton, restockButton,
                salesButton, supplierButton, financeButton, employeeButton
        );

        allButtons.forEach(b -> {
            if (b != null) {
                // Style standar untuk tombol tidak aktif
                b.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: #627181; " + // Abu-abu
                        "-fx-font-weight: 600; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 12 16; " +
                        "-fx-alignment: CENTER_LEFT;");
            }
        });
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

        // 1. Ambil Ringkasan Penjualan dari SaleDAO
        // Kita gunakan method getClosingReport yang sudah kita buat di SaleDAO
        Map<String, Object> reportData = saleDAO.getClosingReport(current.getUserId());

        int totalTrx = 0;
        double totalCash = 0;
        if (reportData != null) {
            totalTrx = ((Number) reportData.get("total_orders")).intValue();
            totalCash = (reportData.get("total_cash") != null) ?
                    ((Number) reportData.get("total_cash")).doubleValue() : 0.0;
        }

        // 2. Hitung Durasi Kerja (Closing Hours Logic)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(shift.getClockIn(), now);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        // 3. Rakit Pesan Laporan Penutupan
        String reportMsg = String.format(
                "📝 LAPORAN PENUTUPAN SHIFT\n" +
                        "----------------------------------\n" +
                        "Karyawan    : %s\n" +
                        "Waktu Masuk : %s\n" +
                        "Waktu Keluar: %s\n" +
                        "Total Durasi: %d Jam %d Menit\n" +
                        "----------------------------------\n" +
                        "Total Order : %d Transaksi\n" +
                        "Total Uang   : %s\n" +
                        "----------------------------------\n" +
                        "Harap serahkan uang tunai sesuai jumlah di atas\n" +
                        "kepada Admin sebelum meninggalkan toko.",
                current.getUsername().toUpperCase(),
                com.rplbo.app.util.FormatterUtil.formatDate(shift.getClockIn()),
                com.rplbo.app.util.FormatterUtil.formatDate(now),
                hours, minutes,
                totalTrx,
                com.rplbo.app.util.FormatterUtil.formatCurrency(totalCash)
        );

        // 4. Tampilkan Konfirmasi Final
        Alert report = new Alert(Alert.AlertType.INFORMATION);
        report.setTitle("Shift Summary");
        report.setHeaderText("Terima kasih atas kerja keras Anda hari ini!");
        report.setContentText(reportMsg);

        // Tunggu sampai user klik OK baru tutup shift di DB
        report.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 5. Simpan waktu keluar ke DB (ActiveRecord)
                shift.doClockOut();
                System.out.println("✅ Shift closed for " + current.getUsername());

//                 6. Opsional: Auto-Logout setelah tutup shift demi keamanan
//                 handleLogout(null);
            }
        });
    }

    @FXML
    private void handleExportMonthlySummary() {
        // 🛡️ Hanya Admin yang boleh melihat laba bersih bulanan
        if (!UserSession.getInstance().isAdmin()) {
            new Alert(Alert.AlertType.ERROR, "Akses Ditolak! Hanya Admin yang bisa mengunduh laporan laba.").show();
            return;
        }

        // 1. Ambil data agregat (Tanggal, Transaksi, Omset, Laba)
        List<Map<String, Object>> summaryData = saleDAO.getMonthlyReportData();

        if (summaryData != null && !summaryData.isEmpty()) {
            String fileName = "SUMMARY_KEUANGAN_" + java.time.LocalDate.now();

            // 2. Ekspor menggunakan utilitas generic kita
            CSVExporter.exportData(summaryData, fileName);

            new Alert(Alert.AlertType.INFORMATION, "Ringkasan Laba Rugi berhasil diekspor ke folder Downloads.").show();
        } else {
            new Alert(Alert.AlertType.WARNING, "Belum ada data transaksi untuk bulan ini.").show();
        }
    }
}