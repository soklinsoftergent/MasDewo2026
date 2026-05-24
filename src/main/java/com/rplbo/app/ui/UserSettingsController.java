package com.rplbo.app.ui;

import com.rplbo.app.models.User;
import com.rplbo.app.services.UserSession;
import com.rplbo.app.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class UserSettingsController {

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
    }

    @FXML
    private void handleSaveProfile() {
        try {
            String newEmail = emailField.getText().trim();
            if (!ValidationUtil.isValidEmail(newEmail)) {
                throw new IllegalArgumentException("Email tidak valid!");
            }

            targetUser.setUsername(usernameField.getText());
            targetUser.setEmail(newEmail);
            targetUser.setPhoneNumber(phoneField.getText());

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Profil berhasil diperbarui!");
            alert.show();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    private void handleLogout() {
        // Logika logout...
        System.out.println("Logging out...");
    }

    @FXML private void handleUpdatePassword() { /* Logika update password */ }
}