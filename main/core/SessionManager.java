package main.core;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SessionManager is responsible for managing user sessions in the library management system.
 * It provides methods for logging in, logging out, checking if a user is logged in,
 * and retrieving the current user's information.
 * The class implements the Singleton design pattern to ensure only one instance exists.
 */
public class SessionManager {
    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    /**
     * Singleton instance of SessionManager.
     * Uses double-checked locking for thread safety and performance.
     */
    private static volatile SessionManager instance;

    /** Lock object for synchronizing access to the singleton instance */
    private static final Object lock = new Object();

    /** Current user session details */
    private String currentUser;

    /** Current user's name */
    private String currentUserName;

    /** Encryption key for the current session */
    private String key;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the session manager.
     */
    private SessionManager() {}

    /**
     * Returns the singleton instance of SessionManager.
     * Uses double-checked locking to ensure thread safety and performance.
     *
     * @return The singleton instance of SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new SessionManager();
                    logger.log(Level.INFO, "SessionManager initialized");
                }
            }
        }
        return instance;
    }

    /**
     * Logs in a user with the specified credentials.
     * Sets the current user, user name, and encryption key for the session.
     *
     * @param user The user ID of the logging-in user
     * @param name The name of the logging-in user
     * @param encryptionKey The encryption key for the session
     */
    public synchronized void login(String user, String name, String encryptionKey) {
        this.currentUser = user;
        this.currentUserName = name;
        this.key = encryptionKey;
        logger.log(Level.INFO, "User logged in: " + user);
    }

    /**
     * Logs out the current user, clearing the session details.
     * Sets the current user, user name, and encryption key to null.
     */
    public synchronized void logout() {
        logger.log(Level.INFO, "User logging out: " + currentUser);
        this.currentUser = null;
        this.currentUserName = null;
        this.key = null;
    }

    /**
     * Checks if a user is currently logged in.
     * A user is considered logged in if both currentUser and key are not null.
     *
     * @return true if a user is logged in, false otherwise
     */
    public synchronized boolean isLoggedIn() {
        return currentUser != null && key != null;
    }

    /**
     * Retrieves the current user's ID.
     *
     * @return The ID of the currently logged-in user, or null if no user is logged in
     */
    public synchronized String getCurrentUser() {
        return currentUser;
    }

    /**
     * Retrieves the current user's name.
     *
     * @return The name of the currently logged-in user, or null if no user is logged in
     */
    public synchronized String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * Retrieves the encryption key for the current session.
     *
     * @return The encryption key, or null if no user is logged in
     */
    public synchronized String getKey() {
        return key;
    }
}