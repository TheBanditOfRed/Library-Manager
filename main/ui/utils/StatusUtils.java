package main.ui.utils;

import com.google.gson.JsonObject;
import main.core.ResourceManager;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            case 1 -> ResourceManager.getString("status.ontime");
            case 0 -> ResourceManager.getString("status.duetoday");
            case -1 -> ResourceManager.getString("status.overdue");
            default -> ResourceManager.getString("status.unknown");
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
}
