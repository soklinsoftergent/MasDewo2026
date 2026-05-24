package com.rplbo.app;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.db.DBInitializer;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing NeoMasDewo ERP...");

        try {
            // 1. Initialize Connection Pool (Reads from config.properties)
            // This MUST happen before any UI or Logic runs
            DBConnection.initialize();

            // 2. Ensure Database structure and seed data exists
            DBInitializer.run();

            // 3. Launch JavaFX Application
            Application.launch(MainApplication.class, args);

        } catch (Exception e) {
            System.err.println("Critical System Failure: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}