package com.rplbo.app.ui;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.*;
import com.rplbo.app.dao.*;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RestockController {

    @FXML
    private TableView<RestockTempItem> restockTable;
    @FXML
    private TableColumn<RestockTempItem, String> colItemName, colQuantity, colUnitCost, colSupplier, colSubtotal, colAction;
    @FXML
    private Label totalExpenseLabel;
    @FXML
    private TextField productSearchField;

    private final ObservableList<RestockTempItem> restockData = FXCollections.observableArrayList();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final RestockDAO restockDAO = new RestockDAO();

    private final ContextMenu searchDropdown = new ContextMenu();
    private Item selectedItemFromSearch;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchAutoComplete();
        restockTable.setItems(restockData);
    }

    private void setupTableColumns() {
        colItemName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItem().getName()));
        colQuantity.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colUnitCost.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getItem().getPurchasePrice())));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getSubtotal())));

        // Supplier Dropdown Cell Factory
        colSupplier.setCellFactory(col -> new TableCell<>() {
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
                if (empty || getTableRow().getItem() == null) setGraphic(null);
                else setGraphic(combo);
            }
        });

        // Delete Button Column
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("X");

            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff8e8e; -fx-font-weight: bold;");
                btn.setOnAction(e -> {
                    restockData.remove(getTableRow().getItem());
                    updateTotal();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void setupSearchAutoComplete() {
        productSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                searchDropdown.hide();
                return;
            }

            List<Item> results = itemDAO.searchFast(newVal);

            if (!results.isEmpty()) {
                showDropdown(results);
            } else {
                searchDropdown.hide();
            }
        });
    }

    private void showDropdown(List<Item> results) {
        // Jalankan di Platform.runLater agar UI tidak lag
        javafx.application.Platform.runLater(() -> {
            searchDropdown.getItems().clear();

            results.stream().limit(10).forEach(item -> {
                MenuItem mi = new MenuItem(item.getSku() + " - " + item.getName());
                mi.setOnAction(e -> {
                    productSearchField.setText(item.getName());
                    this.selectedItemFromSearch = item;
                    searchDropdown.hide();
                });
                searchDropdown.getItems().add(mi);
            });

            if (!searchDropdown.isShowing()) {
                // Tampilkan tepat di bawah TextField
                searchDropdown.show(productSearchField, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        });
    }

    @FXML
    private void handleAddToRestock() {
        if (selectedItemFromSearch == null) {
            // Fallback: if they didn't click dropdown but typed full name
            List<Item> items = itemDAO.searchFast(productSearchField.getText());
            if (!items.isEmpty()) selectedItemFromSearch = items.get(0);
        }

        if (selectedItemFromSearch != null) {
            // Prevent adding the same item twice in one list
            boolean exists = restockData.stream().anyMatch(ri -> ri.getItem().getId().equals(selectedItemFromSearch.getId()));

            if (!exists) {
                restockData.add(new RestockTempItem(selectedItemFromSearch, 1));
                updateTotal();
            }

            // Clear search for next item
            productSearchField.clear();
            selectedItemFromSearch = null;
        }
    }

    @FXML
    public void handleExecuteRestock(ActionEvent actionEvent) {
        if (restockData.isEmpty()) {
            Alert newAlert = new Alert(Alert.AlertType.INFORMATION);
            newAlert.setHeaderText(null);
            newAlert.setContentText("No restock data found");
            newAlert.show();
            return;
        }

        // 1. Gather Data from UI
        User current = UserSession.getInstance().getCurrentUser();
        double totalAmount = restockData.stream().mapToDouble(RestockTempItem::getSubtotal).sum();

        Expense expense = new Expense(current.getUserId(), totalAmount, "Restok Supplier");
        List<ExpenseItem> items = new ArrayList<>();

        for (RestockTempItem ri : restockData) {
            items.add(new ExpenseItem(0, ri.getItem().getId(), ri.getQuantity(),
                    ri.getItem().getPurchasePrice(), ri.getSubtotal()));
        }

        // 2. Hand over to the DAO Expert
        if (restockDAO.executeRestock(expense, items, current.getUserId())) {
            new Alert(Alert.AlertType.INFORMATION, "✅ Restok Berhasil!").show();
            restockData.clear();
            updateTotal();
            productSearchField.clear();
        } else {
            new Alert(Alert.AlertType.ERROR, "❌ Gagal memproses restok. Cek log!").show();
        }
    }

    // Di dalam RestockController.java

    private void updateTotal() {
        // 1. Hitung total dari semua baris di ObservableList
        double total = restockData.stream()
                .mapToDouble(RestockTempItem::getSubtotal)
                .sum();

        // 2. Format ke Rupiah menggunakan FormatterUtil
        // Jika FormatterUtil belum ada, gunakan idr.format(total)
        totalExpenseLabel.setText(com.rplbo.app.util.FormatterUtil.formatCurrency(total));

        System.out.println("📊 Total Pengeluaran Update: " + total);
    }
}
