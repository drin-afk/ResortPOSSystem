package util;

import java.math.BigDecimal;

public class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /** 4 to 20 characters: letters, numbers, and underscores only. */
    public static boolean isValidUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{4,20}");
    }

    /** At least 6 characters. */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /** Philippine mobile number: 09XXXXXXXXX (11 digits). */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^09\\d{9}$");
    }

    /** Full name: letters, spaces, periods, hyphens, and apostrophes. */
    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2
                && name.matches("^[A-Za-zÑñ .'-]+$");
    }

    /** True if an integer and greater than 0 (e.g., "3"). */
    public static boolean isPositiveInt(String text) {
        try {
            return Integer.parseInt(text.trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** True if an integer and 0 or greater. */
    public static boolean isNonNegativeInt(String text) {
        try {
            return Integer.parseInt(text.trim()) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** True if a valid monetary amount and 0 or greater (e.g., "150.50"). */
    public static boolean isValidMoney(String text) {
        try {
            return new BigDecimal(text.trim()).compareTo(BigDecimal.ZERO) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Converts text to BigDecimal. Call only after isValidMoney. */
    public static BigDecimal toMoney(String text) {
        return new BigDecimal(text.trim());
    }
}