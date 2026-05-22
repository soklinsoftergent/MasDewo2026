package com.rplbo.app;

import com.rplbo.app.dao.*;
import com.rplbo.app.models.*;
import com.rplbo.app.services.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class MainDashboardController {
    // UI References from MainDashboard.fxml
    @FXML private StackPane contentArea;
    @FXML private Label pageTitleLabel, criticalStockBadge, adminRoleBadge;
    @FXML private Button dashboardButton, inventoryButton, salesButton, financeButton, employeeButton;

    // DAOs
    private final ItemDAO itemDAO = new ItemDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final UserDAO userDAO = new UserDAO();
    private final FinanceDAO financeDAO = new FinanceDAO();

    // Caching UI Nodes for speed
    private final Map<String, Node> viewCache = new HashMap<>();
    private final DecimalFormat idr = new DecimalFormat("Rp #,###");

    // We store these so the "Badge" can interact with the Inventory page
    private TextField globalInventorySearchField;

    @FXML
    public void initialize() {
        setupAccessControl();

        // Set up the "Jump to Inventory" logic from the header badge
        criticalStockBadge.setOnMouseClicked(event -> {
            showInventory();
            if (globalInventorySearchField != null) {
                globalInventorySearchField.setText("Kritis");
                // This triggers the listener automatically
            }
        });

        showDashboard(); // Default startup view
    }

    private void setupAccessControl() {
        User current = UserSession.getInstance().getCurrentUser();
        boolean isAdmin = (current != null && current.isAdmin());
        adminRoleBadge.setText(isAdmin ? "Admin" : "Staff");

        financeButton.setVisible(isAdmin);
        financeButton.setManaged(isAdmin);
        employeeButton.setVisible(isAdmin);
        employeeButton.setManaged(isAdmin);
    }

    // --- PAGE NAVIGATION ---

    @FXML public void showDashboard() {
        Node node = loadView("/com/rplbo/app/pages/DashboardPage.fxml");
        updateDashboardMetrics(node);
        renderView(node, "Dashboard", dashboardButton);
    }

    @FXML public void showInventory() {
        Node node = loadView("/com/rplbo/app/pages/InventoryPage.fxml");
        populateInventoryTable(node);
        renderView(node, "Inventory", inventoryButton);
    }

    @FXML public void showSales() {
        Node node = loadView("/com/rplbo/app/pages/SalesPage.fxml");
        populateSalesTable(node);
        renderView(node, "Penjualan", salesButton);
    }

    @FXML public void showFinance() {
        Node node = loadView("/com/rplbo/app/pages/FinancePage.fxml");
        populateFinanceTable(node);
        renderView(node, "Finance & Kas", financeButton);
    }

    @FXML public void showEmployees() {
        Node node = loadView("/com/rplbo/app/pages/EmployeesPage.fxml");
        populateEmployeeTable(node);
        renderView(node, "Karyawan", employeeButton);
    }

    // --- DATA PLUMBING (DAO -> FXML) ---

    private void updateDashboardMetrics(Node root) {
        Label rev = (Label) root.lookup("#dashboardRevenueLabel");
        Label trx = (Label) root.lookup("#dashboardTransactionsLabel");
        VBox criticalBox = (VBox) root.lookup("#criticalItemsListBox");
        BarChart<String, Number> chart = (BarChart) root.lookup("#inventoryStockChart");
        PieChart pie = (PieChart) root.lookup("#platformPieChart");

        // Set Numbers
        rev.setText(idr.format(saleDAO.getTotalRevenue()));
        trx.setText(String.valueOf(saleDAO.getTransactionCount()));

        // Populate Critical List
        List<Item> critical = itemDAO.getLowStockItems(5);
        criticalBox.getChildren().clear();
        critical.forEach(i -> criticalBox.getChildren().add(new Label("⚠️ " + i.getName() + " (" + i.getStock() + ")")));
        criticalStockBadge.setText(critical.size() + " stok kritis");

        // Populate Bar Chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        itemDAO.getLowStockItems(10).forEach(i -> series.getData().add(new XYChart.Data<>(i.getName(), i.getStock())));
        chart.getData().setAll(series);

        // Populate Pie Chart (Categories)
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        itemDAO.getCategoryDistribution().forEach((cat, count) -> pieData.add(new PieChart.Data(cat, count)));
        pie.setData(pieData);
    }

    private void populateInventoryTable(Node root) {
        globalInventorySearchField = (TextField) root.lookup("#inventorySearchField");
        TableView<Item> table = (TableView<Item>) root.lookup("#inventoryTable");

        // Map Columns (Assuming your UI lead named them correctly in FXML)
        ((TableColumn<Item, String>) table.getColumns().get(0)).setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        ((TableColumn<Item, String>) table.getColumns().get(2)).setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStock())));
        ((TableColumn<Item, String>) table.getColumns().get(4)).setCellValueFactory(d -> new SimpleStringProperty(idr.format(d.getValue().getSellingPrice())));

        table.setItems(FXCollections.observableArrayList(itemDAO.getAllItems()));

        // Wire the search bar to your Trie!
        globalInventorySearchField.textProperty().addListener((obs, old, val) -> {
            table.setItems(FXCollections.observableArrayList(itemDAO.searchFast(val)));
        });
    }

    private void populateSalesTable(Node root) {
        TableView<Map<String, Object>> table = (TableView<Map<String, Object>>) root.lookup("#salesTable");

        // Invoice Column
        ((TableColumn<Map<String, Object>, String>) table.getColumns().get(0)).setCellValueFactory(d ->
                new SimpleStringProperty("INV-" + d.getValue().get("sale_id")));

        // Total Column
        ((TableColumn<Map<String, Object>, String>) table.getColumns().get(3)).setCellValueFactory(d ->
                new SimpleStringProperty(idr.format(((Number)d.getValue().get("total_amount")).doubleValue())));

        table.setItems(FXCollections.observableArrayList(saleDAO.getAllSalesDetailed()));
    }

    private void populateFinanceTable(Node root) {
        Label bal = (Label) root.lookup("#cashBalanceLabel");
        bal.setText(idr.format(financeDAO.getKasBalance()));

        TableView<Map<String, Object>> table = (TableView) root.lookup("#financeTable");
        table.setItems(FXCollections.observableArrayList(financeDAO.getRecentTransactions()));
    }

    private void populateEmployeeTable(Node root) {
        VBox box = (VBox) root.lookup("#employeeListBox");
        box.getChildren().clear();
        userDAO.getAllUsers().forEach(u -> {
            box.getChildren().add(new Label("👤 " + u.getUsername() + (u.isAdmin() ? " [ADMIN]" : " [STAFF]")));
        });
    }

    // --- UI HELPERS ---

    private Node loadView(String path) {
        if (viewCache.containsKey(path)) return viewCache.get(path);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Node node = loader.load();
            viewCache.put(path, node);
            return node;
        } catch (IOException e) { throw new RuntimeException("FXML error: " + path, e); }
    }

    private void renderView(Node node, String title, Button activeBtn) {
        contentArea.getChildren().setAll(node);
        pageTitleLabel.setText(title);
        resetSidebarStyles();
        if (activeBtn != null) activeBtn.setStyle("-fx-background-color: #49647c; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void resetSidebarStyles() {
        List.of(dashboardButton, inventoryButton, salesButton, financeButton, employeeButton)
                .forEach(b -> b.setStyle("-fx-background-color: transparent; -fx-text-fill: #627181;"));
    }
}