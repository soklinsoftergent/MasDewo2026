package com.rplbo.app.dao;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.Item;
import com.rplbo.app.models.ItemType;
import com.rplbo.app.util.InventoryTrie;
import java.sql.*;
import java.util.*;

public class ItemDAO {
    private static final InventoryTrie searchTree = new InventoryTrie();
    private final DBConnection db = DBConnection.getInstance();

    public void initializeSearchTree() {
        // Karena static, kita harus bersihkan dulu agar tidak duplikat saat refresh
        searchTree.clear();
        List<Item> all = getAllItems();
        for (Item item : all) {
            searchTree.insert(item);
        }
        System.out.println("🌳 [System] Search tree initialized with " + all.size() + " items.");
    }

    public List<Item> searchFast(String query) {
        return searchTree.search(query);
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        List<Map<String, Object>> data = db.selectAll("items");
        for (Map<String, Object> row : data) {
            items.add(new Item(row));
        }
        return items;
    }

    /**
     * UPDATE STOK DENGAN OPTIMISTIC LOCKING
     * Menjamin data tidak tabrakan jika diakses banyak user.
     */
    public boolean updateStockSecure(int itemId, int newStock, int currentVersion) {
        String sql = "UPDATE items SET stock = ?, version = version + 1 WHERE id = ? AND version = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newStock);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, currentVersion);

            int affected = pstmt.executeUpdate();
            return affected > 0; // Mengembalikan false jika version sudah berubah (diedit orang lain)
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getLowStockItems(int threshold) {
        List<Item> items = new ArrayList<>();
        // Gunakan selectAllCustom agar lebih fleksibel
        String sql = "SELECT * FROM items WHERE stock <= ?";
        List<Map<String, Object>> rows = db.selectAllCustom(sql, threshold);
        for (Map<String, Object> row : rows) {
            items.add(new Item(row));
        }
        return items;
    }

    public String getTypeNameById(int typeId) {
        Object name = db.fetchOneByKeyColumn("name", "item_types", "it_ty_id", typeId);
        String typeName = (String) name;
        return (name != null) ? typeName : "Unknown";
    }

    // Metode Restock yang sudah diperbarui dengan Audit Trail
    public boolean restockItem(int itemId, int quantity, int userId) {
        Map<String, Object> data = db.fetchRow("items", "id", itemId);
        if (data == null) return false;

        Item item = new Item(data);
        int newStock = item.getStock() + quantity;

        // Gunakan update secure
        if (updateStockSecure(item.getId(), newStock, item.getVersion())) {
            new StockMovementDAO().logChange(itemId, userId, quantity, "RESTOK_MANUAL");
            return true;
        }
        return false;
    }

    /**
     * Mengambil semua kategori (ItemType) dari database.
     * Digunakan untuk mengisi dropdown/ComboBox di UI.
     */
    public List<ItemType> getAllTypes() {
        List<ItemType> types = new ArrayList<>();

        // 1. Ambil data mentah (List of Maps) dari tabel item_types
        List<Map<String, Object>> data = db.selectAll("item_types");

        if (data != null) {
            for (Map<String, Object> row : data) {
                // 2. Gunakan Constructor Map yang sudah kita refactor di model ItemType
                types.add(new ItemType(row));
            }
        }

        return types;
    }

    /**
     * Menghitung jumlah jenis barang per kategori untuk ditampilkan di PieChart.
     * Query ini menggabungkan tabel items dan item_types.
     */
    public Map<String, Integer> getCategoryDistribution() {
        Map<String, Integer> distribution = new HashMap<>();

        // SQL dengan JOIN dan Alias 'total'
        String sql = "SELECT t.name, COUNT(i.id) AS total " +
                "FROM items i " +
                "JOIN item_types t ON i.it_ty_id = t.it_ty_id " +
                "GROUP BY t.name";

        // Panggil selectAllCustom dari DBConnection
        List<Map<String, Object>> rows = db.selectAllCustom(sql);

        if (rows != null) {
            for (Map<String, Object> row : rows) {
                String categoryName = (String) row.get("name");

                // Ambil hasil COUNT (biasanya bertipe Long di JDBC)
                Object totalObj = row.get("total");
                int count = (totalObj != null) ? ((Number) totalObj).intValue() : 0;

                distribution.put(categoryName, count);
            }
        }

        // Jika database kosong, berikan data dummy agar chart tidak error (Optional)
        if (distribution.isEmpty()) {
            distribution.put("Belum ada data", 1);
        }

        return distribution;
    }

    public boolean batchInsert(List<Item> items) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            for (Item item : items) {
                // Kita butuh nama kategori untuk generate SKU otomatis
                String typeName = getTypeNameById(item.getItTyId());

                // Simpan menggunakan logic save() yang sudah ada
                // Tapi karena kita dalam transaksi manual, kita panggil metode insert kustom
                if (!item.save()) {
                    throw new SQLException("Gagal menyimpan item: " + item.getName());
                }
            }

            conn.commit();
            initializeSearchTree(); // RE-INDEX TRIE setelah batch sukses
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            db.releaseConnection(conn);
        }
    }
}