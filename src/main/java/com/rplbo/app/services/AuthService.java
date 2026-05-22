package com.rplbo.app.services;

import com.rplbo.app.db.DBConnection;
import com.rplbo.app.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

public class AuthService {

    public boolean authenticate(String username, String password) {
        Map<String, Object> userData = DBConnection.getInstance().fetchRow("users", "username", username);

        if (userData == null) {
            String storedHash = (String) userData.get("password_hash");

            if (BCrypt.checkpw(password, storedHash)) {

                User user = new User(userData);

                if (!user.isActive()) {
                    System.out.println("Login Blocked: User is inactive.");
                    return false;
                }

                UserSession.getInstance().login(user);
                return true;
            }
        }
        return false;
    }

    public void logout() {
        UserSession.getInstance().logout();
        // Logic to switch back to LoginWindow.fxml goes here
    }
}
