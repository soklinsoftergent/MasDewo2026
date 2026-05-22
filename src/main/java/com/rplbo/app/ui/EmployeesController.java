package com.rplbo.app.ui;

import com.rplbo.app.dao.UserDAO;
import com.rplbo.app.models.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

        // Action Column (Edit/PHK Buttons)
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button btnPhk = new Button("PHK");
            {
                btnPhk.setStyle("-fx-background-color: transparent; -fx-border-color: #ff8e8e; -fx-text-fill: #ff8e8e; -fx-border-radius: 5;");
                btnPhk.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handlePhk(user);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, btnPhk));
            }
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

    @FXML
    private void handleShowAddDialog() {
        // Logic Lead will implement the Add User Dialog here
        System.out.println("Opening Add Employee Dialog...");
    }

    private void handlePhk(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Nonaktifkan karyawan " + user.getUsername() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
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
}