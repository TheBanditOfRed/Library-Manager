package main.core;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionManager {
    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());
    private static volatile SessionManager instance;
    private static final Object lock = new Object();

    private String currentUser;
    private String currentUserName;
    private String key;

    private SessionManager() {
        // Private constructor to prevent instantiation
    }

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

    public synchronized void login(String user, String name, String encryptionKey) {
        this.currentUser = user;
        this.currentUserName = name;
        this.key = encryptionKey;
        logger.log(Level.INFO, "User logged in: " + user);
    }

    public synchronized void logout() {
        logger.log(Level.INFO, "User logging out: " + currentUser);
        this.currentUser = null;
        this.currentUserName = null;
        this.key = null;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null && key != null;
    }

    // Synchronized getters
    public synchronized String getCurrentUser() {
        return currentUser;
    }

    public synchronized String getCurrentUserName() {
        return currentUserName;
    }

    public synchronized String getKey() {
        return key;
    }
}