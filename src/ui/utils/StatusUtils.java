package ui.utils;

import com.google.gson.JsonObject;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling various status-related operations in the library management system.
 * Provides methods to get status messages, validate book fields, and manage card layouts.
 */
public class StatusUtils {
    private static final Logger logger = Logger.getLogger(StatusUtils.class.getName());

    /**
     * Gets the status message for a book based on its status code.
     *
     * @param statusCode The status code of the book
     * @return A user-friendly status message
     */
    public static String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case 1 -> core.ResourceManager.getString("status.ontime");
            case 0 -> core.ResourceManager.getString("status.duetoday");
            case -1 -> core.ResourceManager.getString("status.overdue");
            default -> core.ResourceManager.getString("status.unknown");
        };
    }

    /**
     * Gets the name of the current card in the card layout.
     *
     * @return The name of the current card or "unknown" if it cannot be determined
     */
    public static String getCurrentCardName(CardLayout cardLayout) {
        try {
            String layoutString = cardLayout.toString();
            if (layoutString.contains("login")) {
                return "login";
            } else if (layoutString.contains("main")) {
                return "main";
            }
            return "unknown";
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error determining current card layout", e);
            return "unknown";
        }
    }

    /**
     * Validates that a book JSON object contains all required fields.
     *
     * @param book The JsonObject representing a book to validate
     * @return true if all required fields are present or if book is null (for safety), false if any required field is missing
     */
    public static boolean hasRequiredBookFields(JsonObject book) {
        if (book == null) {
            logger.log(Level.WARNING, "Book validation failed: null book object");
            return true;
        }

        String[] requiredFields = {"BookID", "Title", "Author", "Publisher", "Available"};
        for (String field : requiredFields) {
            if (!book.has(field)) {
                logger.log(Level.WARNING, "Book validation failed: missing field '" + field + "'");
                return true;
            }
        }

        logger.log(Level.FINE, "Book validation passed for book: " + book.get("BookID").getAsString());
        return false;
    }

    /**
     * Validates the fields for adding a new book.
     * Checks if any of the required fields are empty.
     *
     * @param shelfNumber The shelf number of the book
     * @param title The title of the book
     * @param author The author of the book
     * @param publisher The publisher of the book
     * @param available The availability status of the book
     * @param onLoan The on-loan status of the book
     * @return true if any field is empty, false otherwise
     */
    public static boolean validateNewBookFields(String shelfNumber, String title, String author, String publisher, String available, String onLoan) {
        return shelfNumber.isEmpty() || title.isEmpty() || author.isEmpty() || publisher.isEmpty() || available.isEmpty() || onLoan.isEmpty();
    }

    /**
     * Validates that the numeric fields for a new book are valid integers.
     * If any field is not a valid integer, it logs the error and returns true.
     *
     * @param shelfNumber The shelf number of the book
     * @param available The number of available copies
     * @param onLoan The number of copies currently on loan
     * @return true if any field is not a valid integer, false otherwise
     */
    public static boolean validateNumericNewBookFields(String shelfNumber, String available, String onLoan) {
        try {
            Integer.parseInt(shelfNumber);
            Integer.parseInt(available);
            Integer.parseInt(onLoan);
            return false;
        } catch (NumberFormatException e) {
            logger.log(Level.INFO, "Numeric validation failed for fields: " + e.getMessage());
            return true;
        }
    }

    public static boolean validateNewUserFields(String userId, String userName, String userPassword, String userType){
        boolean emptyFields = userId.isEmpty() || userName.isEmpty() || userPassword.isEmpty() || userType.isEmpty();
        boolean isInteger = Integer.parseInt(userId) >= 0;

        if (emptyFields || !isInteger){
            logger.log(Level.INFO, "User validation failed: empty fields or invalid user ID");
            return true;
        } else {
            logger.log(Level.FINE, "User validation passed for user: " + userId);
            return false;
        }
    }
}
