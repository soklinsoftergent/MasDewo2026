package com.rplbo.app.ui;

import com.rplbo.app.dao.ItemDAO;
import com.rplbo.app.models.Item;
import com.rplbo.app.models.ItemType;
import com.rplbo.app.services.CloudSyncService;
import com.rplbo.app.util.CSVImporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InventoryController implements Refreshable {
    // --- FXML Bindings ---
    @FXML private TextField inventorySearchField;
    @FXML private Button addProductButton, editStockButton, inventoryFilterButton, syncToCloudButton;
    @FXML private TableView<Item> inventoryTable;
    @FXML private TableColumn<Item, String> inventoryNameColumn, inventoryCategoryColumn,
            inventoryStockColumn, inventoryBuyColumn,
            inventorySellColumn, inventoryStatusColumn,
            inventoryPlatformColumn;

    // --- Members ---
    private final ItemDAO itemDAO = new ItemDAO();
    private final ObservableList<Item> masterData = FXCollections.observableArrayList();
    private final DecimalFormat idr = new DecimalFormat("Rp #,###");

    // Simpan status filter saat ini
    private String currentCategoryFilter = "Semua Kategori";
    private String currentStatusFilter = "Semua Status";
    private FilteredList<Item> filteredData; // Pindahkan ke level class

    @FXML
    public void initialize() {
        setupTableColumns();
        refresh();
        setupSearchLogic();
        wireActions();
    }

    private void setupTableColumns() {
        // 1. Basic Mappings
        inventoryNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        inventoryStockColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStock())));

        // 2. Category Lookup (DAO Lead logic)
        inventoryCategoryColumn.setCellValueFactory(d -> {
            String catName = itemDAO.getTypeNameById(d.getValue().getItTyId());
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

        inventoryPlatformColumn.setCellValueFactory(d -> {
            Integer extId = d.getValue().getExternalId();
            if (extId != null) {
                return new SimpleStringProperty("Cloud ID: " + extId);
            }
            return new SimpleStringProperty("Lokal");
        });
    }

    @Override
    public void refresh() {
        List<Item> items = itemDAO.getAllItems();
        masterData.setAll(items);
        inventoryTable.setItems(masterData);
    }

    private void setupSearchLogic() {
        // Inisialisasi filteredData dengan masterData
        filteredData = new FilteredList<>(masterData, p -> true);
        inventoryTable.setItems(filteredData);

        // Listener untuk kolom pencarian
        inventorySearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }

    private void wireActions() {
        addProductButton.setOnAction(e -> handleAddProduct());
        editStockButton.setOnAction(e -> handleEditStock());
//        syncToCloudButton.setOnAction(e -> handleSyncToCloudAction());
    }

    @FXML
    private void handleSyncToCloudAction() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Peringatan", "Pilih produk terlebih dahulu dari tabel untuk di-sync!");
            return;
        }

        if (selected.getExternalId() != null) {
            showAlert("Info", "Produk ini sudah tersinkronisasi ke Cloud (ID: " + selected.getExternalId() + ").");
            return;
        }

        // Panggil fungsi sync yang sudah Anda buat
        handleSyncToCloud(selected);
    }


    @FXML
    private void handleAddProduct() {
        // 1. Setup Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Produk Baru");
        dialog.setHeaderText("Masukkan detail produk inventaris");

        // Styling (Dark Theme)
        dialog.getDialogPane().setStyle("-fx-background-color: #1f1f1f; -fx-border-color: #4d667b;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 2. Buat Form Input
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = createStyledField("Nama Produk (Unique)");
        TextField brandField = createStyledField("Brand (misal: Sony, Fotga)");
        TextField modelField = createStyledField("Model/Tipe (misal: A7iii, M42)");

        ComboBox<ItemType> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(itemDAO.getAllTypes()));
        typeCombo.setPromptText("Pilih Kategori");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white;");

        Spinner<Integer> stockSpinner = new Spinner<>(0, 9999, 0);
        stockSpinner.setEditable(true);
        stockSpinner.setMaxWidth(Double.MAX_VALUE);

        TextField buyPriceField = createStyledField("Harga Beli (Modal)");
        TextField sellPriceField = createStyledField("Harga Jual");

        // Tambahkan ke Grid
        String labelStyle = "-fx-text-fill: #dbe7ef; -fx-font-weight: bold;";
        grid.add(createLabel("Nama:", labelStyle), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Brand:", labelStyle), 0, 1);
        grid.add(brandField, 1, 1);
        grid.add(createLabel("Model:", labelStyle), 0, 2);
        grid.add(modelField, 1, 2);
        grid.add(createLabel("Kategori:", labelStyle), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(createLabel("Stok Awal:", labelStyle), 0, 4);
        grid.add(stockSpinner, 1, 4);
        grid.add(createLabel("Harga Beli:", labelStyle), 0, 5);
        grid.add(buyPriceField, 1, 5);
        grid.add(createLabel("Harga Jual:", labelStyle), 0, 6);
        grid.add(sellPriceField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // 3. Logika Simpan
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                // Validasi
                if (nameField.getText().isEmpty() || typeCombo.getValue() == null) {
                    throw new IllegalArgumentException("Nama dan Kategori wajib diisi!");
                }

                // Ambil data
                String name = nameField.getText();
                String brand = brandField.getText();
                String model = modelField.getText();
                int stock = stockSpinner.getValue();
                int typeId = typeCombo.getValue().getItTyId();
                String typeName = typeCombo.getValue().getItemTypeName();
                double buy = Double.parseDouble(buyPriceField.getText().replace(",", ""));
                double sell = Double.parseDouble(sellPriceField.getText().replace(",", ""));

                // BUAT OBJEK (Tanpa SKU karena akan di-generate otomatis)
                Item newItem = new Item(name, brand, model, stock, typeId, buy, sell);

                // SIMPAN KE DATABASE (Menggunakan logic 2-step SKU kita)
                if (newItem.save()) {
                    System.out.println("✅ Produk berhasil disimpan: " + newItem.getSku());
                    refresh(); // Refresh Tabel utama
                } else {
                    throw new Exception("Gagal menyimpan ke database. Cek apakah nama duplikat.");
                }

            } catch (NumberFormatException e) {
                showAlert("Input Error", "Harga harus berupa angka!");
                event.consume(); // Jangan tutup dialog
            } catch (Exception e) {
                showAlert("Error", e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    // Helpers
    private TextField createStyledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white; -fx-prompt-text-fill: #8a98a4; -fx-padding: 8;");
        return tf;
    }

    private Label createLabel(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
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

    private void handleSyncToCloud(Item item) {
        CloudSyncService syncService = new CloudSyncService();

        System.out.println("☁️ Menghubungkan ke server e-commerce...");

        syncService.syncItemToCloud(item).thenAccept(remoteId -> {
            if (remoteId != null) {
                // Update local DB dengan ID dari cloud (Gunakan ActiveRecord!)
                // Kita asumsikan ada method setExternalId di model Item
                item.setExternalId(remoteId);

                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION, "Berhasil Listing!\nCloud ID: " + remoteId).show();
                });
            } else {
                javafx.application.Platform.runLater(() -> {
                    new Alert(Alert.AlertType.ERROR, "Gagal sinkronisasi ke cloud.").show();
                });
            }
        });
    }

    @FXML
    private void handleBatchAdd() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Pilih File CSV Produk");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(inventoryTable.getScene().getWindow());

        if (selectedFile != null) {
            // 1. Parse File
            List<Item> itemsToImport = CSVImporter.parseItems(selectedFile);

            if (itemsToImport.isEmpty()) {
                showAlert("Error", "File kosong atau format salah!");
                return;
            }

            // 2. Konfirmasi
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Impor " + itemsToImport.size() + " produk sekaligus?", ButtonType.YES, ButtonType.NO);

            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                // 3. Jalankan Batch Insert
                if (itemDAO.batchInsert(itemsToImport)) {
                    showAlert("Sukses", "Berhasil mengimpor " + itemsToImport.size() + " produk!");
                    refresh(); // Refresh tabel UI
                } else {
                    showAlert("Gagal", "Terjadi kesalahan saat batch insert. Cek format data.");
                }
            }
        }
    }

    @FXML
    private void handleInventoryFilter() {
        // 1. Buat Dialog Kustom
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Filter Inventaris");
        dialog.setHeaderText("Pilih kriteria penyaringan barang");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #1f1f1f;");

        // 2. Siapkan Pilihan
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("Semua Kategori");
        itemDAO.getAllTypes().forEach(t -> categoryCombo.getItems().add(t.getItemTypeName()));
        categoryCombo.setValue(currentCategoryFilter);
        categoryCombo.setMaxWidth(Double.MAX_VALUE);
        categoryCombo.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Semua Status", "Tersedia", "Kritis", "Habis");
        statusCombo.setValue(currentStatusFilter);
        statusCombo.setMaxWidth(Double.MAX_VALUE);
        statusCombo.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white;");

        // Layout Form
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setPadding(new Insets(20));
        String labelStyle = "-fx-text-fill: #dbe7ef; -fx-font-weight: bold;";

        grid.add(createLabel("Kategori:", labelStyle), 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(createLabel("Status Stok:", labelStyle), 0, 1);
        grid.add(statusCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 3. Eksekusi Filter
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                currentCategoryFilter = categoryCombo.getValue();
                currentStatusFilter = statusCombo.getValue();
                applyFilters();

                // Beri visual feedback pada tombol filter
                if (currentCategoryFilter.equals("Semua Kategori") && currentStatusFilter.equals("Semua Status")) {
                    inventoryFilterButton.setText("Filter");
                } else {
                    inventoryFilterButton.setText("Filter: Aktif");
                }
            }
        });
    }

    /**
     * Logika Pusat untuk menggabungkan Search + Category + Status
     */
    private void applyFilters() {
        String searchKeyword = inventorySearchField.getText().toLowerCase().trim();

        filteredData.setPredicate(item -> {
            // A. Filter Pencarian (Nama atau SKU)
            boolean matchesSearch = searchKeyword.isEmpty() ||
                    item.getName().toLowerCase().contains(searchKeyword) ||
                    item.getSku().toLowerCase().contains(searchKeyword);

            // B. Filter Kategori
            String itemCat = itemDAO.getTypeNameById(item.getItTyId());
            boolean matchesCategory = currentCategoryFilter.equals("Semua Kategori") ||
                    itemCat.equals(currentCategoryFilter);

            // C. Filter Status Stok
            boolean matchesStatus = true;
            if (!currentStatusFilter.equals("Semua Status")) {
                int stock = item.getStock();
                switch (currentStatusFilter) {
                    case "Tersedia" -> matchesStatus = stock >= 5;
                    case "Kritis" -> matchesStatus = stock > 0 && stock < 5;
                    case "Habis" -> matchesStatus = stock <= 0;
                }
            }

            return matchesSearch && matchesCategory && matchesStatus;
        });
    }

    @FXML
    private void handleEditProduct() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getSellingPrice()));
        dialog.setTitle("Edit Harga");
        dialog.setHeaderText("Ubah Harga Jual: " + selected.getName());
        dialog.setContentText("Harga Baru (Rp):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double newPrice = Double.parseDouble(input);
                selected.setSellingPrice(newPrice); // ActiveRecord otomatis UPDATE ke DB
                inventoryTable.refresh();
                new Alert(Alert.AlertType.INFORMATION, "Harga berhasil diperbarui!").show();
            } catch (NumberFormatException e) {
                showAlert("Error", "Input harus berupa angka!");
            }
        });
    }

    @FXML
    private void handleAddCategory() {
        // 1. Inisialisasi Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Kategori Baru");
        dialog.setHeaderText("Masukkan nama kategori produk baru");

        // Styling Dialog
        dialog.getDialogPane().setStyle("-fx-background-color: #1f1f1f; -fx-border-color: #d8c3ff;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 2. Buat Form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = createStyledField("Contoh: Lensa, Tripod, dsb.");
        TextArea descField = new TextArea();
        descField.setPromptText("Deskripsi kategori (Opsional)");
        descField.setPrefRowCount(3);
        descField.setStyle("-fx-control-inner-background: #3d5062; -fx-text-fill: white; -fx-prompt-text-fill: #8a98a4;");

        String labelStyle = "-fx-text-fill: #dbe7ef; -fx-font-weight: bold;";
        grid.add(createLabel("Nama Kategori:", labelStyle), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Deskripsi:", labelStyle), 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 3. Logika Simpan
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();

            if (name.isEmpty()) {
                showAlert("Input Error", "Nama kategori tidak boleh kosong!");
                event.consume();
                return;
            }

            // Gunakan model ItemType yang sudah ActiveRecord
            ItemType newType = new ItemType(name, desc);

            if (newType.save()) {
                System.out.println("✅ Kategori baru berhasil dibuat: " + name);
                // Tidak perlu refresh tabel inventory, tapi data akan muncul saat klik "Tambah Produk"
            } else {
                showAlert("Error", "Gagal menyimpan kategori. Mungkin nama sudah ada?");
                event.consume();
            }
        });

        dialog.showAndWait();
    }
}