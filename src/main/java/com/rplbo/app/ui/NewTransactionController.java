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
import javafx.stage.Stage;
import java.util.*;

public class NewTransactionController {

    @FXML private TextField searchBar;
    @FXML private ListView<Item> searchResultsList;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colName, colPrice, colQty, colSubtotal;
    @FXML private Label totalLabel;

    private final ItemDAO itemDAO = new ItemDAO();
    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();
    private SalesController parentController;

    @FXML
    public void initialize() {
        setupCartTable();

        // --- FIX: Beritahu ListView cara menampilkan Item ---
        searchResultsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Tampilan: SKU - Nama Barang (Tersedia: 10)
                    setText(item.getSku() + " - " + item.getName() + " (Stok: " + item.getStock() + ")");

                    // Beri warna merah jika stok sangat sedikit
                    if (item.getStock() < 5) {
                        setStyle("-fx-text-fill: #ff8e8e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // --- TRIE SEARCH LOGIC ---
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                searchResultsList.getItems().clear();
            } else {
                // High-speed search
                List<Item> results = itemDAO.searchFast(newVal);
                searchResultsList.setItems(FXCollections.observableArrayList(results));
            }
        });

        // Add item to cart on double click from results
        searchResultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Item selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) addToCart(selected);
            }
        });
    }

    private void setupCartTable() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItem().getName()));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getItem().getSellingPrice())));
        colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(FormatterUtil.formatCurrency(d.getValue().getSubtotal())));
        cartTable.setItems(cartData);
    }

    private void addToCart(Item item) {
        if (item.getStock() <= 0) {
            new Alert(Alert.AlertType.WARNING, "Stok Habis!").show();
            return;
        }
        cartData.add(new CartItem(item, 1));
        updateTotal();
    }

    private void updateTotal() {
        double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();
        totalLabel.setText(FormatterUtil.formatCurrency(total));
    }

    @FXML
    private void handlePay() {
        if (cartData.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Keranjang belanja kosong!").show();
            return;
        }

        try {
            // 1. Ambil info User dari Session
            User currentUser = UserSession.getInstance().getCurrentUser();
            if (currentUser == null) throw new IllegalStateException("Sesi berakhir, silakan login kembali.");

            // 2. Hitung Total dan Profit untuk objek Sale
            double totalAmount = 0;
            double totalProfit = 0;

            for (CartItem ci : cartData) {
                totalAmount += ci.getSubtotal();
                // Profit = (Harga Jual - Harga Beli) * Quantity
                double itemProfit = (ci.getItem().getSellingPrice() - ci.getItem().getPurchasePrice()) * ci.getQuantity();
                totalProfit += itemProfit;
            }

            // 3. Buat Objek Sale (Gunakan Constructor Transaksi BARU)
            // Parameter: custId=1 (Guest), userId, ecomId=3 (Direct Store), total
            Sale sale = new Sale(1, currentUser.getUserId(), 3, totalAmount);
            sale.setProfit(totalProfit);
            sale.setPaid(true); // POS dianggap langsung lunas
            sale.setPaymentMethod("CASH");

            // 4. Ubah CartItem (UI) menjadi list SaleItem (Model)
            List<SaleItem> itemsToSave = new ArrayList<>();
            for (CartItem ci : cartData) {
                // saleId diisi 0 karena akan diupdate otomatis oleh SaleDAO setelah insert
                SaleItem si = new SaleItem(
                        0,
                        ci.getItem().getId(),
                        ci.getQuantity(),
                        ci.getItem().getSellingPrice(),
                        ci.getSubtotal()
                );
                itemsToSave.add(si);
            }

            // 5. EKSEKUSI TRANSAKSI ATOMIK LEWAT DAO
            // Ini akan mengurusi: simpan Sale, ambil ID, simpan SaleItems, potong Stok, & buat Log Audit.
            boolean success = SaleDAO.executeFullSale(sale, itemsToSave);

            if (success) {
                // Beri tahu UI Lead untuk refresh tabel utama
                if (parentController != null) {
                    parentController.loadSalesHistory();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Transaksi Berhasil disimpan!");
                alert.showAndWait();

                // Tutup jendela POS
                handleCancel();
            } else {
                new Alert(Alert.AlertType.ERROR, "❌ Transaksi Gagal! Periksa koneksi atau stok barang.").show();
            }

        } catch (Exception e) {
            System.err.println("🔥 Kesalahan saat Checkout: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
        }
    }

    @FXML private void handleCancel() {
        ((Stage) searchBar.getScene().getWindow()).close();
    }

    public void setParentController(SalesController parent) { this.parentController = parent; }
}