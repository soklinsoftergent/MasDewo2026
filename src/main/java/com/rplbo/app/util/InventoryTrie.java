package com.rplbo.app.util;

import com.rplbo.app.models.Item;
import java.util.*;

public class InventoryTrie {
    private class Node {
        Map<Character, Node> children = new HashMap<>();
        List<Item> items = new ArrayList<>(); // Store items that match this prefix
    }

    private final Node root = new Node();

    // 1. Logic to insert an item name into the tree
    public void insert(Item item) {
        Node current = root;
        String searchKey = (item.getName() + " " + item.getSku()).toLowerCase();

        for (char c : searchKey.toCharArray()) {
            current.children.putIfAbsent(c, new Node());
            current = current.children.get(c);
            // Every node along the path knows this item exists below it
            if (!current.items.contains(item)) {
                current.items.add(item);
            }
//            current.items.add(item);
        }
    }

    // 2. Logic to search the tree
    public List<Item> search(String prefix) {

        if (prefix == null || prefix.isEmpty()) return new ArrayList<>();

        Node current = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            current = current.children.get(c);
            if (current == null) return new ArrayList<>(); // No match
        }
        return current.items; // Return all items matching this prefix
    }

    public void clear() {
        root.children.clear();
        root.items.clear();
    }
}