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
    @FXML private TableColumn<Item, String> inventorySkuColumn, inventoryNameColumn, inventoryCategoryColumn,
            inventoryStockColumn, inventoryBuyColumn,
            inventorySellColumn, inventoryStatusColumn,
            inventoryPlatformColumn, inventoryExternalIdColumn;

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
        // 1. Mapping SKU
        inventorySkuColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSku() != null ? d.getValue().getSku() : "-"
        ));
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
        // 5. Mapping External ID (Cloud ID)
        inventoryExternalIdColumn.setCellValueFactory(d -> {
            Integer extId = d.getValue().getExternalId();
            return new SimpleStringProperty(extId != null ? String.valueOf(extId) : "Lokal Only");
        });
        // Beri gaya italic/muted jika masih Lokal Only
        inventoryExternalIdColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.equals("Lokal Only")) {
                        setStyle("-fx-text-fill: #8a98a4; -fx-font-style: italic;");
                    } else {
                        setStyle("-fx-text-fill: #79e07c; -fx-font-weight: bold;"); // Warna hijau jika tersinkron
                    }
                }
            }
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
        TextField externalIdField = createStyledField("ID Marketplace/Cloud (Opsional)");

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
        grid.add(createLabel("External ID:", labelStyle), 0, 7);
        grid.add(externalIdField, 1, 7);

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
                String extIdRaw = externalIdField.getText().trim();
                Integer externalId = extIdRaw.isEmpty() ? null : Integer.parseInt(extIdRaw);

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

    @FXML
    private void handleEditStock() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Peringatan", "Pilih produk yang ingin diedit dari tabel!");
            return;
        }

        // 1. Setup Dialog (Tetap sama)
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Produk: " + selected.getSku());
        dialog.setHeaderText("Kosongkan kolom jika tidak ingin mengubah datanya");
        dialog.getDialogPane().setStyle("-fx-background-color: #1f1f1f; -fx-border-color: #4d667b;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 2. Buat Form Input
        // Kita berikan PromptText berupa data saat ini agar user tahu isinya apa
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nameField = createStyledField("");
        nameField.setPromptText(selected.getName()); // Tampilkan nama lama sebagai bantuan

        TextField brandField = createStyledField("");
        brandField.setPromptText(selected.getBrand());

        TextField modelField = createStyledField("");
        modelField.setPromptText(selected.getModel());

        ComboBox<ItemType> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(itemDAO.getAllTypes()));
        typeCombo.setPromptText("Kategori saat ini: " + itemDAO.getTypeNameById(selected.getItTyId()));
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        typeCombo.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white;");

        // Gunakan TextField biasa untuk harga agar bisa dikosongkan (Spinner sulit dikosongkan)
        TextField stockField = createStyledField("");
        stockField.setPromptText(String.valueOf(selected.getStock()));

        TextField buyPriceField = createStyledField("");
        buyPriceField.setPromptText(idr.format(selected.getPurchasePrice()));

        TextField sellPriceField = createStyledField("");
        sellPriceField.setPromptText(idr.format(selected.getSellingPrice()));

        TextField extIdField = createStyledField("");
        extIdField.setPromptText(selected.getExternalId() != null ? String.valueOf(selected.getExternalId()) : "N/A");

        // Tambahkan ke Grid (Sama seperti sebelumnya)
        String labelStyle = "-fx-text-fill: #dbe7ef; -fx-font-weight: bold;";
        grid.add(createLabel("Nama Produk:", labelStyle), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Brand:", labelStyle), 0, 1);
        grid.add(brandField, 1, 1);
        grid.add(createLabel("Model:", labelStyle), 0, 2);
        grid.add(modelField, 1, 2);
        grid.add(createLabel("Kategori:", labelStyle), 0, 3);
        grid.add(typeCombo, 1, 3);
        grid.add(createLabel("Stok Baru:", labelStyle), 0, 4);
        grid.add(stockField, 1, 4);
        grid.add(createLabel("Harga Beli:", labelStyle), 0, 5);
        grid.add(buyPriceField, 1, 5);
        grid.add(createLabel("Harga Jual:", labelStyle), 0, 6);
        grid.add(sellPriceField, 1, 6);
        grid.add(createLabel("External ID:", labelStyle), 0, 7);
        grid.add(extIdField, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // 3. Logika Update saat OK diklik
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                // LOGIKA DEFAULT: Jika kosong gunakan 'selected.getX()'

                String newName = nameField.getText().trim().isEmpty() ?
                        selected.getName() : nameField.getText().trim();

                String newBrand = brandField.getText().trim().isEmpty() ?
                        selected.getBrand() : brandField.getText().trim();

                String newModel = modelField.getText().trim().isEmpty() ?
                        selected.getModel() : modelField.getText().trim();

                int newTypeId = (typeCombo.getValue() == null) ?
                        selected.getItTyId() : typeCombo.getValue().getItTyId();

                int newStock = stockField.getText().trim().isEmpty() ?
                        selected.getStock() : Integer.parseInt(stockField.getText().trim());

                double newBuy = buyPriceField.getText().trim().isEmpty() ?
                        selected.getPurchasePrice() : Double.parseDouble(buyPriceField.getText().trim());

                double newSell = sellPriceField.getText().trim().isEmpty() ?
                        selected.getSellingPrice() : Double.parseDouble(sellPriceField.getText().trim());

                String extIdRaw = extIdField.getText().trim();
                Integer newExtId;

                if (extIdRaw.isEmpty()) {
                    // Jika input kosong, ambil nilai lama (bisa Integer null, tidak akan crash)
                    newExtId = selected.getExternalId();
                } else {
                    // Jika ada input, gunakan Integer.valueOf agar tetap menjadi Object Integer
                    try {
                        newExtId = Integer.valueOf(extIdRaw);
                    } catch (NumberFormatException e) {
                        newExtId = selected.getExternalId(); // Fallback jika input salah
                    }
                }

                // 4. Update Objek (ActiveRecord memproses ke DB)
                selected.setName(newName);
                selected.setBrand(newBrand);
                selected.setModel(newModel);
                selected.setItemTypeId(newTypeId);
                selected.setStock(newStock);
                selected.setPurchasePrice(newBuy);
                selected.setSellingPrice(newSell);
                selected.setExternalId(newExtId);

                // 5. Regenerasi SKU (Otomatis menyesuaikan jika ada perubahan)
                selected.applySmartSku();

                inventoryTable.refresh();
                System.out.println("✅ Update selesai untuk SKU: " + selected.getSku());

            } catch (NumberFormatException e) {
                showAlert("Input Error", "Stok dan Harga harus berupa angka!");
                event.consume();
            } catch (Exception e) {
                showAlert("Error", "Gagal: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
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