package main.ui;

import com.google.gson.JsonObject;
import main.core.ResourceManager;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for GUI-related helper methods.
 */
public class GuiHelper {

    /**
     * Checks if a row is selected in the given JTable.
     * If no row is selected, displays an error message dialog.
     *
     * @param table The JTable to check for selection
     * @param parentComponent The parent component for the dialog
     * @param errorMsg The error message to display if no row is selected
     * @return true if a row is selected, false otherwise
     */
    static boolean isRowSelected(JTable table, Component parentComponent, String errorMsg) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentComponent,
                    errorMsg,
                    ResourceManager.getString("error"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Displays an error dialog with the specified title and message.
     *
     * @param parentComponent The parent component for the dialog, or null if there is no parent component
     * @param title The title of the error dialog
     * @param message The error message to display
     */
    public static void showErrorDialog(Component parentComponent,String message, String title) {
        JOptionPane.showMessageDialog(parentComponent,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

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
            System.err.println("Error determining current card: " + e.getMessage());
            return "unknown";
        }
    }

    /**
     * Validates that a book JSON object contains all required fields.
     *
     * @param book The JsonObject representing a book
     * @return true if all required fields are present, false otherwise
     */
    public static boolean hasRequiredBookFields(JsonObject book) {
        return book != null &&
                book.has("BookID") &&
                book.has("Title") &&
                book.has("Author") &&
                book.has("Publisher") &&
                book.has("Available");
    }

    /**
     * Calculates the overdue fee based on the number of days overdue and user type.
     *
     * @param daysOverdue The number of days the book is overdue
     * @param userType The type of user (e.g., "Students", "General Public", "Admins")
     * @return The calculated fee, or 0.0 if an error occurs
     */
    public static double calculateFee(int daysOverdue, String userType) {
        try {
            if (userType.equals("Students")) {
                return daysOverdue * 0.5f;
            } else if (userType.equals("General Public")) {
                return daysOverdue * 1.0f;
            } else if (userType.equals("Admins")) {
                return 0.0f;
            } else {
                throw new IllegalArgumentException("Unknown user type: " + userType);
            }
        } catch (Exception e) {
            System.err.println("Error calculating fee: " + e.getMessage());
            return 0.0f; // Default to no fee in case of error
        }
    }
}
