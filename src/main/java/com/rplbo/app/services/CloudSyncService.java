package com.rplbo.app.services;

import com.google.gson.Gson;
import com.rplbo.app.models.Item;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CloudSyncService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    /**
     * Mengirim barang lokal ke API online.
     * Mengembalikan ID dari cloud jika sukses.
     */
    public CompletableFuture<Integer> syncItemToCloud(Item item) {
        // 1. Map data internal ke format yang diminta API luar
        Map<String, Object> body = new HashMap<>();
        body.put("title", item.getName());
        body.put("price", item.getSellingPrice());
        body.put("description", "SKU: " + item.getSku() + " | Brand: " + item.getBrand());
        body.put("image", "https://i.pravatar.cc"); // Placeholder
        body.put("category", "electronics");

        String jsonBody = gson.toJson(body);

        // 2. Buat Request HTTP POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://fakestoreapi.com/products"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 3. Jalankan secara ASYNC (Agar UI tidak membeku saat nunggu internet)
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        Map<String, Object> res = gson.fromJson(response.body(), Map.class);
                        return ((Number) res.get("id")).intValue();
                    }
                    return null;
                });
    }
}