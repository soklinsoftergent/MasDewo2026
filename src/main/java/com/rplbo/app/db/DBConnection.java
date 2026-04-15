package com.rplbo.app.db;

import java.sql.*;
import java.util.Map;
import java.util.StringJoiner;

public class DBConnection {
    public enum Dialect {
        MYSQL,
        SQLITE
    }

    private static DBConnection instance;
    private Connection conn;
    private Dialect dialect;

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
            String url = "jdbc:mysql://" + host + ":3306/" + db
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conn = DriverManager.getConnection(url, user, passwd);
            this.dialect = Dialect.MYSQL;
        } catch (Exception e) {
            connectSqlite(e);
        }
    }

    private void connectSqlite(Exception mysqlError) {
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection("jdbc:sqlite:masdewo.db");
            this.dialect = Dialect.SQLITE;
            try (Statement statement = conn.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
        } catch (Exception sqliteError) {
            IllegalStateException error = new IllegalStateException("Can't connect to database", mysqlError);
            error.addSuppressed(sqliteError);
            throw error;
        }
    }

    public void ensureConnection() throws SQLException {
        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
            connectDb();
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Database connection is not available.");
            }
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
        return insertIntoTableAndGetId(tableName, data) != null;
    }

    public Integer insertIntoTableAndGetId(String tableName, Map<String, Object> data) {
        try {
            ensureConnection();
            StringJoiner columns = new StringJoiner(", ");
            StringJoiner placeholders = new StringJoiner(", ");

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                columns.add(entry.getKey());
                placeholders.add("?");
            }

            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            int i = 1;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                pstmt.setObject(i++, entry.getValue());
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Replicates your updateField
     */
    public boolean updateField(String tableName, String keyColumn, Object keyValue, Map<String, Object> updates) {
        try {
            ensureConnection();
            StringJoiner setClause = new StringJoiner(", ");
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                setClause.add(entry.getKey() + " = ?");
            }

            String sql = String.format("UPDATE %s SET %s WHERE %s = ?", tableName, setClause, keyColumn);
            PreparedStatement pstmt = conn.prepareStatement(sql);

            int i = 1;
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                pstmt.setObject(i++, entry.getValue());
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

    public Dialect getDialect() {
        return dialect;
    }

    public boolean isMySql() {
        return dialect == Dialect.MYSQL;
    }

    public boolean isSqlite() {
        return dialect == Dialect.SQLITE;
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

