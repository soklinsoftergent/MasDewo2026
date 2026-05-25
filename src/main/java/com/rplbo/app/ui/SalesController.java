package com.rplbo.app.ui;

import com.rplbo.app.dao.ItemDAO;
import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.models.*;
import com.rplbo.app.services.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;

public class SalesController {
    @FXML private TextField searchProductField;
    @FXML
    private TableView<CartItem> cartTable;
    @FXML private Label totalLabel;

    private final ObservableList<CartItem> cartData = FXCollections.observableArrayList();
    private final ItemDAO itemDAO = new ItemDAO();

    @FXML
    public void handleSearch() {
        String query = searchProductField.getText();
        List<Item> results = itemDAO.searchFast(query);

        if (results.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Barang tidak ditemukan!").show();
            return;
        }

        // Tampilkan pilihan jika ada lebih dari 1 hasil
        ChoiceDialog<Item> dialog = new ChoiceDialog<>(results.get(0), results);
        dialog.setTitle("Pilih Produk");
        dialog.setHeaderText("Ditemukan " + results.size() + " produk matching");
        dialog.setContentText("Pilih produk:");

        dialog.showAndWait().ifPresent(selectedItem -> {
            // Minta jumlah (Quantity)
            TextInputDialog qtyDialog = new TextInputDialog("1");
            qtyDialog.setTitle("Jumlah");
            qtyDialog.setHeaderText("Beli " + selectedItem.getName());
            qtyDialog.setContentText("Masukkan jumlah unit:");

            qtyDialog.showAndWait().ifPresent(qtyStr -> {
                try {
                    int qty = Integer.parseInt(qtyStr);
                    if (qty > selectedItem.getStock()) {
                        new Alert(Alert.AlertType.ERROR, "Stok tidak cukup!").show();
                    } else {
                        // Tambah ke list internal (ObservableList)
                        cartData.add(new CartItem(selectedItem, qty));
                        updateTotalLabel();
                    }
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Input harus angka!").show();
                }
            });
        });
    }

    // Di dalam method updateTotalLabel() atau initialize()
    private void updateTotalLabel() {
        double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();
        // Panggil dari Utility
        totalLabel.setText(com.rplbo.app.util.FormatterUtil.formatCurrency(total));
    }
    
    @FXML
    public void handleCheckout() {
        if (cartData.isEmpty()) return;

        // 1. Siapkan data Sale
        User current = UserSession.getInstance().getCurrentUser();
        double total = cartData.stream().mapToDouble(CartItem::getSubtotal).sum();
        Sale newSale = new Sale(1, current.getUserId(), 1, total);

        // 2. Siapkan list SaleItem
        List<SaleItem> saleItems = new ArrayList<>();
        for (CartItem ci : cartData) {
            saleItems.add(new SaleItem(0, ci.getItem().getId(), ci.getQuantity(),
                    ci.getItem().getSellingPrice(), ci.getSubtotal()));
        }

        // 3. PANGGIL BOSS FIGHT (Atomic Transaction)
        boolean success = SaleDAO.executeFullSale(newSale, saleItems);

        if (success) {
            // 4. Update Kas (Financial System)
            new KasTransaction(current.getUserId(), "INCOME", total, "Penjualan Langsung").save();
            KasBalance kb = new KasBalance(0); // Ambil dari DB
            kb.refresh();
            kb.incrementBalance(total);

            cartData.clear();
            new Alert(Alert.AlertType.INFORMATION, "✅ Transaksi Berhasil!").show();
        } else {
            new Alert(Alert.AlertType.ERROR, "❌ Transaksi Gagal (Stok Tidak Cukup)").show();
        }
    }
}