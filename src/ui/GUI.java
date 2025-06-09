package ui;

import core.LoggingManager;
import core.ResourceManager;
import ui.panels.*;
import ui.utils.LanguageManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Main graphical user interface for the library management system.
 * Provides a comprehensive UI with login capabilities, book browsing,
 * book management, and internationalization support.
 * Uses a card layout to switch between login and main application screens.
 */
public class GUI extends JFrame {
    private static final Logger logger = Logger.getLogger(GUI.class.getName());

    /** The main container panel that holds all UI cards */
    public JPanel cardPanel;

    /** Layout manager for switching between different UI screens */
    public CardLayout cardLayout;

    /** User preferences storage for settings like language selection */
    public final Preferences prefs = Preferences.userNodeForPackage(GUI.class);


    /**
     * Constructs the GUI application window and initializes all components.
     * Sets up the card layout for switching between login and main application panels.
     * Loads application icons and sets the initial screen to the login panel.
     */
    public GUI() {
        logger.log(Level.INFO, "Initializing GUI application window");
        initializeWindow();
        initializeLayout();
        initializePanels();
        showLoginScreen();
        logger.log(Level.INFO, "GUI initialization completed");
    }

    /**
     * Initializes the main application window with title, size, and icon.
     * Sets the default close operation to exit the application.
     */
    private void initializeWindow() {
        setTitle(core.ResourceManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Load saved window size and position from preferences
        int width = prefs.getInt("window.width", 1000);
        int height = prefs.getInt("window.height", 700);
        int x = prefs.getInt("window.x", -1);
        int y = prefs.getInt("window.y", -1);
        
        setSize(width, height);
        setResizable(true);
        
        if (x >= 0 && y >= 0) {
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
        }
        
        setApplicationIcon();
        
        // Save window state when closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveWindowState();
            }
        });
    }

    /**
     * Saves the current window state to preferences.
     */
    private void saveWindowState() {
        prefs.putInt("window.width", getWidth());
        prefs.putInt("window.height", getHeight());
        prefs.putInt("window.x", getX());
        prefs.putInt("window.y", getY());
    }

    /**
     * Initializes the card layout and main panel container.
     * Sets the content pane to the card panel for dynamic screen switching.
     */
    private void initializeLayout() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        setContentPane(cardPanel);
    }

    /**
     * Initializes the panels for the login screen and main application.
     * Adds the login panel and main application panel to the card layout.
     */
    private void initializePanels() {
        // Create instance of LoginPanel
        LoginPanel loginPanel = new LoginPanel(this);
        cardPanel.add(loginPanel, "login");
        
        // Create main panel (assuming MainApplicationPanel is still static for now)
        MainApplicationPanel.createMainPanel(this);
        cardPanel.add(MainApplicationPanel.mainPanel, "main");
    }

    /** Displays the login screen by switching to the login card in the card layout. */
    private void showLoginScreen() {
        cardLayout.show(cardPanel, "login");
    }

    /** Sets the application icon with multiple sizes for better OS compatibility. */
    public void setApplicationIcon() {
        logger.log(Level.INFO, "Attempting to load application icons...");

        try {
            java.util.List<Image> iconImages = new java.util.ArrayList<>();

            // Try to load multiple icon sizes
            String[] iconPaths = {
                    "/resources/icon/icon-16.png",
                    "/resources/icon/icon-32.png",
                    "/resources/icon/icon-64.png",
                    "/resources/icon/icon-128.png",
                    "/resources/icon/icon-256.png",
                    "/resources/icon/icon.png" // fallback
            };

            for (String path : iconPaths) {
                try {
                    if (getClass().getResource(path) != null) {
                        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(path)));
                        iconImages.add(icon.getImage());
                        logger.log(Level.INFO, "Successfully loaded icon: " + path);
                    } else {
                        logger.log(Level.WARNING, "Icon resource not found: " + path);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to load icon: " + path + " - " + e.getMessage());
                }
            }

            if (!iconImages.isEmpty()) {
                setIconImages(iconImages);
                logger.log(Level.INFO, "Application icons set successfully (" + iconImages.size() + " sizes loaded)");
            } else {
                logger.log(Level.WARNING, "No application icons could be loaded - using default system icon");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Critical error setting application icons: " + e.getMessage(), e);
        }
    }


    /**
     * Displays a popup options menu with language selection and logout options.
     * The menu appears below the component that triggered it (typically the options button).
     *
     * @param component The UI component that triggered the menu display
     */
    public void showOptionsMenu(JComponent component) {
        JPopupMenu optionsMenu = new JPopupMenu();

        JMenu languageMenu = new JMenu(ResourceManager.getString("menu.language"));

        JMenuItem englishItem = new JMenuItem(core.ResourceManager.getString("menu.language.english"));
        englishItem.addActionListener(_ -> LanguageManager.changeLanguage(this, "en"));

        JMenuItem portugueseItem = new JMenuItem(core.ResourceManager.getString("menu.language.portuguese"));
        portugueseItem.addActionListener(_ -> LanguageManager.changeLanguage(this, "pt"));

        languageMenu.add(englishItem);
        languageMenu.add(portugueseItem);

        JMenuItem logoutItem = new JMenuItem(ResourceManager.getString("menu.logout"));
        logoutItem.addActionListener(_ -> logout());

        JMenuItem quitItem = new JMenuItem(ResourceManager.getString("menu.quit"));
        quitItem.addActionListener(_ -> exitApplication());

        optionsMenu.add(languageMenu);
        optionsMenu.addSeparator();
        optionsMenu.add(logoutItem);
        optionsMenu.addSeparator();
        optionsMenu.add(quitItem);

        optionsMenu.show(component, 0, component.getHeight());
    }

    /**
     * Logs out the current user and returns to the login screen.
     * Clears all user-specific data from memory for security.
     * Resets the login form fields.
     */
    public void logout() {
        logger.log(Level.INFO, "Initiating logout");
        core.SessionManager.getInstance().logout();
        MainApplicationPanel.welcomeLabel.setText(ResourceManager.getString("welcome.message"));
        LoginPanel loginPanel = (LoginPanel) cardPanel.getComponent(0); // Assuming login panel is first
        loginPanel.clearFields();
        cardLayout.show(cardPanel, "login");
        logger.log(Level.INFO, "Logout completed - returned to login screen");
    }


    /**
     * Refreshes all tables that display book data after database changes.
     */
    public void refreshAllBookTables() {
        try {
            logger.log(Level.INFO, "Refreshing all book tables after database update");

            // Refresh browse books table
            if (BrowseBooksPanel.browseBooksTableModel != null && BrowseBooksPanel.browseBooksSearchField != null) {
                BrowseBooksPanel.loadBrowseBooksToTable(this, BrowseBooksPanel.browseBooksTableModel, BrowseBooksPanel.browseBooksSearchField.getText());
            }

            // Refresh manage books table (admin only)
            if (ManagementPanel.manageBooksTableModel != null && ManagementPanel.manageBooksSearchField != null) {
                ManagementPanel.loadAllBooksToTable(this, ManagementPanel.manageBooksTableModel, ManagementPanel.manageBooksSearchField.getText());
            }

            // Refresh my books table (if user is logged in)
            if (MyBooksPanel.myBooksTableModel != null && core.SessionManager.getInstance().getCurrentUser() != null) {
                MyBooksPanel.loadMyBooksToTable(this, MyBooksPanel.myBooksTableModel);
            }

            logger.log(Level.INFO, "All book tables refreshed successfully");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to refresh some tables", e);
        }
    }

    /**
     * Safely exits the application after confirming with the user
     * and performing necessary cleanup.
     */
    private void exitApplication() {
        int response = JOptionPane.showConfirmDialog(
                this,
                ResourceManager.getString("confirm.quit"),
                ResourceManager.getString("confirm.quit.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            logger.log(Level.INFO, "User initiated application shutdown");

            try {
                // Perform cleanup
                if (core.SessionManager.getInstance().isLoggedIn()) {
                    logout();
                }

                // Ensure all logs are written
                LoggingManager.flushLogs();

                logger.log(Level.INFO, "Application shutdown complete");

                // Exit the application
                System.exit(0);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during application shutdown", e);
                System.exit(1);
            }
        }
    }
}