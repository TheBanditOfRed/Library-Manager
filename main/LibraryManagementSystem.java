package main;

import main.ui.GUI;
import main.core.ResourceManager;

import javax.swing.*;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application launcher that handles initialization,
 * error handling, and startup of the library management system.
 */
public class LibraryManagementSystem {
    private static final Logger LOGGER = Logger.getLogger(LibraryManagementSystem.class.getName());

    /**
     * Main method to launch the library management system.
     * Handles exceptions globally and initializes the GUI.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            intializeApplication();

            SwingUtilities.invokeLater(() -> {
                try{
                    GUI gui = new GUI();
                    gui.setVisible(true);
                } catch (Exception e){
                    handleGUIException(e);
                }
            });
        } catch (Exception e) {
            handleStartupException(e);
        }
    }

    /**
     * Initializes application settings and settings.
     */
    private static void intializeApplication() {
        Preferences prefs = Preferences.userNodeForPackage(GUI.class);
        String savedLanguage = prefs.get("language", "en");
        ResourceManager.setLocale(savedLanguage);

        LOGGER.info("Application initialized with language: " + savedLanguage);
    }

    /**
     * Handles exceptions that occur during GUI initialization.
     * Displays an error message and logs the exception.
     *
     * @param e The exception that occurred
     */
    private static void handleGUIException(Exception e) {
        LOGGER.log(Level.SEVERE, "Error initializing GUI", e);
        JOptionPane.showMessageDialog(null,
                ResourceManager.getString("gui.error.initialization") + "\n" + e.getMessage() + "\n\n" + ResourceManager.getString("gui.error.exit"),
                ResourceManager.getString("error"),
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    /**
     * Handles exceptions that occur during application startup.
     * Displays an error message and logs the exception.
     *
     * @param e The exception that occurred
     */
    private static void handleStartupException(Exception e) {
        LOGGER.log(Level.SEVERE, "Error during application startup", e);
        JOptionPane.showMessageDialog(null,
                ResourceManager.getString("startup.error") + "\n" + e.getMessage() + "\n\n" + ResourceManager.getString("gui.error.exit"),
                ResourceManager.getString("error"),
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}

