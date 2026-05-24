package com.rplbo.app;

public class AppLauncher {
    public static void main(String[] args) {
        // This is a normal class that doesn't extend Application.
        // It "tricks" the JVM into skipping the JavaFX module check.
        Main.main(args);
    }
}