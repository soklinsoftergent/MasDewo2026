package com.rplbo.app.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Regex untuk Email Standar
    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    // Regex Password: Minimal 8 karakter, ada angka, huruf besar, huruf kecil, dan simbol
    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$";

    private static final Pattern EMAIL_REGEX =
            Pattern.compile(EMAIL_PATTERN);

    private static final Pattern PASSWORD_REGEX =
            Pattern.compile(PASSWORD_PATTERN);

    /**
     * Cek apakah format email valid.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_REGEX.matcher(email).matches();
    }

    /**
     * Cek apakah password kuat (Strong Password).
     */
    public static boolean isStrongPassword(String password) {
        if (password == null) return false;
        return PASSWORD_REGEX.matcher(password).matches();
    }
}