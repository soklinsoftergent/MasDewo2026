package com.rplbo.app.util;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormatterUtil {
    // Satu format untuk semua!
    private static final DecimalFormat IDR_FORMAT = new DecimalFormat("Rp #,###");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public static String formatCurrency(double amount) {
        return IDR_FORMAT.format(amount);
    }

    public static String formatDate(LocalDateTime date) {
        if (date == null) return "-";
        return date.format(DATE_FORMAT);
    }
}