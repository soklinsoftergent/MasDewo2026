package com.rplbo.app.ui;

import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.models.Sale;
import com.rplbo.app.models.SaleItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import java.util.List;

public class SalesDetailController {
    @FXML private Label invoiceLabel, dateLabel, customerLabel, cashierLabel, totalLabel;
    @FXML private TableView<SaleItem> detailTable;
    @FXML private TableColumn<SaleItem, String> colItem, colQty, colPrice, colTotal;

    public void setSaleData(Sale sale) {
        invoiceLabel.setText("INV-" + String.format("%04d", sale.getSaleId()));
        totalLabel.setText("Rp. " + String.format("%,.0f", sale.getTotalAmount()));

        // Load items via DAO
        SaleDAO dao = new SaleDAO();
        List<SaleItem> items = dao.getItemsForSale(sale.getSaleId());

        // Setup columns
        colItem.setCellValueFactory(d -> new SimpleStringProperty("Item ID: " + d.getValue().getItemId())); // Simplified
        colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f", d.getValue().getUnitPrice())));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f", d.getValue().getTotalPrice())));

        detailTable.getItems().setAll(items);
    }
}