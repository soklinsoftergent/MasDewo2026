package com.rplbo.app;

<<<<<<< HEAD
import com.rplbo.app.db.DBConnection;
import com.rplbo.app.dao.ItemDAO;
import com.rplbo.app.models.Item;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Connection (Use your dev_user credentials)
        DBConnection.initialize("127.0.0.1", "Dewa", "Supaidaa-M4n", "masdewo");
        ItemDAO itemDAO = new ItemDAO();

        System.out.println("=== 📦 STARTING ITEM DAO TEST ===");

        // TEST 1: Get All Items (Verifies your bulk data import)
        System.out.println("\nTest 1: Fetching all items...");
        List<Item> allItems = itemDAO.getAllItems();
        System.out.println("Total items found: " + allItems.size());
        if (!allItems.isEmpty()) {
            System.out.println("First item in DB: " + allItems.get(0).getName());
=======
import com.rplbo.app.dao.UserDAO;
import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import javafx.application.Application;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }

    public static void initializeDatabase() {
        try {
            Properties config = loadConfig();
            DBConnection.initialize(
                    requireProperty(config, "db.host"),
                    requireProperty(config, "db.user"),
                    sanitizePassword(requireProperty(config, "db.pass")),
                    requireProperty(config, "db.name")
            );
            ensureRuntimeSchema();
            seedDemoData();
        } catch (Exception e) {
            throw new IllegalStateException("Aplikasi gagal dijalankan: " + e.getMessage(), e);
>>>>>>> origin/joe
        }

        // TEST 2: Search Logic (Simulates the Search Bar)
        System.out.println("\nTest 2: Searching for 'Adapter'...");
        List<Item> searchResults = itemDAO.searchByName("Adapter");
        for (Item item : searchResults) {
            System.out.println("Found: " + item.getName() + " (Stock: " + item.getStock() + ")");
        }

        // TEST 3: Low Stock Badge (Simulates Dashboard alert)
        System.out.println("\nTest 3: Checking for critical stock (<= 3)...");
        List<Item> criticalItems = itemDAO.getLowStockItems(3);
        for (Item item : criticalItems) {
            System.out.println("⚠️ CRITICAL: " + item.getName() + " | Qty: " + item.getStock());
        }

        // TEST 4: Stock Update (Simulates a Sale or Restock)
        if (!allItems.isEmpty()) {
            Item firstItem = allItems.get(0);
            int oldStock = firstItem.getStock();
            int newStock = oldStock + 10;

            System.out.println("\nTest 4: Updating stock for " + firstItem.getName());
            boolean success = itemDAO.updateStock(firstItem.getId(), newStock);

            if (success) {
                System.out.println("✅ Stock updated successfully from " + oldStock + " to " + newStock);
            } else {
                System.out.println("❌ Stock update failed!");
            }
        }

        System.out.println("\n=== ✅ TEST SUITE COMPLETE ===");
    }

    private static void ensureRuntimeSchema() throws SQLException {
        DBConnection dbConnection = DBConnection.getInstance();
        Connection connection = dbConnection.getConnection();
        try (Statement statement = connection.createStatement()) {
            if (dbConnection.isSqlite()) {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS roles (" +
                                "role_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "role_name TEXT NOT NULL" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "username TEXT UNIQUE NOT NULL, " +
                                "email TEXT UNIQUE NOT NULL, " +
                                "phonenumber TEXT, " +
                                "password_hash TEXT NOT NULL, " +
                                "role_id INTEGER, " +
                                "is_active INTEGER NOT NULL DEFAULT 1, " +
                                "FOREIGN KEY (role_id) REFERENCES roles(role_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS item_types (" +
                                "it_ty_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "name TEXT NOT NULL, " +
                                "description TEXT, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS items (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "name TEXT NOT NULL, " +
                                "stock INTEGER DEFAULT 0, " +
                                "it_ty_id INTEGER, " +
                                "purchase_price REAL NOT NULL, " +
                                "selling_price REAL NOT NULL, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (it_ty_id) REFERENCES item_types(it_ty_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS ecommerces (" +
                                "ecom_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "ecom_name TEXT NOT NULL, " +
                                "ecom_platform_fee REAL DEFAULT 0.00, " +
                                "platform_url TEXT, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS ecommerce_items (" +
                                "ecom_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "item_id INTEGER, " +
                                "ecom_id INTEGER, " +
                                "price_override REAL, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id), " +
                                "FOREIGN KEY (ecom_id) REFERENCES ecommerces(ecom_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS customers (" +
                                "cust_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "name TEXT NOT NULL, " +
                                "phone_number TEXT, " +
                                "email TEXT, " +
                                "address TEXT, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS sales (" +
                                "sale_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "cust_id INTEGER, " +
                                "user_id INTEGER, " +
                                "ecom_id INTEGER, " +
                                "total_amount REAL NOT NULL, " +
                                "is_paid INTEGER DEFAULT 0, " +
                                "is_cancelled INTEGER DEFAULT 0, " +
                                "payment_method TEXT, " +
                                "logistics_fee REAL DEFAULT 0.00, " +
                                "profit REAL DEFAULT 0.00, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (cust_id) REFERENCES customers(cust_id), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                                "FOREIGN KEY (ecom_id) REFERENCES ecommerces(ecom_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS sale_items (" +
                                "sa_it_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "sale_id INTEGER, " +
                                "item_id INTEGER, " +
                                "quantity INTEGER NOT NULL, " +
                                "unit_price REAL NOT NULL, " +
                                "total_price REAL NOT NULL, " +
                                "FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS expenses (" +
                                "expense_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "user_id INTEGER, " +
                                "total REAL NOT NULL, " +
                                "description TEXT, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS expense_items (" +
                                "ex_it_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "expense_id INTEGER, " +
                                "item_id INTEGER, " +
                                "quantity INTEGER NOT NULL, " +
                                "unit_price REAL NOT NULL, " +
                                "total_price REAL NOT NULL, " +
                                "FOREIGN KEY (expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS kas_transactions (" +
                                "kas_trans_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "user_id INTEGER, " +
                                "type TEXT NOT NULL CHECK(type IN ('INCOME','EXPENSE')), " +
                                "transaction_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                                "description TEXT, " +
                                "amount REAL NOT NULL, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS kas (" +
                                "id INTEGER PRIMARY KEY, " +
                                "balance REAL NOT NULL DEFAULT 0.00" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS stock_movement_logs (" +
                                "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "item_id INTEGER, " +
                                "user_id INTEGER, " +
                                "quantity_changed INTEGER NOT NULL, " +
                                "reason TEXT, " +
                                "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")"
                );
            } else {
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS roles (" +
                                "role_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "role_name VARCHAR(50) NOT NULL" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "user_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "username VARCHAR(50) UNIQUE NOT NULL, " +
                                "email VARCHAR(100) UNIQUE NOT NULL, " +
                                "phonenumber VARCHAR(20), " +
                                "password_hash VARCHAR(255) NOT NULL, " +
                                "role_id INT, " +
                                "is_active BOOLEAN DEFAULT TRUE, " +
                                "FOREIGN KEY (role_id) REFERENCES roles(role_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS item_types (" +
                                "it_ty_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "name VARCHAR(100) NOT NULL, " +
                                "description TEXT, " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS items (" +
                                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "name VARCHAR(255) NOT NULL, " +
                                "stock INT DEFAULT 0, " +
                                "it_ty_id INT, " +
                                "purchase_price DECIMAL(15,2) NOT NULL, " +
                                "selling_price DECIMAL(15,2) NOT NULL, " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (it_ty_id) REFERENCES item_types(it_ty_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS ecommerces (" +
                                "ecom_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "ecom_name VARCHAR(100) NOT NULL, " +
                                "ecom_platform_fee DECIMAL(5,2) DEFAULT 0.00, " +
                                "platform_url VARCHAR(255), " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS ecommerce_items (" +
                                "ecom_item_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "item_id INT, " +
                                "ecom_id INT, " +
                                "price_override DECIMAL(15,2), " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id), " +
                                "FOREIGN KEY (ecom_id) REFERENCES ecommerces(ecom_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS customers (" +
                                "cust_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "name VARCHAR(255) NOT NULL, " +
                                "phone_number VARCHAR(20), " +
                                "email VARCHAR(100), " +
                                "address TEXT, " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS sales (" +
                                "sale_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "cust_id INT, " +
                                "user_id INT, " +
                                "ecom_id INT, " +
                                "total_amount DECIMAL(15,2) NOT NULL, " +
                                "is_paid BOOLEAN DEFAULT FALSE, " +
                                "is_cancelled BOOLEAN DEFAULT FALSE, " +
                                "payment_method VARCHAR(50), " +
                                "logistics_fee DECIMAL(15,2) DEFAULT 0.00, " +
                                "profit DECIMAL(15,2) DEFAULT 0.00, " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (cust_id) REFERENCES customers(cust_id), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                                "FOREIGN KEY (ecom_id) REFERENCES ecommerces(ecom_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS sale_items (" +
                                "sa_it_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "sale_id INT, " +
                                "item_id INT, " +
                                "quantity INT NOT NULL, " +
                                "unit_price DECIMAL(15,2) NOT NULL, " +
                                "total_price DECIMAL(15,2) NOT NULL, " +
                                "FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS expenses (" +
                                "expense_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "user_id INT, " +
                                "total DECIMAL(15,2) NOT NULL, " +
                                "description TEXT, " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS expense_items (" +
                                "ex_it_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "expense_id INT, " +
                                "item_id INT, " +
                                "quantity INT NOT NULL, " +
                                "unit_price DECIMAL(15,2) NOT NULL, " +
                                "total_price DECIMAL(15,2) NOT NULL, " +
                                "FOREIGN KEY (expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS kas_transactions (" +
                                "kas_trans_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "user_id INT, " +
                                "type ENUM('INCOME', 'EXPENSE') NOT NULL, " +
                                "transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "description TEXT, " +
                                "amount DECIMAL(15,2) NOT NULL, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS kas (" +
                                "id INT PRIMARY KEY, " +
                                "balance DECIMAL(15,2) NOT NULL DEFAULT 0.00" +
                                ")"
                );
                statement.executeUpdate(
                        "CREATE TABLE IF NOT EXISTS stock_movement_logs (" +
                                "log_id INT PRIMARY KEY AUTO_INCREMENT, " +
                                "item_id INT, " +
                                "user_id INT, " +
                                "quantity_changed INT NOT NULL, " +
                                "reason VARCHAR(255), " +
                                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                "FOREIGN KEY (item_id) REFERENCES items(id), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")"
                );
            }
        }

        upsertFixedId("roles", "role_id", 1, Map.of("role_name", "Admin"));
        upsertFixedId("roles", "role_id", 2, Map.of("role_name", "Staff"));
        upsertFixedId("kas", "id", 1, Map.of("balance", 0.00));
    }

    private static void seedDemoData() throws SQLException {
        if (countRows("users") == 0) {
            UserDAO userDAO = new UserDAO();
            userDAO.saveUser(new User("rafael", "rafael@masdewo.local", "password123", "081111111111", true, null));
            userDAO.saveUser(new User("gentiaras", "gentiaras@masdewo.local", "password123", "082222222222", false, null));
            userDAO.saveUser(new User("jonathan", "jonathan@masdewo.local", "password123", "083333333333", false, null));
            userDAO.saveUser(new User("anselmus", "anselmus@masdewo.local", "password123", "084444444444", false, null));
        }

        if (countRows("item_types") == 0) {
            insertWithId("item_types", Map.of("name", "Alas Kaki", "description", "Kategori alas kaki"));
            insertWithId("item_types", Map.of("name", "Pakaian", "description", "Kategori pakaian"));
            insertWithId("item_types", Map.of("name", "Aksesoris", "description", "Kategori aksesoris"));
        }

        if (countRows("ecommerces") == 0) {
            insertWithId("ecommerces", Map.of("ecom_name", "Shopee", "ecom_platform_fee", 2.5, "platform_url", "https://shopee.co.id"));
            insertWithId("ecommerces", Map.of("ecom_name", "Tokopedia", "ecom_platform_fee", 2.0, "platform_url", "https://tokopedia.com"));
            insertWithId("ecommerces", Map.of("ecom_name", "Langsung", "ecom_platform_fee", 0.0, "platform_url", "-"));
        }

        if (countRows("items") == 0) {
            int item1 = insertWithId("items", orderedMap(
                    "name", "Sepatu Sneakers Putih",
                    "stock", 2,
                    "it_ty_id", 1,
                    "purchase_price", 120000,
                    "selling_price", 185000
            ));
            int item2 = insertWithId("items", orderedMap(
                    "name", "Kaos Polos Hitam",
                    "stock", 1,
                    "it_ty_id", 2,
                    "purchase_price", 45000,
                    "selling_price", 80000
            ));
            int item3 = insertWithId("items", orderedMap(
                    "name", "Celana Chino Cream",
                    "stock", 15,
                    "it_ty_id", 2,
                    "purchase_price", 90000,
                    "selling_price", 160000
            ));
            int item4 = insertWithId("items", orderedMap(
                    "name", "Topi Baseball Merah",
                    "stock", 4,
                    "it_ty_id", 3,
                    "purchase_price", 35000,
                    "selling_price", 85000
            ));
            int item5 = insertWithId("items", orderedMap(
                    "name", "Jaket Hoodie Abu",
                    "stock", 23,
                    "it_ty_id", 2,
                    "purchase_price", 150000,
                    "selling_price", 270000
            ));

            insertWithId("ecommerce_items", orderedMap("item_id", item1, "ecom_id", 1, "price_override", 185000));
            insertWithId("ecommerce_items", orderedMap("item_id", item2, "ecom_id", 2, "price_override", 80000));
            insertWithId("ecommerce_items", orderedMap("item_id", item3, "ecom_id", 1, "price_override", 160000));
            insertWithId("ecommerce_items", orderedMap("item_id", item4, "ecom_id", 2, "price_override", 85000));
            insertWithId("ecommerce_items", orderedMap("item_id", item5, "ecom_id", 1, "price_override", 270000));
        }

        if (countRows("customers") == 0) {
            insertWithId("customers", orderedMap("name", "Andi Pratama", "phone_number", "081234500001", "email", "andi@mail.com", "address", "Bandung"));
            insertWithId("customers", orderedMap("name", "Siti Rahayu", "phone_number", "081234500002", "email", "siti@mail.com", "address", "Jakarta"));
            insertWithId("customers", orderedMap("name", "Dewi Kurnia", "phone_number", "081234500003", "email", "dewi@mail.com", "address", "Bogor"));
            insertWithId("customers", orderedMap("name", "Yanto Sudibyo", "phone_number", "081234500004", "email", "yanto@mail.com", "address", "Depok"));
        }

        if (countRows("sales") == 0) {
            int sale1 = insertWithId("sales", orderedMap("cust_id", 1, "user_id", 1, "ecom_id", 1, "total_amount", 370000, "is_paid", true, "is_cancelled", false, "payment_method", "TRANSFER", "logistics_fee", 0, "profit", 130000));
            int sale2 = insertWithId("sales", orderedMap("cust_id", 2, "user_id", 3, "ecom_id", 2, "total_amount", 160000, "is_paid", true, "is_cancelled", false, "payment_method", "TRANSFER", "logistics_fee", 0, "profit", 70000));
            int sale3 = insertWithId("sales", orderedMap("cust_id", 3, "user_id", 1, "ecom_id", 3, "total_amount", 520000, "is_paid", false, "is_cancelled", false, "payment_method", "COD", "logistics_fee", 0, "profit", 180000));
            int sale4 = insertWithId("sales", orderedMap("cust_id", 4, "user_id", 2, "ecom_id", 1, "total_amount", 270000, "is_paid", true, "is_cancelled", false, "payment_method", "TRANSFER", "logistics_fee", 0, "profit", 120000));

            insertWithId("sale_items", orderedMap("sale_id", sale1, "item_id", 1, "quantity", 2, "unit_price", 185000, "total_price", 370000));
            insertWithId("sale_items", orderedMap("sale_id", sale2, "item_id", 3, "quantity", 1, "unit_price", 160000, "total_price", 160000));
            insertWithId("sale_items", orderedMap("sale_id", sale3, "item_id", 5, "quantity", 1, "unit_price", 270000, "total_price", 270000));
            insertWithId("sale_items", orderedMap("sale_id", sale3, "item_id", 4, "quantity", 3, "unit_price", 85000, "total_price", 250000));
            insertWithId("sale_items", orderedMap("sale_id", sale4, "item_id", 5, "quantity", 1, "unit_price", 270000, "total_price", 270000));
        }

        if (countRows("expenses") == 0) {
            int expense1 = insertWithId("expenses", orderedMap("user_id", 1, "total", 450000, "description", "Restok Barang"));
            int expense2 = insertWithId("expenses", orderedMap("user_id", 2, "total", 200000, "description", "Biaya Operasional"));
            insertWithId("expense_items", orderedMap("expense_id", expense1, "item_id", 1, "quantity", 5, "unit_price", 90000, "total_price", 450000));
            insertWithId("expense_items", orderedMap("expense_id", expense2, "item_id", 2, "quantity", 4, "unit_price", 50000, "total_price", 200000));
        }

        if (countRows("kas_transactions") == 0) {
            insertWithId("kas_transactions", orderedMap("user_id", 1, "type", "INCOME", "transaction_date", "2026-04-08 10:00:00", "description", "Penjualan #INV-0041", "amount", 370000));
            insertWithId("kas_transactions", orderedMap("user_id", 1, "type", "EXPENSE", "transaction_date", "2026-04-07 13:00:00", "description", "Restok Barang", "amount", 450000));
            insertWithId("kas_transactions", orderedMap("user_id", 3, "type", "INCOME", "transaction_date", "2026-04-07 15:00:00", "description", "Penjualan #INV-0040", "amount", 160000));
            insertWithId("kas_transactions", orderedMap("user_id", 2, "type", "EXPENSE", "transaction_date", "2026-04-06 11:00:00", "description", "Biaya Operasional", "amount", 200000));
            insertWithId("kas_transactions", orderedMap("user_id", 1, "type", "INCOME", "transaction_date", "2026-04-06 16:30:00", "description", "Penjualan #INV-0039", "amount", 520000));
        }

        try (PreparedStatement stmt = DBConnection.getInstance().getConnection()
                .prepareStatement("UPDATE kas SET balance = (SELECT COALESCE(SUM(CASE WHEN type='INCOME' THEN amount ELSE -amount END),0) FROM kas_transactions) WHERE id=1")) {
            stmt.executeUpdate();
        }
    }

    private static int countRows(String table) throws SQLException {
        try (PreparedStatement stmt = DBConnection.getInstance().getConnection().prepareStatement("SELECT COUNT(*) FROM " + table);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int insertWithId(String table, Map<String, Object> data) {
        Integer id = DBConnection.getInstance().insertIntoTableAndGetId(table, data);
        return id == null ? 0 : id;
    }

    private static void upsertFixedId(String table, String idColumn, Object idValue, Map<String, Object> data) throws SQLException {
        if (countById(table, idColumn, idValue) > 0) {
            return;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        values.put(idColumn, idValue);
        values.putAll(data);
        insertWithId(table, values);
    }

    private static int countById(String table, String idColumn, Object idValue) throws SQLException {
        try (PreparedStatement stmt = DBConnection.getInstance().getConnection()
                .prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE " + idColumn + " = ?")) {
            stmt.setObject(1, idValue);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static Map<String, Object> orderedMap(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    private static Properties loadConfig() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new IOException("config.properties tidak ditemukan di resources.");
            }
            properties.load(inputStream);
        }
        return properties;
    }

    private static String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Property " + key + " belum diisi.");
        }
        return value.trim();
    }

    private static String sanitizePassword(String password) {
        if (password.length() >= 2 && password.startsWith("\"") && password.endsWith("\"")) {
            return password.substring(1, password.length() - 1);
        }
        return password;
    }
}
