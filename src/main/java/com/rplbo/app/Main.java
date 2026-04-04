package com.rplbo.app;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.dao.UserDAO;
import com.rplbo.app.models.User;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize the singleton
        DBConnection.initialize("127.0.0.1", "Dewa", "Supaidaa-M4n", "masdewo");

        // 2. Create the DAO
        UserDAO userDAO = new UserDAO();

        // 3. Try to fetch the 'admin' user you inserted via Ubuntu Terminal
        User admin = userDAO.getUserByUsername("Admin");

        if (admin != null) {
            System.out.println("✅ Found User in DB!");
            System.out.println(admin.toString());

            // 4. Test the "Active Record" update
            System.out.println("Testing Update: Changing Email Address...");
            admin.setUserEmail("gentisamudra@gmail.com");
            System.out.println("Check your Ubuntu terminal: SELECT email FROM users WHERE username='admin';");
        } else {
            System.out.println("❌ User 'admin' not found. Did you run the SQL seed script?");
        }
    }
}