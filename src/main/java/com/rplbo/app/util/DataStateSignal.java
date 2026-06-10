package com.rplbo.app.util;

public class DataStateSignal {
    // Sinyal apakah data di halaman tertentu perlu di-update
    public static boolean dashboardNeedsRefresh = false;
    public static boolean inventoryNeedsRefresh = false;
    public static boolean salesNeedsRefresh = false;
    public static boolean financeNeedsRefresh = false;

    /**
     * Panggil ini setiap kali ada perubahan di database (Sale, Restock, Edit)
     */
    public static void fireAll() {
        dashboardNeedsRefresh = true;
        inventoryNeedsRefresh = true;
        salesNeedsRefresh = true;
        financeNeedsRefresh = true;
    }
}