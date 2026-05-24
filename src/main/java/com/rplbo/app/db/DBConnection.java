package com.rplbo.app.db;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DBConnection {
    public enum Dialect {
        MYSQL,
        SQLITE
    }

    private static final int MAX_CONNECTIONS = 10;
    private final BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<>(MAX_CONNECTIONS);
    private static DBConnection instance;
    private Dialect dialect;
    // Config variables
    private String host, user, passwd, db;

    // Private constructor (Singleton)
    private DBConnection(String host, String user, String passwd, String db)
    {
        this.host = host;
        this.user = user;
        this.passwd = passwd;
        this.db = db;
        initializePool();
    }

    // Equivalent to your .initialize()
    public static synchronized DBConnection initialize(String host, String user, String passwd, String db)
    {
        if (instance == null) {
            instance = new DBConnection(host, user, passwd, db);
        }
        return instance;
    }

    public static synchronized DBConnection initialize() {
        if (instance == null) {
            Properties prop = new Properties();

            try (InputStream stream = DBConnection.class.getClassLoader().getResourceAsStream("config.properties");) {
                if (stream == null) throw new FileNotFoundException("config.properties not found");
                prop.load(stream);
                instance = new DBConnection(
                        prop.getProperty("db.host"),
                        prop.getProperty("db.user"),
                        prop.getProperty("db.pass"),
                        prop.getProperty("db.name")
                );
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Something went wrong lil bro", e);
            }
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

    private void initializePool()
    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format("jdbc:mysql://%s:3306/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta", host, db);

            for (int i = 1; i <= MAX_CONNECTIONS; i++) {
                Connection c = DriverManager.getConnection(url, user, passwd);
                connectionPool.add(c);
            }
            this.dialect = Dialect.MYSQL;
            System.out.println("MYSQL Connection Pool Initialized (" + MAX_CONNECTIONS + " slots)");
        } catch (Exception e) {
            System.err.println("MYSQL FAILED, changing to sqlite");
            setupSqliteFallback();
        }
    }

    private void setupSqliteFallback()
    {
        try {
            Class.forName("org.sqlite.JDBC");
            for (int i = 1; i < 5; i++) {
                Connection c = DriverManager.getConnection("jdbc:sqlite:masdewo.db");
                c.createStatement().execute("PRAGMA foreign_keys = ON");
                connectionPool.add(c);
            }
            this.dialect = Dialect.SQLITE;
            System.out.println("SQLITE Pool Initialized (local mode)");
        } catch (Exception e) {
            throw new IllegalStateException("Total Database Failure: No Valid Database available.");
        }
    }

//    private void connectSqlite(Exception mysqlError) {
//
//        try {
//            Class.forName("org.sqlite.JDBC");
//            this.conn = DriverManager.getConnection("jdbc:sqlite:masdewo.db");
//            this.dialect = Dialect.SQLITE;
//            try (Statement statement = conn.createStatement()) {
//                statement.execute("PRAGMA foreign_keys = ON");
//            }
//        } catch (Exception sqliteError) {
//            IllegalStateException error = new IllegalStateException("Can't connect to database", mysqlError);
//            error.addSuppressed(sqliteError);
//            throw error;
//        }
//    }

    private void connectDb()
    {
        try {
            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                String url = "jdbc:mysql://" + host + ":3306/" + db
                        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
                Class.forName("com.mysql.cj.jdbc.Driver");
                connectionPool.add(DriverManager.getConnection(url));
            }
//            String url = "jdbc:mysql://" + host + ":3306/" + db
//                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";

//            connectionPool.add(DriverManager.getConnection(url, user, passwd));
//            this.conn = DriverManager.getConnection(url, user, passwd);
            this.dialect = Dialect.MYSQL;
        } catch (Exception e) {
            e.printStackTrace();
//            connectSqlite(e);
        }
    }


    public synchronized Connection getConnection() throws SQLException{
        try {
            Connection conn = connectionPool.take();

            // If connection ded, create new one
            if (conn == null || conn.isClosed() || !conn.isValid(2)) {
                return createNewSingleConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Connection request interrupted");
        }
    }

    public synchronized void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.getAutoCommit()) {
                    conn.setAutoCommit(true);
                }
                connectionPool.offer(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Connection createNewSingleConnection() throws SQLException {
        if (dialect == Dialect.MYSQL) {
            String url = String.format("jdbc:mysql://%s:3306/%s?useSSL=false&serverTimezone=Asia/Jakarta", host, db);
            return DriverManager.getConnection(url, user, passwd);
        } else {
            return DriverManager.getConnection("jdbc:sqlite:masdewo.db");
        }
    }

//    public void ensureConnection() throws SQLException {
//        if (conn == null || conn.isClosed() || !conn.isValid(2)) {
//            connectDb();
//            if (conn == null || conn.isClosed()) {
//                throw new SQLException("Database connection is not available.");
//            }
//        }
//    }
//
//    public void closeConnection() throws SQLException {
//        if (conn != null && !conn.isClosed()) {
//            conn.close();
//        }
//        conn = null;
//    }

    // --- REPLICATING YOUR PYTHON HELPER METHODS ---

    /**
     * Replicates your fetchOneByKeyColumn
     * Usage: fetchOne("name", "items", "id", 1)
     */
    public Object fetchOneByKeyColumn(String tableColumn, String tableName, String keyColumn, Object keyValue) {
        String sql = String.format("SELECT %s FROM %s WHERE %s = ?", tableColumn, tableName, keyColumn);
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, keyValue); // PINDAH KE SINI (sebelum execute)
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseConnection(conn);
        }
        return null;
    }

    public boolean deleteByKeyColumn(String tableName, String keyColumn, Object keyValue) {

        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, keyColumn, keyValue);
        Connection conn = null;

        try {
            conn = getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, keyValue);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            releaseConnection(conn);
        }

//        try {
//            ensureConnection();
//            String sql = (String.format("DELETE FROM %s WHERE %s = ?", tableName, keyColumn, keyValue));
//            PreparedStatement pstmt = conn.prepareStatement(sql);
//            pstmt.setObject(1, keyValue);
//            return pstmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    /**
     * Replicates your insertIntoTable
     * Usage: insertIntoTable("users", Map.of("username", "admin", "email", "a@b.com"))
     */
    public boolean insertIntoTable(String tableName, Map<String, Object> data) {
        return insertIntoTableAndGetId(tableName, data) != null;
    }

    public Integer insertIntoTableAndGetId(String tableName, Map<String, Object> data) {

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            columns.add(entry.getKey());
            placeholders.add("?");
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
        Connection conn = null;

        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            releaseConnection(conn);
        }

//        try {
//            ensureConnection();
//            StringJoiner columns = new StringJoiner(", ");
//            StringJoiner placeholders = new StringJoiner(", ");
//
//            for (Map.Entry<String, Object> entry : data.entrySet()) {
//                columns.add(entry.getKey());
//                placeholders.add("?");
//            }
//
//            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
//            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//
//            int i = 1;
//            for (Map.Entry<String, Object> entry : data.entrySet()) {
//                pstmt.setObject(i++, entry.getValue());
//            }
//
//            int affectedRows = pstmt.executeUpdate();
//            if (affectedRows == 0) {
//                return null;
//            }
//
//            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
//                if (generatedKeys.next()) {
//                    return generatedKeys.getInt(1);
//                }
//            }
//            return null;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
    }

    /**
     * Replicates your updateField
     */
    public boolean updateField(String tableName, String keyColumn, Object keyValue, Map<String, Object> updates) {

        StringJoiner setClause = new StringJoiner(", ");
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            setClause.add(entry.getKey() + " = ?");
        }
        String sql = String.format("UPDATE %s SET %s WHERE %s = ?", tableName, setClause, keyColumn);
        Connection conn = null;

        try {
            conn = getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int i = 1;
                for (Map.Entry<String, Object> entry : updates.entrySet()) {
                    pstmt.setObject(i++, entry.getValue());
                }
                pstmt.setObject(i, keyValue);

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            releaseConnection(conn);
        }
//        try {
//            ensureConnection();
//            StringJoiner setClause = new StringJoiner(", ");
//            for (Map.Entry<String, Object> entry : updates.entrySet()) {
//                setClause.add(entry.getKey() + " = ?");
//            }
//
//            String sql = String.format("UPDATE %s SET %s WHERE %s = ?", tableName, setClause, keyColumn);
//            PreparedStatement pstmt = conn.prepareStatement(sql);
//
//            int i = 1;
//            for (Map.Entry<String, Object> entry : updates.entrySet()) {
//                pstmt.setObject(i++, entry.getValue());
//            }
//            pstmt.setObject(i, keyValue);
//
//            return pstmt.executeUpdate() > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    /**
     * Replicates your addColumn
     */
    public void addColumn(String tableName, String newColumn, String dataType) {
        // Validation logic similar to your regex
        if (!newColumn.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) throw new IllegalArgumentException("Invalid column name");

        String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, newColumn, dataType);

        Connection conn = null;

        try {
            conn = getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Column added successfully.");
                } else {
                    System.out.println("Cek DB deh njink");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseConnection(conn);
        }
//        try {
//            ensureConnection();
//            Statement stmt = conn.createStatement();
//            stmt.executeUpdate(sql);
//            System.out.println("Column added successfully.");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

//    private Connection getConnection() {
//        return conn;
//    }

    public Dialect getDialect() {
        return dialect;
    }

    public boolean isMySql() {
        return dialect == Dialect.MYSQL;
    }

    public boolean isSqlite() {
        return dialect == Dialect.SQLITE;
    }

    public List<Map<String, Object>> selectAll(String tableName) {
        String sql = String.format("SELECT * FROM %s", tableName);
        return getMaps(sql);
    }

    public List<Map<String, Object>> selectAllCustom(String customQuery, Object... params) {
        return getMaps(customQuery, params);
    }

    private List<Map<String, Object>> getMaps(String sql, Object... params) {
        List<Map<String, Object>> result = new ArrayList<>();

        Connection conn = null;
        try  {
//            ensureConnection();
            conn = getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    pstmt.setObject(i + 1, params[i]);
                }

                try (ResultSet rs = pstmt.executeQuery()) {

                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    // Buat row tabel

                    while (rs.next()) {

                        Map<String, Object> row = new LinkedHashMap<>();

                        for (int j = 1; j <= columnCount; j++) {
                            // Masukkan semua object di row tabel
                            row.put(meta.getColumnLabel(j), rs.getObject(j));
                        }
                        result.add(row);
                    }
                }
            }
            return result;
            /* ResultSet iku List<Map<String, Object>>
            Sajrone ResultSet iku String kang dadi row-ne
            Object kuwi luwih kerepe bakal ana akeh barange
            Dadi Object kerep bisa diloop
            Karang Objecte iku cell per column saka row tabel
            Apparently Maps can have many key value pairs
            */

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            releaseConnection(conn);
        }
        return null;
    }

    public Map<String, Object> fetchRow(String tableName, String keyColumn, Object keyValue) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, keyColumn);
        Map<String, Object> result = new LinkedHashMap<>();

        Connection conn = null;

        try {
            conn = getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, keyValue);
//                ensureConnection();
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ResultSetMetaData meta = rs.getMetaData();
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            row.put(meta.getColumnLabel(i), rs.getObject(i));
                        }
                        return row;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            releaseConnection(conn);
        }
//        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
//            ensureConnection();
//            pstmt.setObject(1, keyValue);
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//                    ResultSetMetaData meta = rs.getMetaData();
//                    Map<String, Object> row = new LinkedHashMap<>();
//                    for (int i = 1; i <= meta.getColumnCount(); i++) {
//                        row.put(meta.getColumnLabel(i), rs.getObject(i));
//                    }
//                    return row;
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    public void shutdown() {
        for (Connection c : connectionPool) {
            try {
                if (c != null) c.close();
            } catch (SQLException e) {}
        }
        System.out.println("Pool's closed");
    }

    public Map<String, Object> rowToMap(ResultSet rs) {
        Map<String, Object> row = new LinkedHashMap<>();
        try {
            int i = 1;
            while (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                row.put(meta.getColumnName(i), rs.getObject(i));
                i = i + 1;
            }
            return row;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        DBConnection.initialize();
        DBConnection db = DBConnection.getInstance();
        System.out.println(db.selectAll("users"));
        db.shutdown();
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
//Map<String, Object> user = new LinkedHashMap<>();
//user.put("username", "genti");
//user.put("email", "genti@hp.com");
//db.insertIntoTable("users", user);
//// Always use releaseConnection(conn) if you write a custom query


