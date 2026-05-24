package com.rplbo.app.ui;

import com.rplbo.app.dao.UserDAO;
import com.rplbo.app.models.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;

//import java.awt.*;
import java.io.IOException;
import java.util.List;

public class EmployeesController {

    @FXML private TextField searchField;
    @FXML private TableView<User> employeeTable;
    @FXML private TableColumn<User, String> colId, colUsername, colEmail, colRole, colStatus, colAction;
    @FXML private Label totalEmployeesLabel, activeStaffLabel;

    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 🛡️ SECURITY GUARD
        if (!com.rplbo.app.services.UserSession.getInstance().isAdmin()) {
            System.err.println("🚫 Security Breach: Non-admin tried to access Employee Page.");
            // Logic to redirect or clear content
            return;
        }
        setupTableColumns();
        loadEmployeeData();
        setupSearchFilter();
    }

    private void setupTableColumns() {
        // Bind columns to User model properties
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getUserId())));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserEmail()));

        // Custom Role column (Badge Style)
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAdmin() ? "Admin" : "Staff"));
        colRole.setCellFactory(column -> createBadgeCell("#d8c3ff", "#6d4ab5")); // Purple

        // Custom Status column (Badge Style)
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Aktif" : "Nonaktif"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    String color = item.equals("Aktif") ? "#79e07c" : "#ff8e8e";
                    String text = item.equals("Aktif") ? "#1f2937" : "white";
                    label.setStyle("-fx-background-color: " + color + "; -fx-text-fill: " + text + "; -fx-padding: 4 12; -fx-background-radius: 5; -fx-font-weight: bold;");
                    setGraphic(label);
                }
            }
        });

        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btnToggle = new Button();
            private final Button btnReset = new Button("Reset");
            private final HBox container = new HBox(8, btnToggle, btnReset);

            {
                // Styling tombol Reset
                btnReset.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
                btnReset.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) handleResetPassword(user);
                });

                // Styling HBox
                container.setPadding(new Insets(0, 5, 0, 5));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // Jika sel kosong atau data di baris tersebut null
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }

                // Ambil data User berdasarkan index baris saat ini
                User user = getTableView().getItems().get(getIndex());
                if (user == null) {
                    setGraphic(null);
                    return;
                }

                // Update teks dan gaya tombol Toggle secara dinamis
                if (user.isActive()) {
                    btnToggle.setText("PHK");
                    btnToggle.setStyle("-fx-background-color: transparent; -fx-border-color: #ff8e8e; -fx-text-fill: #ff8e8e; -fx-cursor: hand;");
                } else {
                    btnToggle.setText("Rekrut");
                    btnToggle.setStyle("-fx-background-color: transparent; -fx-border-color: #79e07c; -fx-text-fill: #79e07c; -fx-cursor: hand;");
                }

                // Set action klik
                btnToggle.setOnAction(e -> handleToggleStatus(user));

                setGraphic(container);
            }
        });

        employeeTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Jika klik 2x dan baris tidak kosong
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    User selectedUser = row.getItem();
                    openUserDetailWindow(selectedUser);
                }
            });
            return row;
        });

    }

    private void loadEmployeeData() {
        List<User> users = userDAO.getAllUsers();
        masterData.setAll(users);
        employeeTable.setItems(masterData);

        // Update Summary Labels
        totalEmployeesLabel.setText(String.valueOf(users.size()));
        long activeCount = users.stream().filter(User::isActive).count();
        activeStaffLabel.setText(String.valueOf(activeCount));
    }

    private void setupSearchFilter() {
        FilteredList<User> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getUsername().toLowerCase().contains(lowerCaseFilter) ||
                        user.getUserEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });
        employeeTable.setItems(filteredData);
    }

    private void handlePhk(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Nonaktifkan karyawan " + user.getUsername() + "?", ButtonType.OK, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                user.setActive(false); // ActiveRecord automatically updates DB
                loadEmployeeData(); // Refresh UI
            }
        });
    }

    // Helper for generating standard badges
    private TableCell<User, String> createBadgeCell(String bgColor, String textColor) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-padding: 4 12; -fx-background-radius: 5; -fx-font-weight: bold;");
                    setGraphic(label);
                }
            }
        };
    }

    private void handleToggleStatus(User user) {
        // 1. Debug Print (Untuk memastikan metode terpanggil)
        System.out.println("DEBUG: Menjalankan handleToggleStatus untuk " + user.getUsername());

        if (user.getUserId().equals(com.rplbo.app.services.UserSession.getInstance().getCurrentUser().getUserId())) {
            showError("Aksi Ditolak", "Anda tidak bisa memecat diri sendiri!");
            return;
        }

        boolean statusTarget = !user.isActive(); // Balikkan status saat ini
        String aksiStr = statusTarget ? "mengaktifkan kembali" : "menonaktifkan";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Karyawan");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin " + aksiStr + " karyawan: " + user.getUsername() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 2. Update status (ActiveRecord akan langsung UPDATE ke MySQL)
                user.setActive(statusTarget);

                // 3. Refresh UI
                employeeTable.refresh(); // Refresh warna badge di tabel
                loadEmployeeData();      // Update label total di bagian bawah

                System.out.println("✅ BERHASIL: Status " + user.getUsername() + " kini: " + (user.isActive() ? "Aktif" : "Nonaktif"));
            }
        });
    }

    private void handleResetPassword(User user) {
        String passwordDefault = "masdewo123";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Password");
        alert.setHeaderText("Reset password ke default?");
        alert.setContentText("Password " + user.getUsername() + " akan diubah menjadi: " + passwordDefault);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Hashing password baru
                String newHash = org.mindrot.jbcrypt.BCrypt.hashpw(passwordDefault, org.mindrot.jbcrypt.BCrypt.gensalt());

                // Simpan ke database via ActiveRecord
                user.setUserPasswdHash(newHash);

                Alert success = new Alert(Alert.AlertType.INFORMATION, "✅ Password berhasil direset!");
                success.show();
            }
        });
    }

    @FXML
    private void handleShowAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Karyawan Baru");
        dialog.setHeaderText(null);

        // Styling Dialog Pane (Dark Theme)
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1f1f1f; -fx-border-color: #4d667b; -fx-border-width: 2;");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Form Layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: #1f1f1f;");

        // Input Fields
        TextField usernameField = createStyledTextField("Username");
        TextField emailField = createStyledTextField("Email");
        TextField phoneField = createStyledTextField("Nomor Telepon");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 5;");

        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("Staff", "Admin"));
        roleCombo.getSelectionModel().selectFirst();
        roleCombo.setStyle("-fx-background-color: #3d5062; -fx-mark-color: white;");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        // Label Styling
        String labelStyle = "-fx-text-fill: #dbe7ef; -fx-font-weight: bold;";
        grid.add(createLabel("Username", labelStyle), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(createLabel("Email", labelStyle), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(createLabel("Telepon", labelStyle), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(createLabel("Password", labelStyle), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(createLabel("Peran", labelStyle), 0, 4);
        grid.add(roleCombo, 1, 4);

        dialogPane.setContent(grid);

        // Handle OK Button
        final Button btOk = (Button) dialogPane.lookupButton(ButtonType.OK);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            // Validasi Internal
            String user = usernameField.getText().trim();
            String mail = emailField.getText().trim();
            String pass = passwordField.getText();

            if (user.isEmpty() || mail.isEmpty() || pass.isEmpty()) {
                showError("Input Tidak Lengkap", "Semua kolom wajib diisi!");
                event.consume(); // Jangan tutup dialog
                return;
            }

            // Cek Keamanan Password & Format Email
            if (!com.rplbo.app.util.ValidationUtil.isValidEmail(mail)) {
                showError("Format Salah", "Email tidak valid!");
                event.consume();
                return;
            }

            if (!com.rplbo.app.util.ValidationUtil.isStrongPassword(pass)) {
                showError("Password Lemah", "Password harus 8+ karakter, ada angka, huruf besar/kecil, dan simbol!");
                event.consume();
                return;
            }

            // Cek Duplikat di Database
            if (userDAO.isUsernameOrEmailExists(user, mail)) {
                showError("Duplikat", "Username atau Email sudah terdaftar!");
                event.consume();
                return;
            }

            // SIMPAN!
            boolean isAdmin = roleCombo.getValue().equals("Admin");
            User newUser = new User(user, mail, pass, phoneField.getText(), isAdmin);
            if (newUser.save()) {
                System.out.println("✅ Karyawan berhasil dibuat.");
                loadEmployeeData(); // Refresh table
            } else {
                showError("Gagal", "Terjadi kesalahan saat menyimpan ke database.");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    // UI Helpers
    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #3d5062; -fx-text-fill: white; -fx-prompt-text-fill: #8a98a4; -fx-padding: 10; -fx-background-radius: 5;");
        return tf;
    }

    private Label createLabel(String text, String style) {
        Label l = new Label(text);
        l.setStyle(style);
        return l;
    }

    // Helper untuk menampilkan error
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }

    /**
     * Membuka jendela UserSettingsPage untuk user tertentu
     */
    private void openUserDetailWindow(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rplbo/app/pages/UserSettingsPage.fxml"));
            VBox root = loader.load();

            // Ambil controller-nya dan kirim data user-nya
            UserSettingsController controller = loader.getController();
            controller.setUserData(user);

            // Buat jendela baru (Popup)
            Stage stage = new Stage();
            stage.setTitle("Detail Karyawan: " + user.getUsername());
            stage.setScene(new javafx.scene.Scene(root));

            // Opsional: Buat agar jendela utama tidak bisa diklik sebelum popup ditutup
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Gagal Membuka Detail", "File FXML tidak ditemukan.");
        }
    }
}