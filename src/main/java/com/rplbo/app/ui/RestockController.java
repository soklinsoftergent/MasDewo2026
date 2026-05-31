package com.rplbo.app.ui;

import com.rplbo.app.models.*;
import com.rplbo.app.dao.*;
import com.rplbo.app.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.util.*;

public class RestockController {

    @FXML private TableView<RestockTempItem> restockTable;
    @FXML private TableColumn<RestockTempItem, String> colItemName, colQuantity, colUnitCost, colSupplier, colSubtotal,colAction;
    @FXML private Label totalExpenseLabel;
    @FXML private TextField productSearchField;

    private final ObservableList<RestockTempItem> restockData = FXCollections.observableArrayList();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final RestockDAO restockDAO = new RestockDAO();

    @FXML
    public void initialize() {
        setupTableColumns();
        restockTable.setItems(restockData);
    }

    private void setupTableColumns() {
        colItemName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItem().getName()));

        colSupplier.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<Supplier> combo = new ComboBox<>();
            {
                combo.setItems(FXCollections.observableArrayList(supplierDAO.getAllSuppliers()));
                combo.setMaxWidth(Double.MAX_VALUE);
                combo.setOnAction(e -> {
                    if (getTableRow().getItem() != null) {
                        getTableRow().getItem().setSupplier(combo.getValue());
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(combo);
            }
        });

        colQuantity.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));

        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(
                FormatterUtil.formatCurrency(d.getValue().getSubtotal())
        ));
    }

    @FXML
    private void handleAddRestock() {
        List<Item> results = itemDAO.searchFast(productSearchField.getText());
        if (!results.isEmpty()) {
            Item item = results.get(0);
            restockData.add(new RestockTempItem(item, 1));
            updateTotal();
        }
    }

    @FXML
    private void handleExecuteRestock() {
        if (restockData.isEmpty()) return;

        User current = com.rplbo.app.services.UserSession.getInstance().getCurrentUser();
        double total = restockData.stream().mapToDouble(RestockTempItem::getSubtotal).sum();
        Expense expense = new Expense(current.getUserId(), total, "Restock Barang dari Supplier");

        List<ExpenseItem> items = new ArrayList<>();
        for (RestockTempItem ri : restockData) {
            items.add(new ExpenseItem(0, ri.getItem().getId(), ri.getQuantity(), ri.getItem().getPurchasePrice(), ri.getSubtotal()));
        }

        if (restockDAO.executeRestock(expense, items)) {
            new Alert(Alert.AlertType.INFORMATION, "Stok berhasil ditambahkan!").show();
            restockData.clear();
            updateTotal();
        }
    }

    private void updateTotal() {
        double total = restockData.stream().mapToDouble(RestockTempItem::getSubtotal).sum();
        totalExpenseLabel.setText(FormatterUtil.formatCurrency(total));
    }
}
