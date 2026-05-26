package com.rplbo.app.ui;

import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.models.Sale;
import com.rplbo.app.util.FormatterUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;

public class SalesController {
    @FXML private TableView<Map<String, Object>> salesTable;
    @FXML private TableColumn<Map<String, Object>, String> salesInvoiceColumn, salesCustomerColumn,
            salesTotalColumn, salesCashierColumn, salesStatusColumn;

    private final SaleDAO saleDAO = new SaleDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadSalesHistory();
        setupRowListener();
    }

    private void setupTableColumns() {
        salesInvoiceColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("INV-%04d", (Integer) d.getValue().get("sale_id"))));

        salesCustomerColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getOrDefault("customer_name", "Guest"))));

        salesTotalColumn.setCellValueFactory(d -> new SimpleStringProperty(
                FormatterUtil.formatCurrency(((Number) d.getValue().get("total_amount")).doubleValue())));

        salesCashierColumn.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().get("cashier_name"))));

        salesStatusColumn.setCellValueFactory(d -> {
            boolean isPaid = (Boolean) d.getValue().get("is_paid");
            return new SimpleStringProperty(isPaid ? "Selesai" : "Pending");
        });
    }

    public void loadSalesHistory() {
        List<Map<String, Object>> data = saleDAO.getAllSalesDetailed();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo.app/pages/SalesDetailPage.fxml"));
            VBox root = loader.load();

            SalesDetailController controller = loader.getController();
            // Kita bungkus Map kembali ke Model Sale untuk dikirim ke Detail
            controller.setSaleData(new Sale(rowData));

            Stage stage = new Stage();
            stage.setTitle("Detail Transaksi - " + rowData.get("sale_id"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewTransaction() {
        // Panggil Dialog POS yang sudah Anda buat sebelumnya
        System.out.println("Membuka Layar Kasir...");
    }
}