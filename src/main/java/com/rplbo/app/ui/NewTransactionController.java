package com.rplbo.app.ui;

import com.rplbo.app.dao.*;
import com.rplbo.app.models.*;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.util.FormatterUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.*;

public class NewTransactionController {

    @FXML public TextField custNameField, custPhoneField, custAddressField, custEmailField;
    @FXML private ComboBox<ECommerce> ecomCombo;
    @FXML private TextField searchBar;
    @FXML private ListView<Item> searchResultsList;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colName, colPrice, colQty, colSubtotal;
    @FXML private Label totalLabel;

    private final ItemDAO itemDAO = new ItemDAO();
    private final SaleDAO saleDAO = new SaleDAO(); // Panggil DAO-nya
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();
    private SalesController parentController;

    @FXML
    public void initialize() {
        setupCartTable();

        // --- GANTI DBConnection DENGAN SaleDAO ---
        List<ECommerce> platforms = saleDAO.getAllECommerces();
        ecomCombo.setItems(FXCollections.observableArrayList(platforms));
        if (!platforms.isEmpty()) ecomCombo.getSelectionModel().select(0);

        // --- CellFactory untuk ListView (Nama Barang) ---
        searchResultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSku() + " - " + item.getName() + " (Stok: " + item.getStock() + ")");
                    if (item.getStock() < 5) setStyle("-fx-text-fill: #ff8e8e; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: white;");
                }
            }
        });

        // Search logic (Trie)
        searchBar.textProperty().addListener((obs, old, val) -> {
            if (val.isEmpty()) searchResultsList.getItems().clear();
            else searchResultsList.setItems(FXCollections.observableArrayList(itemDAO.searchFast(val)));
        });

        // Double click to add
        searchResultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Item selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) addToCart(selected);
            }
        });
    }

    @FXML
    private void handlePay() {
        if (cartData.isEmpty()) return;

        try {
            User currentUser = UserSession.getInstance().getCurrentUser();
            ECommerce selectedPlatform = ecomCombo.getValue();

            int ecommerceId;
            if (selectedPlatform != null) {
                ecommerceId = selectedPlatform.getEcomId();
            } else {
                // If nothing is selected, ensure "Kasir" exists and get its ID
                ecommerceId = saleDAO.ensureECommerce("Kasir");
            }

            // 1. Logika Pelanggan lewat SaleDAO
            int customerId = saleDAO.ensureCustomer(
                    custNameField.getText(),
                    custPhoneField.getText(),
                    custEmailField.getText(),
                    custAddressField.getText()
            );

            // 2. Kalkulasi Total & Profit
            double totalAmount = 0;
            double totalProfit = 0;
            for (CartItem ci : cartData) {
                totalAmount += ci.getSubtotal();
                totalProfit += (ci.getItem().getSellingPrice() - ci.getItem().getPurchasePrice()) * ci.getQuantity();
            }

            // 3. Rakit Objek Sale
            Sale sale = new Sale(customerId, currentUser.getUserId(), ecommerceId, totalAmount);
            sale.setProfit(totalProfit);
            sale.setPaid(true);
            sale.setPaymentMethod("CASH");

            // 4. Rakit List SaleItem
            List<SaleItem> itemsToSave = new ArrayList<>();
            for (CartItem ci : cartData) {
                itemsToSave.add(new SaleItem(0, ci.getItem().getId(), ci.getQuantity(),
                        ci.getItem().getSellingPrice(), ci.getSubtotal()));
            }

            // 5. Eksekusi Transaksi Atomik
            if (SaleDAO.executeFullSale(sale, itemsToSave)) {
                if (parentController != null) parentController.loadSalesHistory();

                // Refresh data global (Charts, Badge, dll)
                com.rplbo.app.util.DataStateSignal.fireAll();

                new Alert(Alert.AlertType.INFORMATION, "✅ Transaksi Berhasil!").showAndWait();
                handleCancel();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Gagal: " + e.getMessage()).show();
        }
    }
    private void setupCartTable() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItem().getName()));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getItem().getSellingPrice())));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getSubtotal())));
        cartTable.setItems(cartData);
    }

    private void addToCart(Item item) {
        if (item.getStock() <= 0) return;
        cartData.add(new CartItem(item, 1));
        updateTotal();
    }

    private void updateTotal() {
        double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();
        totalLabel.setText(FormatterUtil.formatCurrency(total));
    }

    @FXML private void handleCancel() {
        ((Stage) searchBar.getScene().getWindow()).close();
    }

    public void setParentController(SalesController p) { this.parentController = p; }
}