package com.rplbo.app.ui;

import com.rplbo.app.dao.ItemDAO;
import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.models.*;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.util.FormatterUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SalesController {
    @FXML private TableView<Map<String, Object>> salesTable;
    @FXML private TableColumn<Map<String, Object>, String> salesInvoiceColumn, salesCustomerColumn,
            salesTotalColumn, salesCashierColumn, salesStatusColumn, salesProductsColumn, salesPlatformColumn;

    private final SaleDAO saleDAO = new SaleDAO();
    private final ItemDAO itemDAO = new ItemDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadSalesHistory();
        setupTableClickListener();
        setupRowListener();
    }

    private void setupTableColumns() {
        // No. Invoice
        salesInvoiceColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("INV-%04d", (Integer) d.getValue().get("sale_id"))));

        // Pelanggan
        salesCustomerColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("customer_name", "Guest"))));

        // Produk (Jumlah Item)
        salesProductsColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().get("item_count") + " Item"));

        // Total
        salesTotalColumn.setCellValueFactory(d -> new SimpleStringProperty(
                com.rplbo.app.util.FormatterUtil.formatCurrency(((Number) d.getValue().get("total_amount")).doubleValue())));

        // Platform
        salesPlatformColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("platform_name", "Direct"))));

        // Kasir
        salesCashierColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().get("cashier_name"))));

        // Status (Badge Styling)
        salesStatusColumn.setCellValueFactory(d -> {
            // Handle database boolean/tinyint safely
            Object isPaidObj = d.getValue().get("is_paid");
            boolean isPaid = false;
            if (isPaidObj instanceof Boolean) isPaid = (Boolean) isPaidObj;
            else if (isPaidObj instanceof Number) isPaid = ((Number) isPaidObj).intValue() == 1;

            return new SimpleStringProperty(isPaid ? "Selesai" : "Pending");
        });

        // Opsional: Tambahkan warna pada status
        salesStatusColumn.setCellFactory(col -> createStatusBadgeCell());
    }

    private TableCell<Map<String, Object>, String> createStatusBadgeCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // 1. Buat Label baru sebagai badge
                    Label badge = new Label(status.toUpperCase());

                    // 2. Tentukan warna berdasarkan teks status
                    String style;
                    if (status.equalsIgnoreCase("Selesai")) {
                        // Hijau (Sukses) - Mengikuti palette Pemasukan
                        style = "-fx-background-color: #79e07c; -fx-text-fill: #1f2937;";
                    } else if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Proses")) {
                        // Kuning/Oren (Menunggu)
                        style = "-fx-background-color: #ffd15a; -fx-text-fill: #6b4f00;";
                    } else {
                        // Merah (Batal) - Mengikuti palette Pengeluaran
                        style = "-fx-background-color: #ff8e8e; -fx-text-fill: white;";
                    }

                    // 3. Tambahkan styling padding dan radius agar berbentuk pill/lonjong
                    badge.setStyle(style + " -fx-padding: 4 12; -fx-background-radius: 8; -fx-font-weight: 800; -fx-font-size: 10px;");

                    // 4. Masukkan badge ke dalam sel tabel
                    setGraphic(badge);
                    setText(null);
                }
            }
        };
    }

    // Perbarui method loadSalesHistory di SalesController.java
    public void loadSalesHistory() {
        User current = UserSession.getInstance().getCurrentUser();
        if (current == null) return;

        List<Map<String, Object>> data;
        if (current.isAdmin()) {
            data = saleDAO.getAllSalesDetailed(); // Admin lihat semua
        } else {
            data = saleDAO.getSalesByUserIdDetailed(current.getUserId()); // Staff lihat punya sendiri
        }

        salesTable.setItems(FXCollections.observableArrayList(data));
    }

    private void setupRowListener() {
        salesTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Map<String, Object> rowData = row.getItem();
                    showSaleDetails(rowData);
                }
            });
            return row;
        });
    }

    private void showSaleDetails(Map<String, Object> rowData) {
        try {
            // 1. Pastikan path dimulai dengan "/" dan tidak ada typo folder
            String fxmlPath = "/com/rplbo/app/pages/SalesDetailPage.fxml";
            java.net.URL resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                System.err.println("❌ ERROR: File tidak ditemukan di: " + fxmlPath);
                // Coba alternatif folder jika Anda memindahkannya ke folder 'ui'
                resource = getClass().getResource("/com/rplbo/app/ui/SalesDetailPage.fxml");
            }

            if (resource == null) {
                throw new IllegalStateException("Lokasi file FXML tidak ditemukan. Periksa folder resources!");
            }

            FXMLLoader loader = new FXMLLoader(resource);
            VBox root = loader.load();

            SalesDetailController controller = loader.getController();
            controller.setSaleData(new Sale(rowData));

            Stage stage = new Stage();
            stage.setTitle("Detail Transaksi - " + rowData.get("sale_id"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Gagal memuat file FXML!");
            e.printStackTrace();
        }
    }

    /**
     * Mendeteksi klik ganda pada baris tabel untuk membuka detail
     */
    private void setupTableClickListener() {
        salesTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Map<String, Object> rowData = row.getItem();
                    openSalesDetail(rowData);
                }
            });
            return row;
        });
    }

    private void openSalesDetail(Map<String, Object> rowData) {
        try {
            // 1. Load FXML Detail
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/pages/SalesDetailPage.fxml"));
            Parent root = loader.load();

            // 2. Ambil Controller Detail dan kirim data
            SalesDetailController controller = loader.getController();

            // Konversi Map kembali ke model Sale agar Controller Detail bisa membacanya
            Sale saleModel = new Sale(rowData);
            controller.setSaleData(saleModel);

            // Tambahan: Isi label pelanggan dan kasir yang belum ada di setSaleData bawaan Anda
            // (Kita modifikasi sedikit setSaleData di bawah)

            // 3. Tampilkan di jendela popup (Modal)
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Detail Transaksi - INV-" + String.format("%04d", saleModel.getSaleId()));
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat halaman detail!").show();
        }
    }

    @FXML
    private void handleNewTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/pages/NewTransactionDialog.fxml"));
            VBox root = loader.load();

            NewTransactionController controller = loader.getController();
            controller.setParentController(this); // Allow the dialog to refresh our table

            Stage stage = new Stage();
            stage.setTitle("NeoMasDewo - POS");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}