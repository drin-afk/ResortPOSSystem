package util;

import model.User;

public class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** True kung naka-login at ang role ay tugma (hal. "ADMIN"). */
    public static boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equalsIgnoreCase(role);
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isCashier() {
        return hasRole("CASHIER");
    }

    public static boolean isCustomer() {
        return hasRole("CUSTOMER");
    }
}