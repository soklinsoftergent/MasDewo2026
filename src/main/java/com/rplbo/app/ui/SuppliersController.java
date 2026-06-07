package com.rplbo.app.ui;

import com.rplbo.app.dao.SuppliersDAO;
import com.rplbo.app.models.Supplier;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class SuppliersController {
    @FXML private TextField searchField;
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, String> colId, colName, colPhone, colEmail, colAddress, colAction;

    private final SuppliersDAO suppliersDAO = new SuppliersDAO();
    private final ObservableList<Supplier> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSupplierId())));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhoneNumber()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnDel = new Button("Hapus");
            {
                btnDel.setStyle("-fx-background-color: transparent; -fx-border-color: #ff8e8e; -fx-text-fill: #ff8e8e;");
                btnDel.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDel);
            }
        });
    }

    private void loadData() {
        masterData.setAll(suppliersDAO.getAllSuppliers());

        FilteredList<Supplier> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, val) -> {
            filteredData.setPredicate(s -> val == null || val.isEmpty() ||
                    s.getName().toLowerCase().contains(val.toLowerCase()));
        });
        supplierTable.setItems(filteredData);
    }

    @FXML
    private void handleShowAddDialog() {
        // 1. Buat Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Supplier Baru");
        dialog.setHeaderText(null);

        // 2. Styling Dialog Pane (Dark Theme agar konsisten)
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1f1f1f; -fx-border-color: #4d667b; -fx-border-width: 2;");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 3. Buat Form Input (GridPane)
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: #1f1f1f;");

        TextField nameField = createStyledField("Nama Perusahaan / Supplier");
        TextField phoneField = createStyledField("Nomor Telepon");
        TextField emailField = createStyledField("Alamat Email");
        TextArea addressField = new TextArea();
        addressField.setPromptText("Alamat Lengkap...");
        addressField.setPrefRowCount(3);
        addressField.setStyle("-fx-control-inner-background: #3d5062; -fx-text-fill: white; -fx-prompt-text-fill: #8a98a4;");

        String labelStyle = "-fx-text-fill: #dbe7ef; -fx-font-weight: bold;";
        grid.add(new Label("Nama:", createLabelStyle(labelStyle)), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Telepon:", createLabelStyle(labelStyle)), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email:", createLabelStyle(labelStyle)), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Alamat:", createLabelStyle(labelStyle)), 0, 3);
        grid.add(addressField, 1, 3);

        dialogPane.setContent(grid);

        // 4. Logika Validasi & Simpan saat Tombol OK diklik
        final Button btOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            // Validasi Sederhana
            if (name.isEmpty()) {
                showError("Input Salah", "Nama Supplier wajib diisi!");
                event.consume(); // Menahan dialog agar tidak tertutup
                return;
            }

            if (!email.isEmpty() && !com.rplbo.app.util.ValidationUtil.isValidEmail(email)) {
                showError("Format Salah", "Email tidak valid!");
                event.consume();
                return;
            }

            // SIMPAN KE DATABASE (Menggunakan ActiveRecord)
            Supplier newSupplier = new Supplier(
                    name,
                    phoneField.getText().trim(),
                    email,
                    addressField.getText().trim()
            );

            if (newSupplier.save()) {
                System.out.println("✅ Supplier berhasil disimpan: " + name);
                loadData(); // Refresh tabel utama
            } else {
                showError("Database Error", "Gagal menyimpan data ke MySQL.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

// --- UI Helpers agar kode bersih ---

    private TextField createStyledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(300);
        tf.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white; -fx-prompt-text-fill: #8a98a4; -fx-padding: 10; -fx-background-radius: 5;");
        return tf;
    }

    private Label createLabelStyle(String style) {
        Label l = new Label();
        l.setStyle(style);
        return l;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void handleDelete(Supplier s) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Hapus supplier " + s.getName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                // Delete logic (DAO Lead must provide delete method)
                if (suppliersDAO.deleteSupplier(s.getSupplierId())) {
                    loadData();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Gagal menghapus! Supplier mungkin masih terhubung ke barang.").show();
                }
            }
        });
    }
}