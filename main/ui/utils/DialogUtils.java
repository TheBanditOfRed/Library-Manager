package main.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for GUI-related helper methods.
 */
public class DialogUtils {
    private static final Logger logger = Logger.getLogger(DialogUtils.class.getName());

    /**
     * Displays an error dialog with the specified title and message.
     *
     * @param parentComponent The parent component for the dialog, or null if there is no parent component
     * @param message The error message to display
     * @param title The title of the error dialog
     */
    public static void showErrorDialog(Component parentComponent, String message, String title) {
        logger.log(Level.INFO, "Displaying error dialog to user: " + title + " - " + message);
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
    }

}