package com.rplbo.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        Main.initializeDatabase();

        URL dashboardUrl = MainApplication.class.getResource("/com/rplbo/app/pages/MainDashboard.fxml");
        if (dashboardUrl == null) {
            throw new IOException("MainDashboard.fxml tidak ditemukan di /com/rplbo/app/pages/");
        }

        FXMLLoader loader = new FXMLLoader(dashboardUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("MasDewo Dashboard");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
    }
}
