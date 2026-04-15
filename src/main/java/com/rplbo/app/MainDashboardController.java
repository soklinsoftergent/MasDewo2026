package com.rplbo.app;

import com.rplbo.app.dao.UserDAO;
import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Item;
import com.rplbo.app.models.ItemType;
import com.rplbo.app.models.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainDashboardController {
    private static final String DASHBOARD_PAGE = "/com/rplbo/app/pages/DashboardPage.fxml";
    private static final String INVENTORY_PAGE = "/com/rplbo/app/pages/InventoryPage.fxml";
    private static final String SALES_PAGE = "/com/rplbo/app/pages/SalesPage.fxml";
    private static final String FINANCE_PAGE = "/com/rplbo/app/pages/FinancePage.fxml";
    private static final String EMPLOYEES_PAGE = "/com/rplbo/app/pages/EmployeesPage.fxml";

    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,###");
    private static final DateTimeFormatter UI_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML private Button dashboardButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button financeButton;
    @FXML private Button employeeButton;
    @FXML private Label pageTitleLabel;
    @FXML private Label criticalStockBadge;
    @FXML private Label adminRoleBadge;
    @FXML private StackPane contentArea;

    private VBox dashNode;
    private VBox invNode;
    private VBox salesNode;
    private VBox finNode;
    private VBox empNode;

    private Label dashboardRevenueLabel;
    private Label dashboardTransactionsLabel;
    private Label dashboardProfitLabel;
    private Label dashboardExpenseLabel;
    private VBox criticalItemsListBox;
    private BarChart<String, Number> inventoryStockChart;
    private PieChart platformPieChart;

    private TextField inventorySearchField;
    private Button addProductButton;
    private Button inventoryFilterButton;
    private TableView<InventoryRow> inventoryTable;
    private TableColumn<InventoryRow, String> inventoryNameColumn;
    private TableColumn<InventoryRow, String> inventoryCategoryColumn;
    private TableColumn<InventoryRow, String> inventoryStockColumn;
    private TableColumn<InventoryRow, String> inventoryBuyColumn;
    private TableColumn<InventoryRow, String> inventorySellColumn;
    private TableColumn<InventoryRow, String> inventoryStatusColumn;
    private TableColumn<InventoryRow, String> inventoryPlatformColumn;

    private TextField salesSearchField;
    private Button newTransactionButton;
    private TableView<SaleRow> salesTable;
    private TableColumn<SaleRow, String> salesInvoiceColumn;
    private TableColumn<SaleRow, String> salesCustomerColumn;
    private TableColumn<SaleRow, String> salesProductsColumn;
    private TableColumn<SaleRow, String> salesTotalColumn;
    private TableColumn<SaleRow, String> salesPlatformColumn;
    private TableColumn<SaleRow, String> salesCashierColumn;
    private TableColumn<SaleRow, String> salesStatusColumn;

    private Label cashBalanceLabel;
    private Label totalIncomeLabel;
    private Label totalExpenseLabel;
    private TableView<FinanceRow> financeTable;
    private TableColumn<FinanceRow, String> financeDescriptionColumn;
    private TableColumn<FinanceRow, String> financeDateColumn;
    private TableColumn<FinanceRow, String> financeAmountColumn;
    private TableColumn<FinanceRow, String> financeTypeColumn;

    private Button addEmployeeButton;
    private VBox employeeListBox;
    private ScrollPane employeesScrollPane;

    private final ObservableList<InventoryRow> inventoryRows = FXCollections.observableArrayList();
    private final ObservableList<SaleRow> saleRows = FXCollections.observableArrayList();
    private final ObservableList<FinanceRow> financeRows = FXCollections.observableArrayList();
    private final ObservableList<EmployeeRow> employeeRows = FXCollections.observableArrayList();

    private boolean criticalFilterEnabled;

    @FXML
    public void initialize() {
        loadAllPages();
        setupTables();
        wireActions();
        refreshAllData();
        showDashboard();
    }

    private void loadAllPages() {
        LoadedPage dashboardPage = loadPage(DASHBOARD_PAGE);
        dashNode = dashboardPage.root;
        dashboardRevenueLabel = getNode(dashboardPage, "dashboardRevenueLabel", Label.class);
        dashboardTransactionsLabel = getNode(dashboardPage, "dashboardTransactionsLabel", Label.class);
        dashboardProfitLabel = getNode(dashboardPage, "dashboardProfitLabel", Label.class);
        dashboardExpenseLabel = getNode(dashboardPage, "dashboardExpenseLabel", Label.class);
        criticalItemsListBox = getNode(dashboardPage, "criticalItemsListBox", VBox.class);
        inventoryStockChart = getNode(dashboardPage, "inventoryStockChart", BarChart.class);
        platformPieChart = getNode(dashboardPage, "platformPieChart", PieChart.class);

        LoadedPage inventoryPage = loadPage(INVENTORY_PAGE);
        invNode = inventoryPage.root;
        inventorySearchField = getNode(inventoryPage, "inventorySearchField", TextField.class);
        addProductButton = getNode(inventoryPage, "addProductButton", Button.class);
        inventoryFilterButton = getNode(inventoryPage, "inventoryFilterButton", Button.class);
        inventoryTable = getNode(inventoryPage, "inventoryTable", TableView.class);
        inventoryNameColumn = getNode(inventoryPage, "inventoryNameColumn", TableColumn.class);
        inventoryCategoryColumn = getNode(inventoryPage, "inventoryCategoryColumn", TableColumn.class);
        inventoryStockColumn = getNode(inventoryPage, "inventoryStockColumn", TableColumn.class);
        inventoryBuyColumn = getNode(inventoryPage, "inventoryBuyColumn", TableColumn.class);
        inventorySellColumn = getNode(inventoryPage, "inventorySellColumn", TableColumn.class);
        inventoryStatusColumn = getNode(inventoryPage, "inventoryStatusColumn", TableColumn.class);
        inventoryPlatformColumn = getNode(inventoryPage, "inventoryPlatformColumn", TableColumn.class);

        LoadedPage salesPage = loadPage(SALES_PAGE);
        salesNode = salesPage.root;
        salesSearchField = getNode(salesPage, "salesSearchField", TextField.class);
        newTransactionButton = getNode(salesPage, "newTransactionButton", Button.class);
        salesTable = getNode(salesPage, "salesTable", TableView.class);
        salesInvoiceColumn = getNode(salesPage, "salesInvoiceColumn", TableColumn.class);
        salesCustomerColumn = getNode(salesPage, "salesCustomerColumn", TableColumn.class);
        salesProductsColumn = getNode(salesPage, "salesProductsColumn", TableColumn.class);
        salesTotalColumn = getNode(salesPage, "salesTotalColumn", TableColumn.class);
        salesPlatformColumn = getNode(salesPage, "salesPlatformColumn", TableColumn.class);
        salesCashierColumn = getNode(salesPage, "salesCashierColumn", TableColumn.class);
        salesStatusColumn = getNode(salesPage, "salesStatusColumn", TableColumn.class);

        LoadedPage financePage = loadPage(FINANCE_PAGE);
        finNode = financePage.root;
        cashBalanceLabel = getNode(financePage, "cashBalanceLabel", Label.class);
        totalIncomeLabel = getNode(financePage, "totalIncomeLabel", Label.class);
        totalExpenseLabel = getNode(financePage, "totalExpenseLabel", Label.class);
        financeTable = getNode(financePage, "financeTable", TableView.class);
        financeDescriptionColumn = getNode(financePage, "financeDescriptionColumn", TableColumn.class);
        financeDateColumn = getNode(financePage, "financeDateColumn", TableColumn.class);
        financeAmountColumn = getNode(financePage, "financeAmountColumn", TableColumn.class);
        financeTypeColumn = getNode(financePage, "financeTypeColumn", TableColumn.class);

        LoadedPage employeePage = loadPage(EMPLOYEES_PAGE);
        empNode = employeePage.root;
        addEmployeeButton = getNode(employeePage, "addEmployeeButton", Button.class);
        employeeListBox = getNode(employeePage, "employeeListBox", VBox.class);
        employeesScrollPane = getNode(employeePage, "employeesScrollPane", ScrollPane.class);

        for (VBox node : Arrays.asList(dashNode, invNode, salesNode, finNode, empNode)) {
            node.setVisible(false);
            node.setManaged(false);
            contentArea.getChildren().add(node);
        }
    }

    private LoadedPage loadPage(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                throw new IOException("Resource tidak ditemukan: " + resourcePath);
            }
            FXMLLoader loader = new FXMLLoader(url);
            VBox root = loader.load();
            return new LoadedPage(root, loader.getNamespace());
        } catch (IOException exception) {
            throw new IllegalStateException("Gagal memuat halaman " + resourcePath, exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getNode(LoadedPage page, String key, Class<T> type) {
        Object value = page.namespace.get(key);
        if (value == null) {
            throw new IllegalStateException("Komponen dengan fx:id '" + key + "' tidak ditemukan.");
        }
        return (T) value;
    }

    private void wireActions() {
        inventorySearchField.setOnAction(event -> applyInventoryFilters());
        addProductButton.setOnAction(event -> handleAddProduct());
        inventoryFilterButton.setOnAction(event -> handleInventoryFilter());

        salesSearchField.setOnAction(event -> applySalesFilters());
        newTransactionButton.setOnAction(event -> handleNewTransaction());

        addEmployeeButton.setOnAction(event -> handleAddEmployee());

        if (adminRoleBadge != null) {
            adminRoleBadge.setText("Admin");
        }
    }

    private void setupTables() {
        inventoryNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        inventoryCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        inventoryStockColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStock())));
        inventoryBuyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPurchasePrice()));
        inventorySellColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSellingPrice()));
        inventoryStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        inventoryPlatformColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlatform()));
        inventoryStatusColumn.setCellFactory(column -> badgeCell(this::inventoryStatusStyle));
        inventoryPlatformColumn.setCellFactory(column -> badgeCell(this::platformStyle));
        inventoryTable.setItems(inventoryRows);

        salesInvoiceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInvoice()));
        salesCustomerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCustomer()));
        salesProductsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProducts()));
        salesTotalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTotal()));
        salesPlatformColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlatform()));
        salesCashierColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCashier()));
        salesStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        salesPlatformColumn.setCellFactory(column -> badgeCell(this::platformStyle));
        salesStatusColumn.setCellFactory(column -> badgeCell(this::saleStatusStyle));
        salesTable.setItems(saleRows);

        financeDescriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        financeDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        financeAmountColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAmount()));
        financeTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTypeLabel()));
        financeAmountColumn.setCellFactory(column -> badgeCell(this::financeAmountStyle));
        financeTypeColumn.setCellFactory(column -> badgeCell(this::financeTypeStyle));
        financeTable.setItems(financeRows);
    }

    private <S> TableCell<S, String> badgeCell(java.util.function.Function<String, String> styleProvider) {
        return new TableCell<S, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(item);
                badge.setStyle(styleProvider.apply(item));
                setGraphic(badge);
                setText(null);
            }
        };
    }

    @FXML
    public void showDashboard() {
        switchTo(dashNode, "Dashboard", dashboardButton);
    }

    @FXML
    public void showInventory() {
        switchTo(invNode, "Manajemen Inventory", inventoryButton);
    }

    @FXML
    public void showSales() {
        switchTo(salesNode, "Penjualan", salesButton);
    }

    @FXML
    public void showFinance() {
        switchTo(finNode, "Keuangan & Kas", financeButton);
    }

    @FXML
    public void showEmployees() {
        switchTo(empNode, "Karyawan", employeeButton);
    }

    private void switchTo(VBox targetNode, String title, Button activeButton) {
        for (Node child : contentArea.getChildren()) {
            child.setVisible(false);
            child.setManaged(false);
        }
        targetNode.setVisible(true);
        targetNode.setManaged(true);
        targetNode.toFront();
        pageTitleLabel.setText(title);
        updateButtonStyle(activeButton);
    }

    private void updateButtonStyle(Button activeButton) {
        List<Button> buttons = List.of(dashboardButton, inventoryButton, salesButton, financeButton, employeeButton);
        String activeStyle = "-fx-background-color: #49647c; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 12 16; -fx-alignment: CENTER_LEFT;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #627181; -fx-font-weight: 600; -fx-background-radius: 8; -fx-padding: 12 16; -fx-alignment: CENTER_LEFT;";
        for (Button button : buttons) {
            button.setStyle(button == activeButton ? activeStyle : inactiveStyle);
        }
    }

    private void refreshAllData() {
        refreshInventoryRows();
        refreshSaleRows();
        refreshFinanceRows();
        refreshEmployeeRows();
        refreshDashboard();
    }

    private void refreshDashboard() {
        DashboardMetrics metrics = loadDashboardMetrics();
        dashboardRevenueLabel.setText(formatCurrency(metrics.totalRevenue));
        dashboardTransactionsLabel.setText(String.valueOf(metrics.totalTransactions));
        dashboardProfitLabel.setText(formatCurrency(metrics.totalProfit));
        dashboardExpenseLabel.setText(formatCurrency(metrics.totalExpense));
        criticalStockBadge.setText(metrics.criticalCount + " stok kritis");

        criticalItemsListBox.getChildren().clear();
        if (metrics.criticalItems.isEmpty()) {
            Label empty = new Label("Tidak ada stok kritis.");
            empty.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 14px;");
            criticalItemsListBox.getChildren().add(empty);
        } else {
            for (InventoryRow row : metrics.criticalItems) {
                HBox line = new HBox();
                line.setSpacing(12);
                Label name = new Label(row.getName());
                name.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: 800; -fx-font-size: 16px;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label qty = new Label(row.getStock() + " Unit");
                qty.setStyle("-fx-text-fill: #9ca3af; -fx-font-weight: 800; -fx-font-size: 16px;");
                line.getChildren().addAll(name, spacer, qty);
                criticalItemsListBox.getChildren().add(line);
            }
        }

        inventoryStockChart.getData().clear();
        XYChart.Series<String, Number> stockSeries = new XYChart.Series<>();
        for (InventoryRow row : inventoryRows.stream().limit(6).toList()) {
            stockSeries.getData().add(new XYChart.Data<>(shortName(row.getName()), row.getStock()));
        }
        inventoryStockChart.getData().add(stockSeries);

        platformPieChart.getData().clear();
        for (PlatformShare share : loadPlatformShares()) {
            platformPieChart.getData().add(new PieChart.Data(share.name, share.count));
        }
    }

    private void refreshInventoryRows() {
        inventoryRows.setAll(loadInventoryRows());
        applyInventoryFilters();
    }

    private void refreshSaleRows() {
        saleRows.setAll(loadSaleRows());
        applySalesFilters();
    }

    private void refreshFinanceRows() {
        FinanceSummary summary = loadFinanceSummary();
        cashBalanceLabel.setText(formatCurrency(summary.balance));
        totalIncomeLabel.setText(formatCurrency(summary.totalIncome));
        totalExpenseLabel.setText(formatCurrency(summary.totalExpense));
        financeRows.setAll(summary.rows);
    }

    private void refreshEmployeeRows() {
        employeeRows.setAll(loadEmployeeRows());
        employeeListBox.getChildren().clear();
        for (EmployeeRow row : employeeRows) {
            employeeListBox.getChildren().add(createEmployeeCard(row));
        }
    }

    private void applyInventoryFilters() {
        String keyword = inventorySearchField.getText() == null ? "" : inventorySearchField.getText().trim().toLowerCase();
        ObservableList<InventoryRow> filtered = FXCollections.observableArrayList();
        for (InventoryRow row : loadInventoryRows()) {
            boolean matchesKeyword = keyword.isBlank()
                    || row.getName().toLowerCase().contains(keyword)
                    || row.getCategory().toLowerCase().contains(keyword)
                    || row.getPlatform().toLowerCase().contains(keyword);
            boolean matchesCritical = !criticalFilterEnabled || row.getStock() < 5;
            if (matchesKeyword && matchesCritical) {
                filtered.add(row);
            }
        }
        inventoryRows.setAll(filtered);
        inventoryFilterButton.setText(criticalFilterEnabled ? "Filter Aktif" : "Filter");
    }

    private void applySalesFilters() {
        String keyword = salesSearchField.getText() == null ? "" : salesSearchField.getText().trim().toLowerCase();
        ObservableList<SaleRow> filtered = FXCollections.observableArrayList();
        for (SaleRow row : loadSaleRows()) {
            boolean matches = keyword.isBlank()
                    || row.getInvoice().toLowerCase().contains(keyword)
                    || row.getCustomer().toLowerCase().contains(keyword)
                    || row.getCashier().toLowerCase().contains(keyword)
                    || row.getPlatform().toLowerCase().contains(keyword);
            if (matches) {
                filtered.add(row);
            }
        }
        saleRows.setAll(filtered);
    }

    private DashboardMetrics loadDashboardMetrics() {
        double totalRevenue = queryDouble("SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE is_cancelled = 0");
        double totalProfit = queryDouble("SELECT COALESCE(SUM(profit), 0) FROM sales WHERE is_cancelled = 0");
        double totalExpense = queryDouble("SELECT COALESCE(SUM(total), 0) FROM expenses");
        int totalTransactions = queryInt("SELECT COUNT(*) FROM sales WHERE is_cancelled = 0");
        List<InventoryRow> criticalItems = new ArrayList<>();
        for (InventoryRow row : loadInventoryRows()) {
            if (row.getStock() < 5) {
                criticalItems.add(row);
            }
        }
        return new DashboardMetrics(totalRevenue, totalProfit, totalExpense, totalTransactions, criticalItems.size(), criticalItems);
    }

    private List<InventoryRow> loadInventoryRows() {
        List<InventoryRow> rows = new ArrayList<>();
        String sql = "SELECT i.id, i.name, COALESCE(t.name, '-') AS category, i.stock, i.purchase_price, i.selling_price, "
                + "COALESCE((SELECT e.ecom_name FROM ecommerce_items ei JOIN ecommerces e ON e.ecom_id = ei.ecom_id "
                + "WHERE ei.item_id = i.id ORDER BY ei.ecom_item_id LIMIT 1), 'Langsung') AS platform "
                + "FROM items i LEFT JOIN item_types t ON t.it_ty_id = i.it_ty_id ORDER BY i.id";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int stock = resultSet.getInt("stock");
                String status = stock <= 0 ? "Habis" : stock < 5 ? "Kritis" : "Tersedia";
                rows.add(new InventoryRow(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("category"),
                        stock,
                        formatCurrency(resultSet.getDouble("purchase_price")),
                        formatCurrency(resultSet.getDouble("selling_price")),
                        status,
                        resultSet.getString("platform")
                ));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat inventory", exception);
        }
        return rows;
    }

    private List<SaleRow> loadSaleRows() {
        List<SaleRow> rows = new ArrayList<>();
        String sql = "SELECT s.sale_id, c.name AS customer_name, COALESCE(SUM(si.quantity), 0) AS item_count, "
                + "s.total_amount, COALESCE(e.ecom_name, 'Langsung') AS platform_name, COALESCE(u.username, '-') AS cashier_name, "
                + "s.is_paid, s.is_cancelled "
                + "FROM sales s "
                + "LEFT JOIN customers c ON c.cust_id = s.cust_id "
                + "LEFT JOIN users u ON u.user_id = s.user_id "
                + "LEFT JOIN ecommerces e ON e.ecom_id = s.ecom_id "
                + "LEFT JOIN sale_items si ON si.sale_id = s.sale_id "
                + "GROUP BY s.sale_id, c.name, s.total_amount, e.ecom_name, u.username, s.is_paid, s.is_cancelled "
                + "ORDER BY s.sale_id DESC";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                boolean cancelled = resultSet.getBoolean("is_cancelled");
                boolean paid = resultSet.getBoolean("is_paid");
                String status = cancelled ? "Batal" : paid ? "Selesai" : "Proses";
                rows.add(new SaleRow(
                        resultSet.getInt("sale_id"),
                        invoiceLabel(resultSet.getInt("sale_id")),
                        resultSet.getString("customer_name"),
                        resultSet.getInt("item_count") + " Item",
                        formatCurrency(resultSet.getDouble("total_amount")),
                        resultSet.getString("platform_name"),
                        capitalize(resultSet.getString("cashier_name")),
                        status
                ));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat data penjualan", exception);
        }
        return rows;
    }

    private FinanceSummary loadFinanceSummary() {
        List<FinanceRow> rows = new ArrayList<>();
        String sql = "SELECT kas_trans_id, description, transaction_date, amount, type FROM kas_transactions "
                + "ORDER BY transaction_date DESC, kas_trans_id DESC";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String type = resultSet.getString("type");
                double amount = resultSet.getDouble("amount");
                rows.add(new FinanceRow(
                        resultSet.getInt("kas_trans_id"),
                        defaultText(resultSet.getString("description"), "-"),
                        formatDate(resultSet.getObject("transaction_date")),
                        (type.equalsIgnoreCase("INCOME") ? "+ " : "- ") + formatCurrency(amount),
                        type.equalsIgnoreCase("INCOME") ? "Pemasukan" : "Pengeluaran"
                ));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat transaksi kas", exception);
        }

        double balance = queryDouble("SELECT COALESCE(balance, 0) FROM kas WHERE id = 1");
        double totalIncome = queryDouble("SELECT COALESCE(SUM(amount), 0) FROM kas_transactions WHERE type = 'INCOME'");
        double totalExpense = queryDouble("SELECT COALESCE(SUM(amount), 0) FROM kas_transactions WHERE type = 'EXPENSE'");
        return new FinanceSummary(balance, totalIncome, totalExpense, rows);
    }

    private List<EmployeeRow> loadEmployeeRows() {
        List<EmployeeRow> rows = new ArrayList<>();
        String sql = "SELECT user_id, username, email, phonenumber, role_id, COALESCE(is_active, 1) AS is_active FROM users ORDER BY role_id ASC, username ASC";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                boolean isAdmin = resultSet.getInt("role_id") == 1;
                boolean active = resultSet.getBoolean("is_active");
                rows.add(new EmployeeRow(
                        resultSet.getInt("user_id"),
                        capitalize(resultSet.getString("username")),
                        defaultText(resultSet.getString("email"), "Tidak ada email"),
                        defaultText(resultSet.getString("phonenumber"), "-"),
                        isAdmin ? "Admin" : "Aktif",
                        active,
                        isAdmin
                ));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat data karyawan", exception);
        }
        return rows;
    }

    private List<PlatformShare> loadPlatformShares() {
        List<PlatformShare> shares = new ArrayList<>();
        String sql = "SELECT COALESCE(e.ecom_name, 'Langsung') AS platform_name, COUNT(*) AS total_sales "
                + "FROM sales s LEFT JOIN ecommerces e ON e.ecom_id = s.ecom_id "
                + "WHERE s.is_cancelled = 0 GROUP BY COALESCE(e.ecom_name, 'Langsung')";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                shares.add(new PlatformShare(resultSet.getString("platform_name"), resultSet.getInt("total_sales")));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat distribusi platform", exception);
        }
        if (shares.isEmpty()) {
            shares.add(new PlatformShare("Belum ada data", 1));
        }
        return shares;
    }

    private Node createEmployeeCard(EmployeeRow row) {
        HBox card = new HBox();
        card.setSpacing(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color: #4d667b; -fx-background-radius: 10;");

        Label avatar = new Label(row.getInitial());
        avatar.setMinSize(42, 42);
        avatar.setStyle("-fx-background-color: #87c47c; -fx-background-radius: 21; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: 800; -fx-alignment: center;");

        VBox infoBox = new VBox();
        infoBox.setSpacing(4);
        Label nameLabel = new Label(row.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 800;");
        Label subtitleLabel = new Label(row.getEmail() + " | " + row.getPhone());
        subtitleLabel.setStyle("-fx-text-fill: #d7e0e8; -fx-font-size: 11px;");
        infoBox.getChildren().addAll(nameLabel, subtitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label badge = new Label(row.getBadgeText());
        badge.setStyle(row.isAdmin()
                ? "-fx-background-color: #d8c3ff; -fx-text-fill: #6d4ab5; -fx-padding: 8 20; -fx-background-radius: 8; -fx-font-weight: 800;"
                : "-fx-background-color: #dff8cd; -fx-text-fill: #4e8f4c; -fx-padding: 8 20; -fx-background-radius: 8; -fx-font-weight: 800;");

        card.getChildren().addAll(avatar, infoBox, spacer, badge);
        return card;
    }

    private void handleAddProduct() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Produk");
        dialog.setHeaderText("Masukkan data produk baru");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        ComboBox<ItemTypeOption> typeCombo = new ComboBox<>(FXCollections.observableArrayList(loadItemTypes()));
        TextField newTypeField = new TextField();
        ComboBox<PlatformOption> platformCombo = new ComboBox<>(FXCollections.observableArrayList(loadPlatforms()));
        Spinner<Integer> stockSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 1));
        TextField purchaseField = new TextField();
        TextField sellingField = new TextField();

        nameField.setPromptText("Nama produk");
        newTypeField.setPromptText("Kategori baru jika belum ada");
        purchaseField.setPromptText("Harga beli");
        sellingField.setPromptText("Harga jual");

        GridPane form = buildForm(
                "Nama Produk", nameField,
                "Kategori", typeCombo,
                "Kategori Baru", newTypeField,
                "Platform", platformCombo,
                "Stok", stockSpinner,
                "Harga Beli", purchaseField,
                "Harga Jual", sellingField
        );
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            String name = requireText(nameField, "Nama produk");
            double purchasePrice = parseAmount(purchaseField.getText(), "Harga beli");
            double sellingPrice = parseAmount(sellingField.getText(), "Harga jual");
            int stock = stockSpinner.getValue();

            ItemTypeOption selectedType = typeCombo.getValue();
            int itemTypeId;
            if (selectedType != null) {
                itemTypeId = selectedType.id;
            } else {
                String categoryName = requireText(newTypeField, "Kategori baru");
                ItemType newType = new ItemType(categoryName, "Kategori dibuat dari UI");
                if (!newType.save()) {
                    throw new IllegalStateException("Kategori gagal disimpan.");
                }
                itemTypeId = newType.getItTyId();
            }

            Item item = new Item(name, stock, itemTypeId, purchasePrice, sellingPrice);
            if (!item.save()) {
                throw new IllegalStateException("Produk gagal disimpan.");
            }

            PlatformOption platform = platformCombo.getValue();
            if (platform != null) {
                Map<String, Object> ecommerceItem = new LinkedHashMap<>();
                ecommerceItem.put("item_id", item.getId());
                ecommerceItem.put("ecom_id", platform.id);
                ecommerceItem.put("price_override", sellingPrice);
                DBConnection.getInstance().insertIntoTableAndGetId("ecommerce_items", ecommerceItem);
            }

            refreshAllData();
        } catch (Exception exception) {
            showError("Tambah produk gagal", exception);
        }
    }

    private void handleNewTransaction() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Transaksi Baru");
        dialog.setHeaderText("Tambah transaksi penjualan");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<ItemOption> itemCombo = new ComboBox<>(FXCollections.observableArrayList(loadAvailableItems()));
        ComboBox<PlatformOption> platformCombo = new ComboBox<>(FXCollections.observableArrayList(loadPlatforms()));
        ComboBox<UserOption> cashierCombo = new ComboBox<>(FXCollections.observableArrayList(loadUsers()));
        Spinner<Integer> quantitySpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        TextField customerNameField = new TextField();
        TextField customerPhoneField = new TextField();
        ComboBox<String> paymentMethodCombo = new ComboBox<>(FXCollections.observableArrayList("TRANSFER", "COD", "QRIS"));
        CheckBox paidCheckBox = new CheckBox("Sudah dibayar");

        customerNameField.setPromptText("Nama pelanggan");
        customerPhoneField.setPromptText("Nomor telepon");
        paymentMethodCombo.getSelectionModel().select("TRANSFER");

        GridPane form = buildForm(
                "Pelanggan", customerNameField,
                "Telepon", customerPhoneField,
                "Produk", itemCombo,
                "Jumlah", quantitySpinner,
                "Platform", platformCombo,
                "Kasir", cashierCombo,
                "Pembayaran", paymentMethodCombo,
                "Status", paidCheckBox
        );
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            ItemOption selectedItem = requireSelection(itemCombo, "Produk");
            PlatformOption selectedPlatform = requireSelection(platformCombo, "Platform");
            UserOption selectedCashier = requireSelection(cashierCombo, "Kasir");
            int quantity = quantitySpinner.getValue();
            if (quantity > selectedItem.stock) {
                throw new IllegalArgumentException("Stok tidak mencukupi.");
            }

            int customerId = ensureCustomer(requireText(customerNameField, "Nama pelanggan"), customerPhoneField.getText());
            double total = selectedItem.sellingPrice * quantity;
            double profit = (selectedItem.sellingPrice - selectedItem.purchasePrice) * quantity;

            Map<String, Object> saleData = new LinkedHashMap<>();
            saleData.put("cust_id", customerId);
            saleData.put("user_id", selectedCashier.id);
            saleData.put("ecom_id", selectedPlatform.id);
            saleData.put("total_amount", total);
            saleData.put("is_paid", paidCheckBox.isSelected());
            saleData.put("is_cancelled", false);
            saleData.put("payment_method", paymentMethodCombo.getValue());
            saleData.put("logistics_fee", 0.0);
            saleData.put("profit", profit);
            Integer saleId = DBConnection.getInstance().insertIntoTableAndGetId("sales", saleData);
            if (saleId == null) {
                throw new IllegalStateException("Transaksi gagal disimpan.");
            }

            Map<String, Object> saleItemData = new LinkedHashMap<>();
            saleItemData.put("sale_id", saleId);
            saleItemData.put("item_id", selectedItem.id);
            saleItemData.put("quantity", quantity);
            saleItemData.put("unit_price", selectedItem.sellingPrice);
            saleItemData.put("total_price", total);
            DBConnection.getInstance().insertIntoTableAndGetId("sale_items", saleItemData);

            Item item = new Item(selectedItem.id, selectedItem.name, selectedItem.stock, selectedItem.typeId, selectedItem.purchasePrice, selectedItem.sellingPrice);
            item.setStock(selectedItem.stock - quantity);

            if (paidCheckBox.isSelected()) {
                Map<String, Object> kasData = new LinkedHashMap<>();
                kasData.put("user_id", selectedCashier.id);
                kasData.put("type", "INCOME");
                kasData.put("description", "Penjualan #" + invoiceLabel(saleId));
                kasData.put("amount", total);
                DBConnection.getInstance().insertIntoTableAndGetId("kas_transactions", kasData);
                syncKasBalance();
            }

            refreshAllData();
            showSales();
        } catch (Exception exception) {
            showError("Tambah transaksi gagal", exception);
        }
    }

    private void handleAddEmployee() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Karyawan");
        dialog.setHeaderText("Masukkan data karyawan baru");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> roleCombo = new ComboBox<>(FXCollections.observableArrayList("Staff", "Admin"));
        roleCombo.getSelectionModel().selectFirst();

        usernameField.setPromptText("Username");
        emailField.setPromptText("Email");
        phoneField.setPromptText("Nomor telepon");
        passwordField.setPromptText("Password");

        GridPane form = buildForm(
                "Username", usernameField,
                "Email", emailField,
                "Telepon", phoneField,
                "Password", passwordField,
                "Peran", roleCombo
        );
        dialog.getDialogPane().setContent(form);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            User user = new User(
                    requireText(usernameField, "Username"),
                    requireText(emailField, "Email"),
                    requireText(passwordField, "Password"),
                    requireText(phoneField, "Nomor telepon"),
                    "Admin".equals(roleCombo.getValue()),
                    null
            );
            UserDAO userDAO = new UserDAO();
            if (!userDAO.saveUser(user)) {
                throw new IllegalStateException("Karyawan gagal disimpan.");
            }
            refreshAllData();
            showEmployees();
        } catch (Exception exception) {
            showError("Tambah karyawan gagal", exception);
        }
    }

    private void handleInventoryFilter() {
        criticalFilterEnabled = !criticalFilterEnabled;
        applyInventoryFilters();
    }

    private List<ItemTypeOption> loadItemTypes() {
        List<ItemTypeOption> options = new ArrayList<>();
        String sql = "SELECT it_ty_id, name FROM item_types ORDER BY name";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                options.add(new ItemTypeOption(resultSet.getInt("it_ty_id"), resultSet.getString("name")));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat kategori", exception);
        }
        return options;
    }

    private List<PlatformOption> loadPlatforms() {
        List<PlatformOption> options = new ArrayList<>();
        String sql = "SELECT ecom_id, ecom_name FROM ecommerces ORDER BY ecom_name";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                options.add(new PlatformOption(resultSet.getInt("ecom_id"), resultSet.getString("ecom_name")));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat platform", exception);
        }
        return options;
    }

    private List<UserOption> loadUsers() {
        List<UserOption> options = new ArrayList<>();
        String sql = "SELECT user_id, username, role_id FROM users ORDER BY username";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                options.add(new UserOption(resultSet.getInt("user_id"), resultSet.getString("username"), resultSet.getInt("role_id") == 1));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat daftar kasir", exception);
        }
        return options;
    }

    private List<ItemOption> loadAvailableItems() {
        List<ItemOption> options = new ArrayList<>();
        String sql = "SELECT id, name, stock, it_ty_id, purchase_price, selling_price FROM items WHERE stock > 0 ORDER BY name";
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                options.add(new ItemOption(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("stock"),
                        resultSet.getInt("it_ty_id"),
                        resultSet.getDouble("purchase_price"),
                        resultSet.getDouble("selling_price")
                ));
            }
        } catch (SQLException exception) {
            showError("Gagal memuat produk", exception);
        }
        return options;
    }

    private int ensureCustomer(String name, String phone) throws SQLException {
        String lookupSql = "SELECT cust_id FROM customers WHERE name = ? LIMIT 1";
        try (PreparedStatement lookup = connection().prepareStatement(lookupSql)) {
            lookup.setString(1, name);
            try (ResultSet resultSet = lookup.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("cust_id");
                }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", name);
        data.put("phone_number", phone == null ? "" : phone.trim());
        data.put("email", "");
        data.put("address", "");
        Integer customerId = DBConnection.getInstance().insertIntoTableAndGetId("customers", data);
        if (customerId == null) {
            throw new IllegalStateException("Pelanggan gagal dibuat.");
        }
        return customerId;
    }

    private void syncKasBalance() throws SQLException {
        String sql = "UPDATE kas SET balance = (SELECT COALESCE(SUM(CASE WHEN type='INCOME' THEN amount ELSE -amount END), 0) FROM kas_transactions) WHERE id = 1";
        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private GridPane buildForm(Object... fields) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(12);
        gridPane.setPadding(new Insets(8));
        for (int index = 0; index < fields.length; index += 2) {
            Label label = new Label(String.valueOf(fields[index]));
            label.setStyle("-fx-text-fill: #1f2937; -fx-font-weight: 700;");
            Node node = (Node) fields[index + 1];
            if (node instanceof Region) {
                ((Region) node).setPrefWidth(280);
            }
            gridPane.add(label, 0, index / 2);
            gridPane.add(node, 1, index / 2);
        }
        return gridPane;
    }

    private Connection connection() throws SQLException {
        DBConnection.getInstance().ensureConnection();
        return DBConnection.getInstance().getConnection();
    }

    private double queryDouble(String sql) {
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getDouble(1) : 0.0;
        } catch (SQLException exception) {
            showError("Query gagal dijalankan", exception);
            return 0.0;
        }
    }

    private int queryInt(String sql) {
        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException exception) {
            showError("Query gagal dijalankan", exception);
            return 0;
        }
    }

    private String formatCurrency(double value) {
        return "Rp. " + INTEGER_FORMAT.format(Math.round(value)).replace(',', '.');
    }

    private String formatDate(Object value) {
        if (value == null) {
            return "-";
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(UI_DATE_FORMAT);
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime().format(UI_DATE_FORMAT);
        }
        String text = value.toString();
        try {
            return LocalDateTime.parse(text.replace(' ', 'T')).format(UI_DATE_FORMAT);
        } catch (DateTimeParseException ignored) {
            return text.length() >= 10 ? text.substring(0, 10) : text;
        }
    }

    private String invoiceLabel(int saleId) {
        return String.format("INV-%04d", saleId);
    }

    private String shortName(String name) {
        return name.length() <= 12 ? name : name.substring(0, 12) + "...";
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String[] parts = value.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return builder.toString();
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String requireText(TextField field, String label) {
        String text = field.getText();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(label + " wajib diisi.");
        }
        return text.trim();
    }

    private double parseAmount(String text, String label) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(label + " wajib diisi.");
        }
        try {
            return Double.parseDouble(text.trim().replace(".", "").replace(",", "."));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " harus berupa angka.");
        }
    }

    private <T> T requireSelection(ComboBox<T> comboBox, String label) {
        T value = comboBox.getValue();
        if (value == null) {
            throw new IllegalArgumentException(label + " wajib dipilih.");
        }
        return value;
    }

    private void showError(String title, Exception exception) {
        exception.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Terjadi Kesalahan");
        alert.setHeaderText(title);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }

    private String inventoryStatusStyle(String status) {
        if ("Habis".equals(status)) {
            return "-fx-background-color: #7f1d1d; -fx-text-fill: white; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
        }
        if ("Kritis".equals(status)) {
            return "-fx-background-color: #ffd15a; -fx-text-fill: #6b4f00; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
        }
        return "-fx-background-color: #69d26d; -fx-text-fill: white; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
    }

    private String platformStyle(String platform) {
        String value = platform.toLowerCase();
        if ("shopee".equals(value)) {
            return "-fx-background-color: #f7c2c5; -fx-text-fill: #b33f44; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
        }
        if ("tokopedia".equals(value)) {
            return "-fx-background-color: #9cc6ff; -fx-text-fill: #315b95; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
        }
        return "-fx-background-color: #d6d8de; -fx-text-fill: #505765; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
    }

    private String saleStatusStyle(String status) {
        if ("Selesai".equals(status)) {
            return "-fx-background-color: #dff8cd; -fx-text-fill: #4e8f4c; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
        }
        if ("Batal".equals(status)) {
            return "-fx-background-color: #7f1d1d; -fx-text-fill: white; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
        }
        return "-fx-background-color: #f4b51d; -fx-text-fill: white; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-weight: 800;";
    }

    private String financeAmountStyle(String amount) {
        return amount.startsWith("+")
                ? "-fx-background-color: #dff8cd; -fx-text-fill: #4e8f4c; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-weight: 800;"
                : "-fx-background-color: #f7c2c5; -fx-text-fill: #b33f44; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-weight: 800;";
    }

    private String financeTypeStyle(String type) {
        return "Pemasukan".equals(type)
                ? "-fx-background-color: #dff8cd; -fx-text-fill: #4e8f4c; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-weight: 800;"
                : "-fx-background-color: #f7c2c5; -fx-text-fill: #b33f44; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-weight: 800;";
    }

    private static final class LoadedPage {
        private final VBox root;
        private final Map<String, Object> namespace;

        private LoadedPage(VBox root, Map<String, Object> namespace) {
            this.root = root;
            this.namespace = namespace;
        }
    }

    private static final class DashboardMetrics {
        private final double totalRevenue;
        private final double totalProfit;
        private final double totalExpense;
        private final int totalTransactions;
        private final int criticalCount;
        private final List<InventoryRow> criticalItems;

        private DashboardMetrics(double totalRevenue, double totalProfit, double totalExpense, int totalTransactions, int criticalCount, List<InventoryRow> criticalItems) {
            this.totalRevenue = totalRevenue;
            this.totalProfit = totalProfit;
            this.totalExpense = totalExpense;
            this.totalTransactions = totalTransactions;
            this.criticalCount = criticalCount;
            this.criticalItems = criticalItems;
        }
    }

    private static final class FinanceSummary {
        private final double balance;
        private final double totalIncome;
        private final double totalExpense;
        private final List<FinanceRow> rows;

        private FinanceSummary(double balance, double totalIncome, double totalExpense, List<FinanceRow> rows) {
            this.balance = balance;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.rows = rows;
        }
    }

    private static final class PlatformShare {
        private final String name;
        private final int count;

        private PlatformShare(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    public static final class InventoryRow {
        private final int id;
        private final String name;
        private final String category;
        private final int stock;
        private final String purchasePrice;
        private final String sellingPrice;
        private final String status;
        private final String platform;

        private InventoryRow(int id, String name, String category, int stock, String purchasePrice, String sellingPrice, String status, String platform) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.stock = stock;
            this.purchasePrice = purchasePrice;
            this.sellingPrice = sellingPrice;
            this.status = status;
            this.platform = platform;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public int getStock() { return stock; }
        public String getPurchasePrice() { return purchasePrice; }
        public String getSellingPrice() { return sellingPrice; }
        public String getStatus() { return status; }
        public String getPlatform() { return platform; }
    }

    public static final class SaleRow {
        private final int id;
        private final String invoice;
        private final String customer;
        private final String products;
        private final String total;
        private final String platform;
        private final String cashier;
        private final String status;

        private SaleRow(int id, String invoice, String customer, String products, String total, String platform, String cashier, String status) {
            this.id = id;
            this.invoice = invoice;
            this.customer = customer;
            this.products = products;
            this.total = total;
            this.platform = platform;
            this.cashier = cashier;
            this.status = status;
        }

        public int getId() { return id; }
        public String getInvoice() { return invoice; }
        public String getCustomer() { return customer; }
        public String getProducts() { return products; }
        public String getTotal() { return total; }
        public String getPlatform() { return platform; }
        public String getCashier() { return cashier; }
        public String getStatus() { return status; }
    }

    public static final class FinanceRow {
        private final int id;
        private final String description;
        private final String date;
        private final String amount;
        private final String typeLabel;

        private FinanceRow(int id, String description, String date, String amount, String typeLabel) {
            this.id = id;
            this.description = description;
            this.date = date;
            this.amount = amount;
            this.typeLabel = typeLabel;
        }

        public int getId() { return id; }
        public String getDescription() { return description; }
        public String getDate() { return date; }
        public String getAmount() { return amount; }
        public String getTypeLabel() { return typeLabel; }
    }

    public static final class EmployeeRow {
        private final int id;
        private final String name;
        private final String email;
        private final String phone;
        private final String badgeText;
        private final boolean active;
        private final boolean admin;

        private EmployeeRow(int id, String name, String email, String phone, String badgeText, boolean active, boolean admin) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.badgeText = badgeText;
            this.active = active;
            this.admin = admin;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getBadgeText() { return badgeText; }
        public boolean isActive() { return active; }
        public boolean isAdmin() { return admin; }
        public String getInitial() { return name.substring(0, 1).toUpperCase(); }
    }

    private static final class ItemTypeOption {
        private final int id;
        private final String name;

        private ItemTypeOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class PlatformOption {
        private final int id;
        private final String name;

        private PlatformOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class UserOption {
        private final int id;
        private final String username;
        private final boolean admin;

        private UserOption(int id, String username, boolean admin) {
            this.id = id;
            this.username = username;
            this.admin = admin;
        }

        @Override
        public String toString() {
            return admin ? username + " (Admin)" : username;
        }
    }

    private static final class ItemOption {
        private final int id;
        private final String name;
        private final int stock;
        private final int typeId;
        private final double purchasePrice;
        private final double sellingPrice;

        private ItemOption(int id, String name, int stock, int typeId, double purchasePrice, double sellingPrice) {
            this.id = id;
            this.name = name;
            this.stock = stock;
            this.typeId = typeId;
            this.purchasePrice = purchasePrice;
            this.sellingPrice = sellingPrice;
        }

        @Override
        public String toString() {
            return name + " (" + stock + " stok)";
        }
    }
}
