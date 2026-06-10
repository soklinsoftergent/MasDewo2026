package com.rplbo.app.ui;

import com.rplbo.app.models.User;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.util.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class UserSettingsController {

    @FXML private Button logoutButton;
    @FXML private Label avatarLabel, displayNameLabel, displayEmailLabel, roleBadge;
    @FXML private TextField usernameField, emailField, phoneField;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;

    private User targetUser; // User yang sedang ditampilkan/diedit

    @FXML
    public void initialize() {
        // Jika dipanggil secara normal (dari sidebar), tampilkan user yang sedang login
        if (targetUser == null) {
            setUserData(UserSession.getInstance().getCurrentUser());
        }
    }

    /**
     * Metode untuk "menyuntikkan" data user dari controller lain (misal dari EmployeesController)
     */
    public void setUserData(User user) {
        this.targetUser = user;

        // Isi Label
        displayNameLabel.setText(user.getUsername());
        displayEmailLabel.setText(user.getUserEmail());
        avatarLabel.setText(user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase());
        roleBadge.setText(user.isAdmin() ? "ADMIN" : "STAFF");

        // Isi Form
        usernameField.setText(user.getUsername());
        emailField.setText(user.getUserEmail());
        phoneField.setText(user.getUserPhoneNumber());

        // 3. LOGIKA VISIBILITAS LOGOUT (Requirement Anda)
        User loggedInUser = UserSession.getInstance().getCurrentUser();
        if (loggedInUser != null && !loggedInUser.getUserId().equals(user.getUserId())) {
            // Jika user yang dibuka BUKAN user yang sedang login saat ini
            logoutButton.setVisible(false);
            logoutButton.setManaged(false);
        } else {
            logoutButton.setVisible(true);
            logoutButton.setManaged(true);
        }
    }

    /**
     * Menyimpan perubahan profil (Username, Email, HP)
     */
    @FXML
    private void handleSaveProfile() {
        try {
            String newUsername = usernameField.getText().trim();
            String newEmail = emailField.getText().trim();
            String newPhone = phoneField.getText().trim();

            // Validasi
            if (newUsername.isEmpty() || newEmail.isEmpty()) {
                throw new IllegalArgumentException("Username dan Email tidak boleh kosong!");
            }
            if (!ValidationUtil.isValidEmail(newEmail)) {
                throw new IllegalArgumentException("Format email tidak valid!");
            }

            // Update menggunakan ActiveRecord (Otomatis Sync ke DB)
            targetUser.setUsername(newUsername);
            targetUser.setEmail(newEmail);
            targetUser.setPhoneNumber(newPhone);

            // Update UI Header
            displayNameLabel.setText(newUsername);
            displayEmailLabel.setText(newEmail);

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Profil berhasil diperbarui!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Update", e.getMessage());
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {

        try {

            // 1. Destroy session
            UserSession.getInstance().logout();

            System.out.println("🚪 Logging out user...");

            // 2. Load login page FIRST
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rplbo/app/pages/LoginWindow.fxml")
            );

            Parent loginRoot = loader.load();

            Stage loginStage = new Stage();

            loginStage.setTitle("MasDewo Store - Login");
            loginStage.setScene(new Scene(loginRoot));

            loginStage.setResizable(false);
            loginStage.sizeToScene();
            loginStage.centerOnScreen();

            // 3. Copy all currently opened windows
            java.util.List<javafx.stage.Window> windows =
                    new java.util.ArrayList<>(javafx.stage.Window.getWindows());

            // 4. Close EVERYTHING
            for (javafx.stage.Window window : windows) {

                // Don't accidentally close the new login window
                if (window != loginStage) {

                    if (window instanceof Stage stage) {
                        stage.close();
                    }

                }
            }

            // 5. Show fresh login page
            loginStage.show();

            System.out.println("✅ All application windows closed.");

        } catch (IOException e) {

            System.err.println("❌ Failed to return to login page.");
            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Logout Error",
                    "Tidak bisa membuka halaman login."
            );
        }
    }

    /**
     * Menyimpan perubahan password dengan verifikasi BCrypt
     */
    @FXML
    private void handleUpdatePassword() {
        try {
            String currentPass = currentPasswordField.getText();
            String newPass = newPasswordField.getText();
            String confirmPass = confirmPasswordField.getText();

            // 1. Cek apakah ini user sendiri? Jika ya, wajib isi password lama.
            User loggedIn = UserSession.getInstance().getCurrentUser();
            if (loggedIn.getUserId().equals(targetUser.getUserId())) {
                if (!BCrypt.checkpw(currentPass, targetUser.getUserPasswdHash())) {
                    throw new IllegalArgumentException("Password saat ini salah!");
                }
            }

            // 2. Validasi Password Baru
            if (!newPass.equals(confirmPass)) {
                throw new IllegalArgumentException("Konfirmasi password baru tidak cocok!");
            }
            if (!ValidationUtil.isStrongPassword(newPass)) {
                throw new IllegalArgumentException("Password baru terlalu lemah (min 8 karakter, simbol, & angka)!");
            }

            // 3. Simpan Hash Baru ke DB
            targetUser.setPassword(newPass); // Method ini otomatis hashing & executeUpdate

            // Bersihkan field
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Password berhasil diganti!");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ganti Password", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}