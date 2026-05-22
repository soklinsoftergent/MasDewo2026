package com.rplbo.app.ui;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import com.rplbo.app.services.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

        System.out.println("Logging in: " + username);

        AuthService authService = new AuthService();

        if (authService.authenticate(username, password)) {
            System.out.println("Login berhasil");

            try {
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("com/rplbo/app/pages/DashboardPage.fxml"));
                Scene scene = new Scene(loader.load());
                stage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Username atau password salah!");
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
