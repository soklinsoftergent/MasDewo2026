package com.rplbo.app.util;

import com.rplbo.app.dao.SaleDAO;
import com.rplbo.app.models.Sale;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ReceiptGenerator {
    private static final int WIDTH = 40; // Lebar standar kertas termal

    public static void generate(Sale sale, String customerName, String cashierName) {
        String fileName = "STRUK_INV_" + sale.getSaleId() + ".txt";
        String path = System.getProperty("user.home") + "/Downloads/" + fileName;

        try (FileWriter writer = new FileWriter(path)) {
            writer.write(center("MASDEWO CAMERA STORE") + "\n");
            writer.write(center("Yogyakarta, Indonesia") + "\n");
            writer.write(line() + "\n");

            writer.write("No. Inv : INV-" + String.format("%04d", sale.getSaleId()) + "\n");
            writer.write("Tgl     : " + FormatterUtil.formatDate(sale.getCreatedAt()) + "\n");
            writer.write("Kasir   : " + cashierName + "\n");
            writer.write("Cust    : " + customerName + "\n");
            writer.write(line() + "\n");

            // Ambil detail barang dari DAO
            SaleDAO dao = new SaleDAO();
            List<Map<String, Object>> items = dao.getItemsForSaleDetailed(sale.getSaleId());

            for (Map<String, Object> item : items) {
                String name = (String) item.get("item_name");
                int qty = ((Number) item.get("quantity")).intValue();
                double price = ((Number) item.get("unit_price")).doubleValue();
                double sub = ((Number) item.get("total_price")).doubleValue();

                writer.write(name + "\n");
                writer.write(formatRow(qty + " x " + FormatterUtil.formatCurrency(price), FormatterUtil.formatCurrency(sub)) + "\n");
            }

            writer.write(line() + "\n");
            writer.write(formatRow("TOTAL", FormatterUtil.formatCurrency(sale.getTotalAmount())) + "\n");
            writer.write(line() + "\n");
            writer.write(center("Terima Kasih Atas Kunjungan Anda") + "\n");
            writer.write(center("Barang yang sudah dibeli") + "\n");
            writer.write(center("tidak dapat ditukar/dikembalikan") + "\n");

            System.out.println("📄 Struk berhasil dibuat: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Helper Formatting ---
    private static String center(String text) {
        int leftPadding = (WIDTH - text.length()) / 2;
        return " ".repeat(Math.max(0, leftPadding)) + text;
    }

    private static String line() {
        return "-".repeat(WIDTH);
    }

    private static String formatRow(String left, String right) {
        int space = WIDTH - left.length() - right.length();
        return left + " ".repeat(Math.max(1, space)) + right;
    }
}