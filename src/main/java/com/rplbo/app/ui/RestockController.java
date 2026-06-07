package com.rplbo.app.ui;

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
    private final SuppliersDAO suppliersDAO = new SuppliersDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final RestockDAO restockDAO = new RestockDAO();

    private final ContextMenu searchDropdown = new ContextMenu();
    private Item selectedItemFromSearch;

    @FXML
    public void initialize() {
        restockTable.setEditable(true);
        setupTableColumns();
        setupSearchAutoComplete();
        restockTable.setItems(restockData);
    }

    private void setupTableColumns() {
        colItemName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItem().getName()));

        // --- LOGIKA IN-LINE EDITING UNTUK QUANTITY ---
        colQuantity.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));

        // Gunakan TextField agar kolom bisa diketik
        colQuantity.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn());

        // Tangani saat user menekan ENTER setelah mengetik angka baru
        colQuantity.setOnEditCommit(event -> {
            RestockTempItem rowData = event.getRowValue();
            try {
                // Ambil string baru, ubah ke int
                int newQty = Integer.parseInt(event.getNewValue());

                if (newQty > 0) {
                    rowData.setQuantity(newQty);
                    updateTotal(); // Hitung ulang total pengeluaran di bawah
                } else {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                // Jika bukan angka atau <= 0, abaikan
                System.err.println("Input kuantitas tidak valid!");
            }
            // Refresh tabel agar subtotal di kolom sebelah ikut ter-update secara visual
            restockTable.refresh();
        });

        colUnitCost.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getItem().getPurchasePrice())));

        // Subtotal otomatis ter-update karena kita memanggil refresh() di atas
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getSubtotal())));
        // Supplier Dropdown Cell Factory
        colSupplier.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Supplier> combo = new ComboBox<>();

            {
                combo.setItems(FXCollections.observableArrayList(suppliersDAO.getAllSuppliers()));
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
        if (selectedItemFromSearch != null) {
            // 1. Cari apakah produk ini sudah ada di tabel?
            RestockTempItem existingItem = null;
            for (RestockTempItem current : restockData) {
                if (current.getItem().getId().equals(selectedItemFromSearch.getId())) {
                    existingItem = current;
                    break;
                }
            }

            if (existingItem != null) {
                // 2. Jika ADA: Tambah quantity-nya (misal tambah 1 tiap klik)
                existingItem.setQuantity(existingItem.getQuantity() + 1);

                // SANGAT PENTING: Karena kita pakai primitif 'int',
                // kita harus paksa tabel untuk gambar ulang barisnya
                restockTable.refresh();
            } else {
                // 3. Jika BELUM ADA: Tambah baris baru
                restockData.add(new RestockTempItem(selectedItemFromSearch, 1));
            }

            updateTotal();
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
