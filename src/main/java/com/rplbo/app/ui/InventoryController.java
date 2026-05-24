package com.rplbo.app.ui;

import com.rplbo.app.dao.ItemDAO;
import com.rplbo.app.models.Item;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.text.DecimalFormat;
import java.util.List;

public class InventoryController {

    // --- FXML Bindings ---
    @FXML private TextField inventorySearchField;
    @FXML private Button addProductButton, editStockButton, inventoryFilterButton;
    @FXML private TableView<Item> inventoryTable;
    @FXML private TableColumn<Item, String> inventoryNameColumn, inventoryCategoryColumn,
            inventoryStockColumn, inventoryBuyColumn,
            inventorySellColumn, inventoryStatusColumn,
            inventoryPlatformColumn;

    // --- Members ---
    private final ItemDAO itemDAO = new ItemDAO();
    private final ObservableList<Item> masterData = FXCollections.observableArrayList();
    private final DecimalFormat idr = new DecimalFormat("Rp #,###");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
        setupSearchLogic();
        wireActions();
    }

    private void setupTableColumns() {
        // 1. Basic Mappings
        inventoryNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        inventoryStockColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStock())));

        // 2. Category Lookup (DAO Lead logic)
        inventoryCategoryColumn.setCellValueFactory(d -> {
            String catName = itemDAO.getTypeNameById(d.getValue().getItemTypeId());
            return new SimpleStringProperty(catName);
        });

        // 3. Currency Formatting
        inventoryBuyColumn.setCellValueFactory(d -> new SimpleStringProperty(idr.format(d.getValue().getPurchasePrice())));
        inventorySellColumn.setCellValueFactory(d -> new SimpleStringProperty(idr.format(d.getValue().getSellingPrice())));

        // 4. Status Badge (Logic for Habis/Kritis/Tersedia)
        inventoryStatusColumn.setCellValueFactory(d -> {
            int stock = d.getValue().getStock();
            if (stock <= 0) return new SimpleStringProperty("Habis");
            if (stock < 5) return new SimpleStringProperty("Kritis");
            return new SimpleStringProperty("Tersedia");
        });
        inventoryStatusColumn.setCellFactory(col -> createStatusBadgeCell());

        // 5. Platform Placeholder (Can be expanded if platform logic is added)
        inventoryPlatformColumn.setCellValueFactory(d -> new SimpleStringProperty("Store"));
    }

    private void loadData() {
        List<Item> items = itemDAO.getAllItems();
        masterData.setAll(items);
        inventoryTable.setItems(masterData);
    }

    private void setupSearchLogic() {
        // Connect the search bar to the list using a FilteredList
        FilteredList<Item> filteredData = new FilteredList<>(masterData, p -> true);

        inventorySearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(item -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return item.getName().toLowerCase().contains(lower) ||
                        item.getSku().toLowerCase().contains(lower);
            });
        });

        inventoryTable.setItems(filteredData);
    }

    private void wireActions() {
        addProductButton.setOnAction(e -> handleAddProduct());
        editStockButton.setOnAction(e -> handleEditStock());
    }

    private void handleAddProduct() {
        // Logic for opening the Add Dialog
        System.out.println("Opening Add Product Dialog...");
    }

    private void handleEditStock() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Peringatan", "Pilih produk terlebih dahulu!");
            return;
        }

        // Simple InputDialog to update stock
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getStock()));
        dialog.setTitle("Edit Stok");
        dialog.setHeaderText("Update stok untuk: " + selected.getName());
        dialog.setContentText("Jumlah stok baru:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int newStock = Integer.parseInt(input);
                selected.setStock(newStock); // ActiveRecord handles DB Update
                inventoryTable.refresh();
            } catch (NumberFormatException e) {
                showAlert("Error", "Input harus berupa angka!");
            }
        });
    }

    // --- UI Helper: Badge Styling ---
    private TableCell<Item, String> createStatusBadgeCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    String style = switch (status) {
                        case "Habis" -> "-fx-background-color: #7f1d1d; -fx-text-fill: white;";
                        case "Kritis" -> "-fx-background-color: #ffd15a; -fx-text-fill: #6b4f00;";
                        default -> "-fx-background-color: #69d26d; -fx-text-fill: #1f2937;";
                    };
                    badge.setStyle(style + " -fx-padding: 4 10; -fx-background-radius: 5; -fx-font-weight: bold;");
                    setGraphic(badge);
                }
            }
        };
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }
}