package com.rplbo.app.ui;

import com.rplbo.app.models.User;
import com.rplbo.app.services.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserSettingsController {
    @FXML
    public Label avatarLabel;
    @FXML
    public Label displayNameLabel;
    @FXML
    public Label roleBadge;
    @FXML
    public Label displayEmailLabel;
    @FXML
    public Label lastLoginLabel;
    @FXML
    public TextField usernameField;
    @FXML
    public TextField emailField;
    @FXML
    public TextField phoneField;
    @FXML
    public PasswordField currentPasswordField;
    @FXML
    public PasswordField newPasswordField;
    @FXML
    public PasswordField confirmPasswordField;

    @FXML
    public void handleLogout(ActionEvent actionEvent) {

    }
    @FXML
    public void handleSaveProfile(ActionEvent actionEvent) {

    }
    @FXML
    public void handleUpdatePassword(ActionEvent actionEvent) {

    }
    @FXML
    public void initialize() {
        User current = UserSession.getInstance().getCurrentUser();

        // Fill the display labels
        displayNameLabel.setText(current.getUsername());
        displayEmailLabel.setText(current.getUserEmail());
        avatarLabel.setText(current.getUsername().substring(0, 2).toUpperCase());

        // Pre-fill the form fields
        usernameField.setText(current.getUsername());
        emailField.setText(current.getUserEmail());
        phoneField.setText(current.getUserPhoneNumber());
    }
}
