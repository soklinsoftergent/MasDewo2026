package com.rplbo.app.ui;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.KasTransaction;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FinanceController implements Refreshable {

    // --- FXML Bindings ---
    @FXML private Label cashBalanceLabel, totalIncomeLabel, totalExpenseLabel;
    @FXML private TableView<KasTransaction> financeTable;
    @FXML private TableColumn<KasTransaction, String> financeDescriptionColumn, financeDateColumn,
            financeAmountColumn, financeTypeColumn;

    // --- Members ---
    private final ObservableList<KasTransaction> transactionData = FXCollections.observableArrayList();
    private final DecimalFormat idr = new DecimalFormat("Rp #,###");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTableColumns();
        refresh();
    }

    private void setupTableColumns() {
        // 1. Basic Mappings
        financeDescriptionColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        financeDateColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransactionDate().format(dateFormatter)));

        // 2. Type Column with Badge Styling
        financeTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getType().equals("INCOME") ? "Pemasukan" : "Pengeluaran"
        ));
        financeTypeColumn.setCellFactory(col -> createTypeBadgeCell());

        // 3. Amount Column with Color logic
        financeAmountColumn.setCellValueFactory(d -> {
            String prefix = d.getValue().getType().equals("INCOME") ? "+ " : "- ";
            return new SimpleStringProperty(prefix + idr.format(d.getValue().getAmount()));
        });
        financeAmountColumn.setCellFactory(col -> createAmountColorCell());
    }

    @Override
    public void refresh() {
        loadSummaryCards();
        loadTransactionHistory();
    }

    private void loadSummaryCards() {
        DBConnection db = DBConnection.getInstance();

        // Fetch Balance from the 'kas' table (ID 1)
        Map<String, Object> kasRow = db.fetchRow("kas", "id", 1);
        double balance = (kasRow != null) ? ((Number) kasRow.get("balance")).doubleValue() : 0.0;

        // Calculate Totals from transactions
        double income = queryAggregate("SELECT SUM(amount) FROM kas_transactions WHERE type = 'INCOME'");
        double expense = queryAggregate("SELECT SUM(amount) FROM kas_transactions WHERE type = 'EXPENSE'");

        cashBalanceLabel.setText(idr.format(balance));
        totalIncomeLabel.setText(idr.format(income));
        totalExpenseLabel.setText(idr.format(expense));
    }

    private void loadTransactionHistory() {
        List<KasTransaction> list = new ArrayList<>();
        List<Map<String, Object>> data = DBConnection.getInstance().selectAll("kas_transactions");

        // Standardize: Newest transactions at the top
        for (int i = data.size() - 1; i >= 0; i--) {
            list.add(new KasTransaction(data.get(i)));
        }

        transactionData.setAll(list);
        financeTable.setItems(transactionData);
    }

    // --- UI Helper: Badge/Color Styling ---

    private TableCell<KasTransaction, String> createTypeBadgeCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(type);
                    String color = type.equals("Pemasukan") ? "#79e07c" : "#ff8e8e";
                    String textColor = type.equals("Pemasukan") ? "#1f2937" : "white";
                    badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: " + textColor +
                            "; -fx-padding: 3 10; -fx-background-radius: 5; -fx-font-weight: bold;");
                    setGraphic(badge);
                }
            }
        };
    }

    private TableCell<KasTransaction, String> createAmountColorCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(amount);
                    setStyle(amount.startsWith("+") ? "-fx-text-fill: #79e07c; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ff8e8e; -fx-font-weight: bold;");
                }
            }
        };
    }

    private double queryAggregate(String sql) {
        // A quick helper to avoid writing the full DAO logic for simple sums
        try (java.sql.Connection conn = DBConnection.getInstance().getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            double res = rs.next() ? rs.getDouble(1) : 0.0;
            DBConnection.getInstance().releaseConnection(conn);
            return res;
        } catch (Exception e) { return 0.0; }
    }
}