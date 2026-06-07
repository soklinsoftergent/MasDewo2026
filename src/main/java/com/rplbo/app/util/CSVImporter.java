package com.rplbo.app.util;

import com.rplbo.app.models.Item;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CSVImporter {
    /**
     * Format CSV: Nama,Brand,Model,Stok,ID_Kategori,HargaBeli,HargaJual
     */
    public static List<Item> parseItems(File file) {
        List<Item> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip header

                String[] v = line.split(",");
                if (v.length < 7) continue;

                Item item = new Item(
                        v[0].trim(), // Name
                        v[1].trim(), // Brand
                        v[2].trim(), // Model
                        Integer.parseInt(v[3].trim()), // Stock
                        Integer.parseInt(v[4].trim()), // Category ID
                        Double.parseDouble(v[5].trim()), // Buy Price
                        Double.parseDouble(v[6].trim())  // Sell Price
                );
                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}