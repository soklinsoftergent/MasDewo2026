package com.rplbo.app.ui;

import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.models.Sale;
import com.rplbo.app.util.FormatterUtil;
import com.rplbo.app.util.ReceiptGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.Map;

public class SalesDetailController {
    @FXML private Label invoiceLabel, dateLabel, customerLabel, cashierLabel, platformLabel, totalLabel;
    @FXML private TableView<Map<String, Object>> detailTable;
    @FXML private TableColumn<Map<String, Object>, String> colItem, colQty, colPrice, colTotal;
    private Sale currentSale;
    private final SaleDAO saleDAO = new SaleDAO();

    public void setSaleData(Sale sale) {

        this.currentSale = sale;
        // 1. Set info dasar dari model
        invoiceLabel.setText(String.format("INV-%04d", sale.getSaleId()));
        dateLabel.setText(FormatterUtil.formatDate(sale.getCreatedAt()));
        totalLabel.setText(FormatterUtil.formatCurrency(sale.getTotalAmount()));

        // 2. Panggil DAO untuk mendapatkan info Nama Pelanggan & Kasir (JOIN)
        Map<String, Object> details = saleDAO.getSaleWithDetails(sale.getSaleId());
        if (details != null) {
            customerLabel.setText(String.valueOf(details.getOrDefault("customer_name", "Guest")));
            cashierLabel.setText(String.valueOf(details.get("cashier_name")));
            platformLabel.setText(String.valueOf(details.getOrDefault("platform_name", "Direct Store")));
        }

        // 3. Muat item-item di dalam tabel menggunakan JOIN yang sudah ada di SaleDAO
        List<Map<String, Object>> items = saleDAO.getItemsForSaleDetailed(sale.getSaleId());

        colItem.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("item_name"))));
        colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("quantity"))));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(
                FormatterUtil.formatCurrency(((Number) d.getValue().get("unit_price")).doubleValue())));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                FormatterUtil.formatCurrency(((Number) d.getValue().get("total_price")).doubleValue())));

        detailTable.getItems().setAll(items);
    }

    @FXML
    private void handlePrintReceipt() {
        if (currentSale != null) {
            // Ambil nama dari label UI atau dari detailInfo
            String cust = customerLabel.getText();
            String cash = cashierLabel.getText();

            ReceiptGenerator.generate(currentSale, cust, cash);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Struk telah dikirim ke folder Downloads.");
            alert.show();
        }
    }
}