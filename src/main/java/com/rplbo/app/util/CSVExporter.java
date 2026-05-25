package com.rplbo.app.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVExporter {

    public static boolean export(List<Map<String, Object>> data, String filename) {
        if (data == null || data.isEmpty()) {
            System.err.println("data is null or empty");
            return false;
        }

        String path = System.getProperty("user.home") + "/Downloads/" + filename + ".csv";

        try (FileWriter writer = new FileWriter(path)) {
            Set<String> headers = data.get(0).keySet();
            writer.append(String.join(",", headers)).append("\n");

            for (Map<String, Object> row : data) {
                StringBuilder line = new StringBuilder();
                for (String header : headers) {
                    Object value = row.get(header);
                    String cleanValue = (value != null) ? value.toString().replace(",", " ") : "";
                    line.append(cleanValue).append(",");
                }
                writer.append(line.substring(0, line.length() - 1)).append("\n");
            }

            System.out.println("Laporan berhasi disimpan di: " + path);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void exportSales(List<Map<String, Object>> data, String fileName) {
        String path = System.getProperty("user.home") + "/Downloads/" + fileName + ".csv";

        try (FileWriter writer = new FileWriter(path)) {
            writer.append("ID,Pelanggan,Total,Tanggal,Untung\n");

            for (Map<String, Object> row : data) {
                // Gunakan String.valueOf() karena ia aman terhadap null (mengembalikan string "null" bukannya crash)
                writer.append(String.valueOf(row.get("sale_id"))).append(",")
                        .append(String.valueOf(row.getOrDefault("customer_name", "Guest"))).append(",")
                        .append(String.valueOf(row.get("total_amount"))).append(",")
                        .append(String.valueOf(row.get("created_at"))).append(",")
                        .append(String.valueOf(row.get("profit"))).append("\n");
            }
            System.out.println("✅ CSV Exported to: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
