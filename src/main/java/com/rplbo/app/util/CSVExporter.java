package com.rplbo.app.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CSVExporter {

    /**
     * The ONLY method you need.
     * It automatically reads whatever columns you defined in your SQL.
     */
    public static void exportData(List<Map<String, Object>> data, String fileName) {
        if (data == null || data.isEmpty()) {
            System.err.println("❌ Export failed: No data provided.");
            return;
        }

        // Save to the user's Downloads folder
        String path = System.getProperty("user.home") + "/Downloads/" + fileName + ".csv";

        try (FileWriter writer = new FileWriter(path)) {
            // 1. Get headers automatically from the Map keys
            Set<String> headers = data.get(0).keySet();
            writer.append(String.join(",", headers)).append("\n");

            // 2. Write rows dynamically
            for (Map<String, Object> row : data) {
                StringBuilder line = new StringBuilder();
                for (String header : headers) {
                    Object value = row.get(header);

                    // Cleanup: Replace commas in data with spaces so they don't break the CSV columns
                    String cleanValue = (value != null) ? String.valueOf(value).replace(",", " ") : "-";
                    line.append(cleanValue).append(",");
                }
                // Remove trailing comma and add newline
                writer.append(line.substring(0, line.length() - 1)).append("\n");
            }

            System.out.println("✅ CSV Exported successfully to: " + path);

        } catch (IOException e) {
            System.err.println("❌ System Error writing CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
}