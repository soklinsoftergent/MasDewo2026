package com.rplbo.app.util;

import com.rplbo.app.db.DBConnection;

public class SkuGenerator {

    /**
     * Menghasilkan SKU secara otomatis dengan mencari nama kategori ke DB.
     */
    public static String generate(int itemId, int typeId, String brand, String model) {
        // 1. Ambil Nama Kategori dari Database (Otomatis!)
        Object res = DBConnection.getInstance().fetchOneByKeyColumn("name", "item_types", "it_ty_id", typeId);
        String typeName = (res != null) ? res.toString() : "GENERIC";

        // 2. Persingkat Type (L Plate -> LPL)
        String typePart = typeName.replaceAll("[ a-z]", "").toUpperCase();
        if (typePart.length() < 3) typePart = typeName.substring(0, Math.min(typeName.length(), 3)).toUpperCase();

        // 3. Persingkat Brand & Model
        String brandPart = (brand != null && brand.length() >= 3) ? brand.substring(0, 3).toUpperCase() : "GEN";
        String modelPart = (model != null && model.length() >= 3) ? model.substring(0, 3).toUpperCase() : "MDL";

        // 4. Gabungkan dengan ID unik
        return String.format("%s-%s-%s-%03d", typePart, brandPart, modelPart, itemId);
    }
}