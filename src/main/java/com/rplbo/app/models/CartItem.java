package com.rplbo.app.models;

import com.rplbo.app.models.Item;

public class CartItem {
    private Item item;
    private int quantity;
    private double subtotal;

    public CartItem(Item item, int qty) {
        this.item = item;
        this.quantity = qty;
        this.subtotal = item.getSellingPrice() * qty;
    }
    // Getters...

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}