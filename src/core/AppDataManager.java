package core;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages application data directories and file locations.
 * Handles proper separation of application files and user data.
 */
public class AppDataManager {
    private static final Logger logger = Logger.getLogger(AppDataManager.class.getName());
    private static final String APP_NAME = "Library Manager";
    
    /**
     * Gets the user data directory where JSON files and user-modifiable data are stored.
     * Windows: %LOCALAPPDATA%/Library Manager/data
     * macOS: ~/Library/Application Support/Library Manager/data
     * Linux: ~/.config/Library Manager/data
     */
    public static String getUserDataDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String dataDir;
        
        if (os.contains("win")) {
            dataDir = System.getenv("LOCALAPPDATA") + File.separator + APP_NAME + File.separator + "data";
        } else if (os.contains("mac")) {
            dataDir = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME + "/data";
        } else {
            dataDir = System.getProperty("user.home") + "/.config/" + APP_NAME + "/data";
        }
        
        // Ensure directory exists
        try {
            Files.createDirectories(Paths.get(dataDir));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create data directory: " + dataDir, e);
        }
        
        return dataDir;
    }
    
    /**
     * Gets the logs directory.
     */
    public static String getLogsDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String logsDir;
        
        if (os.contains("win")) {
            logsDir = System.getenv("LOCALAPPDATA") + File.separator + APP_NAME + File.separator + "logs";
        } else if (os.contains("mac")) {
            logsDir = System.getProperty("user.home") + "/Library/Application Support/" + APP_NAME + "/logs";
        } else {
            logsDir = System.getProperty("user.home") + "/.config/" + APP_NAME + "/logs";
        }
        
        // Ensure directory exists
        try {
            Files.createDirectories(Paths.get(logsDir));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create logs directory: " + logsDir, e);
        }
        
        return logsDir;
    }
    
    /**
     * Initializes user data files by copying from application resources if they don't exist.
     */
    public static void initializeUserDataFiles() {
        String userDataDir = getUserDataDirectory();
        
        // Initialize data files
        initializeDataFile("/resources/data/BookData.json", userDataDir + File.separator + "BookData.json");
        initializeDataFile("/resources/data/UserData.json", userDataDir + File.separator + "UserData.json");
        
        logger.info("User data files initialized in: " + userDataDir);
    }
    
    /**
     * Copies a resource file to user data directory if it doesn't already exist.
     */
    private static void initializeDataFile(String resourcePath, String targetPath) {
        Path targetFilePath = Paths.get(targetPath);
        
        // Only copy if file doesn't exist (preserve user modifications)
        if (!Files.exists(targetFilePath)) {
            try (InputStream inputStream = AppDataManager.class.getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    Files.copy(inputStream, targetFilePath);
                    logger.info("Initialized data file: " + targetPath);
                } else {
                    logger.warning("Resource not found: " + resourcePath);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to initialize data file: " + targetPath, e);
            }
        }
    }
    
    /**
     * Gets the full path for a data file in the user data directory.
     */
    public static String getDataFilePath(String fileName) {
        return getUserDataDirectory() + File.separator + fileName;
    }
}