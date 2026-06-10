package com.rplbo.app.models;

// src/main/java/com/rplbo/app/models/CartItem.java
public class CartItem {
    private final Item item;
    private int quantity;
    private double subtotal;

    public CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
        this.subtotal = item.getSellingPrice() * quantity;
    }
    // Getters...
    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return subtotal; }
}