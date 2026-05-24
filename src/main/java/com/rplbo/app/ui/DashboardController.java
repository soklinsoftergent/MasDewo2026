package com.rplbo.app.ui;

import com.rplbo.app.dao.ItemDAO;
import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class DashboardController {

    // --- FXML Bindings ---
    @FXML private Label dashboardRevenueLabel, dashboardTransactionsLabel,
            dashboardProfitLabel, dashboardExpenseLabel;
    @FXML private VBox criticalItemsListBox;
    @FXML private BarChart<String, Number> inventoryStockChart;
    @FXML private PieChart platformPieChart;

    // --- Members ---
    private final ItemDAO itemDAO = new ItemDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final DecimalFormat idr = new DecimalFormat("Rp #,###");

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    public void refreshDashboard() {
        loadSummaryCards();
        loadCriticalStockList();
        loadInventoryBarChart();
        loadCategoryPieChart();
    }

    /**
     * Fills the 4 top cards with aggregate data from the DB.
     */
    private void loadSummaryCards() {
        // 1. Total Revenue & Profit from SaleDAO
        double revenue = saleDAO.getTotalRevenue();
        double profit = saleDAO.getTotalProfit();
        int transactions = saleDAO.getTransactionCount();

        // 2. Total Expenses (We'll use a direct query here or create an ExpenseDAO)
        double expenses = getTotalExpenses();

        dashboardRevenueLabel.setText(idr.format(revenue));
        dashboardTransactionsLabel.setText(String.valueOf(transactions));
        dashboardProfitLabel.setText(idr.format(profit));
        dashboardExpenseLabel.setText(idr.format(expenses));
    }

    /**
     * Populates the "Stok Kritis" section using the scrollable VBox.
     */
    private void loadCriticalStockList() {
        criticalItemsListBox.getChildren().clear();
        List<Item> lowStock = itemDAO.getLowStockItems(5);

        if (lowStock.isEmpty()) {
            Label placeholder = new Label("Semua stok aman.");
            placeholder.setStyle("-fx-text-fill: #9ca3af; -fx-font-style: italic;");
            criticalItemsListBox.getChildren().add(placeholder);
            return;
        }

        for (Item item : lowStock) {
            HBox row = new HBox();
            row.setSpacing(10);
            row.setStyle("-fx-padding: 5 0; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

            Label name = new Label(item.getName());
            name.setStyle("-fx-text-fill: #4d667b; -fx-font-weight: bold; -fx-font-size: 14px;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label stock = new Label(item.getStock() + " Unit");
            stock.setStyle("-fx-text-fill: #b33f44; -fx-font-weight: 800;");

            row.getChildren().addAll(name, spacer, stock);
            criticalItemsListBox.getChildren().add(row);
        }
    }

    /**
     * Shows stock levels of the first 10 items.
     */
    private void loadInventoryBarChart() {
        inventoryStockChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<Item> items = itemDAO.getAllItems();
        // Limit to 8 items so the chart isn't crowded
        items.stream().limit(8).forEach(item -> {
            series.getData().add(new XYChart.Data<>(item.getSku(), item.getStock()));
        });

        inventoryStockChart.getData().add(series);
    }

    /**
     * Shows how items are distributed across categories.
     */
    private void loadCategoryPieChart() {
        platformPieChart.getData().clear();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        // Get data from your new DAO method
        Map<String, Integer> distribution = itemDAO.getCategoryDistribution();

        distribution.forEach((category, count) -> {
            pieData.add(new PieChart.Data(category, count));
        });

        platformPieChart.setData(pieData);
    }

    // Helper for local aggregate
    private double getTotalExpenses() {
        // As the DAO Lead, you can move this to an ExpenseDAO later
        Object result = DBConnection.getInstance().fetchOneByKeyColumn("SUM(total)", "expenses", "1", 1);
        return (result != null) ? ((Number) result).doubleValue() : 0.0;
    }
}