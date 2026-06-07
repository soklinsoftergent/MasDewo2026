package com.rplbo.app.ui;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import com.rplbo.app.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class LoginWindowController {
    @FXML
    public Label statusLabel;
    @FXML
    public Button loginButton;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField usernameField;

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // validate input
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username dan password harus terisi!");
            return;
        }

        // Tampilkan status sedang memproses
        statusLabel.setText("Sedang memverifikasi...");
        statusLabel.setStyle("-fx-text-fill: #dbe7ef;");

        System.out.println("Logging in: " + username);

        AuthService authService = new AuthService();

        if (authService.authenticate(username, password)) {
            System.out.println("Login berhasil");
            navigateToDashboard(actionEvent);
        } else {
            statusLabel.setText("Kredensial yang dimasukkan salah");
            statusLabel.setStyle("-fx-text-fill: #ff8e8e;");
        }
    }

    private void navigateToDashboard(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/rplbo/app/pages/MainDashboard.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setTitle("Dewa App");
            stage.setResizable(true);
            stage.setMinWidth(1100);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Gagal memuat dashboard");
            e.printStackTrace();
        }
    }

    private void switchToDashboard(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("com/rplbo/app/pages/DashboardPage.fxml"));
            Scene scene = new Scene(loader.load());

            stage.setTitle("Toko Mas Dewo");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Gagal ngemot Toko Mas Dewo");
            e.printStackTrace();
        }
    }

}
