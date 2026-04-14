package com.rplbo.app.db;

import java.sql.*;
import java.util.Map;
import java.util.StringJoiner;

public class DBConnection {
    private static DBConnection instance;
    private Connection conn;

    // Config variables
    private String host, user, passwd, db;

    // Private constructor (Singleton)
    private DBConnection(String host, String user, String passwd, String db) {
        this.host = host;
        this.user = user;
        this.passwd = passwd;
        this.db = db;
        connectDb();
    }

    // Equivalent to your .initialize()
    public static synchronized DBConnection initialize(String host, String user, String passwd, String db) {
        if (instance == null) {
            instance = new DBConnection(host, user, passwd, db);
        }
        return instance;
    }

    // Equivalent to your .get_instance()
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            throw new RuntimeException("DBConnection not initialized. Call initialize() first.");
        }
        return instance;
    }

    private void connectDb() {
        try {
            String url = "jdbc:mysql://" + host + ":3306/" + db; // OR jbdc:sqlite:masdewo.db
            Class.forName("com.mysql.cj.jdbc.Driver"); // OR org.sqlite.JDBC
            this.conn = DriverManager.getConnection(url, user, passwd); // OR no user and no passwd
//            // SQLite needs this to enforce Foreign Key constraints
//            Statement stmt = conn.createStatement();
//            stmt.execute("PRAGMA foreign_keys = ON;");
        } catch (Exception e) {
            System.err.println("FATAL: Can't connect to database: " + e.getMessage());
        }
    }

    public void ensureConnection() throws SQLException {
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            connectDb();
        }
    }

    // --- REPLICATING YOUR PYTHON HELPER METHODS ---

    /**
     * Replicates your fetchOneByKeyColumn
     * Usage: fetchOne("name", "items", "id", 1)
     */
    public ResultSet fetchOneByKeyColumn(String tableColumn, String tableName, String keyColumn, Object keyValue) {
        try {
            ensureConnection();
            String sql = String.format("SELECT %s FROM %s WHERE %s = ?", tableColumn, tableName, keyColumn);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setObject(1, keyValue);
            return pstmt.executeQuery(); // In Java, we return ResultSet instead of tuple
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Replicates your insertIntoTable
     * Usage: insertIntoTable("users", Map.of("username", "admin", "email", "a@b.com"))
     */
    public boolean insertIntoTable(String tableName, Map<String, Object> data) {
        try {
            ensureConnection();
            StringJoiner columns = new StringJoiner(", ");
            StringJoiner placeholders = new StringJoiner(", ");

            for (String key : data.keySet()) {
                columns.add(key);
                placeholders.add("?");
            }

            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
            PreparedStatement pstmt = conn.prepareStatement(sql);

            int i = 1;
            for (Object value : data.values()) {
                pstmt.setObject(i++, value);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Replicates your updateField
     */
    public boolean updateField(String tableName, String keyColumn, Object keyValue, Map<String, Object> updates) {
        try {
            ensureConnection();
            StringJoiner setClause = new StringJoiner(", ");
            for (String key : updates.keySet()) {
                setClause.add(key + " = ?");
            }

            String sql = String.format("UPDATE %s SET %s WHERE %s = ?", tableName, setClause, keyColumn);
            PreparedStatement pstmt = conn.prepareStatement(sql);

            int i = 1;
            for (Object value : updates.values()) {
                pstmt.setObject(i++, value);
            }
            pstmt.setObject(i, keyValue);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Replicates your addColumn
     */
    public void addColumn(String tableName, String newColumn, String dataType) {
        // Validation logic similar to your regex
        if (!newColumn.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) throw new IllegalArgumentException("Invalid column name");

        try {
            ensureConnection();
            String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, newColumn, dataType);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println("Column added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public ResultSet selectAll(String tableName) {
        try {
            ensureConnection();
            String sql = String.format("SELECT * FROM %s", tableName);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

//EXAMPLE USAGE
//// Initialize once at start
//DBConnection.initialize("localhost", "root", "pass", "masdewo");
//
//// Use anywhere else
//DBConnection db = DBConnection.getInstance();
//
//// Insert example
//Map<String, Object> user = new HashMap<>();
//user.put("username", "genti");
//user.put("email", "genti@hp.com");
//db.insertIntoTable("users", user);

