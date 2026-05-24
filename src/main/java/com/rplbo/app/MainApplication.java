package com.rplbo.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // The DB is already initialized by Main.java before launch()
        // So we go straight to loading the UI.

        URL dashboardUrl = getClass().getResource("/com/rplbo/app/pages/MainDashboard.fxml");

        if (dashboardUrl == null) {
            // Helpful error for the "Fuckers" on the team
            throw new IOException("FATAL: MainDashboard.fxml not found! Check src/main/resources/com/rplbo/app/pages/");
        }

        FXMLLoader loader = new FXMLLoader(dashboardUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("MasDewo Management System - Dashboard");

        // Responsive size for the Dashboard
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        stage.setScene(scene);
        stage.show();
    }
}