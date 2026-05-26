package com.rplbo.app.ui;

import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.models.Sale;
import com.rplbo.app.util.FormatterUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.Map;

public class SalesDetailController {
    @FXML private Label invoiceLabel, dateLabel, customerLabel, cashierLabel, totalLabel;
    @FXML private TableView<Map<String, Object>> detailTable;
    @FXML private TableColumn<Map<String, Object>, String> colItem, colQty, colPrice, colTotal;

    public void setSaleData(Sale sale) {
        invoiceLabel.setText(String.format("INV-%04d", sale.getSaleId()));
        dateLabel.setText(FormatterUtil.formatDate(sale.getCreatedAt()));
        totalLabel.setText(FormatterUtil.formatCurrency(sale.getTotalAmount()));

        SaleDAO dao = new SaleDAO();
        List<Map<String, Object>> items = dao.getItemsForSaleDetailed(sale.getSaleId());

        // Setup columns menggunakan Map key dari hasil JOIN
        colItem.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("item_name"))));
        colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().get("quantity"))));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(
                FormatterUtil.formatCurrency(((Number) d.getValue().get("unit_price")).doubleValue())));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                FormatterUtil.formatCurrency(((Number) d.getValue().get("total_price")).doubleValue())));

        detailTable.getItems().setAll(items);
    }
}