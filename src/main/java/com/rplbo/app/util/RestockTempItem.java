package com.rplbo.app.util;

import com.rplbo.app.models.*;

public class RestockTempItem {
    private Item item;
    private int quantity;
    private Supplier supplier;

    public RestockTempItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() { return this.item; }
    public int getQuantity() { return this.quantity; }

    // TAMBAHKAN INI: Agar quantity bisa diupdate (untuk deduplikasi)
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getSubtotal() { return item.getPurchasePrice() * quantity; }
    public void setSupplier(Supplier s) { this.supplier = s; }
    public Supplier getSupplier() { return this.supplier; }
}