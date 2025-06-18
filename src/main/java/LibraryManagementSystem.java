import core.LoggingManager;
import core.ResourceManager;
import ui.GUI;
import core.AppDataManager;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Main application launcher that handles initialization,
 * error handling, and startup of the library management system.
 */
public class LibraryManagementSystem {
    private static final Logger logger = Logger.getLogger(LibraryManagementSystem.class.getName());

    /**
     * Main method to launch the library management system.
     * Handles exceptions globally and initializes the GUI.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        AppDataManager.initializeUserDataFiles();
        
        LoggingManager.initializeLogging();

        logger.info("=== Library Management System Starting ===");
        logger.info("Java Version: " + System.getProperty("java.version"));
        logger.info("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        logger.info("User Directory: " + System.getProperty("user.dir"));
        logger.info("Data Directory: " + AppDataManager.getUserDataDirectory());
        logger.info("Log Directory: " + AppDataManager.getLogsDirectory());
    
        try {
            logger.info("Setting system look and feel...");
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.info("System look and feel set successfully");

            logger.info("Initializing application settings...");
            initializeApplication();
            logger.info("Application settings initialized successfully");
            
            logger.info("Starting GUI on Event Dispatch Thread...");
            SwingUtilities.invokeLater(() -> {
                try {
                    logger.info("Creating GUI instance...");
                    GUI gui = new GUI();
                    gui.setVisible(true);
                    logger.info("=== GUI successfully initialized and displayed ===");
                    logger.info("Application startup completed - Ready for user interaction");
                } catch (Exception e) {
                    handleGUIException(e);
                }
            });
            
        } catch (Exception e) {
            handleStartupException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("=== Library Management System Shutting Down ===");
            logger.info("Performing cleanup operations...");
            LoggingManager.flushLogs();
            logger.info("Shutdown completed");
        }));
    }

    /**
     * Initializes application settings and locale configuration.
     * Loads saved language preferences and sets up core.ResourceManager.
     */
    private static void initializeApplication() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GUI.class);
            String savedLanguage = prefs.get("language", "en");
            
            logger.info("Loading saved language preference: " + savedLanguage);

            ResourceManager.setLocale(savedLanguage);
            
            logger.info("Application initialized successfully with language: " + savedLanguage);
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load language preferences, using default", e);
            ResourceManager.setLocale("en");
        }
    }

    /**
     * Handles exceptions that occur during GUI initialization.
     * Displays an error message and logs the exception.
     *
     * @param e The exception that occurred
     */
    private static void handleGUIException(Exception e) {
        logger.log(Level.SEVERE, "Critical error during GUI initialization", e);

        String errorMessage;
        try {
            errorMessage = ResourceManager.getString("gui.error.initialization") + "\n" + 
                          e.getMessage() + "\n\n" + 
                          ResourceManager.getString("gui.error.exit");
        } catch (Exception resourceError) {
            errorMessage = "Failed to initialize GUI: " + e.getMessage() + 
                          "\n\nThe application will now exit.";
            logger.log(Level.SEVERE, "core.ResourceManager also failed during error handling", resourceError);
        }
        
        JOptionPane.showMessageDialog(null,
                errorMessage,
                "Critical Error",
                JOptionPane.ERROR_MESSAGE);
        
        logger.severe("Application terminating due to GUI initialization failure");
        LoggingManager.flushLogs(); // Ensure logs are written before exit
        System.exit(1);
    }

    /**
     * Handles exceptions that occur during application startup.
     * Displays an error message and logs the exception.
     *
     * @param e The exception that occurred
     */
    private static void handleStartupException(Exception e) {
        logger.log(Level.SEVERE, "Critical error during application startup", e);

        String errorMessage;
        try {
            errorMessage = ResourceManager.getString("startup.error") + "\n" +
                          e.getMessage() + "\n\n" + 
                          ResourceManager.getString("gui.error.exit");
        } catch (Exception resourceError) {
            errorMessage = "Failed to start application: " + e.getMessage() + 
                          "\n\nThe application will now exit.";
            logger.log(Level.SEVERE, "core.ResourceManager also failed during startup error handling", resourceError);
        }
        
        JOptionPane.showMessageDialog(null,
                errorMessage,
                "Startup Error",
                JOptionPane.ERROR_MESSAGE);
        
        logger.severe("Application terminating due to startup failure");
        LoggingManager.flushLogs(); // Ensure logs are written before exit
        System.exit(1);
    }
}