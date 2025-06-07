package main.ui;

import main.core.ResourceManager;
import main.ui.panels.*;
import main.ui.utils.LanguageManager;

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
    public final JPanel cardPanel;

    /** Layout manager for switching between different UI screens */
    public final CardLayout cardLayout;

    /** User preferences storage for settings like language selection */
    public final Preferences prefs = Preferences.userNodeForPackage(GUI.class);


    /**
     * Initializes the main application window and sets up the UI structure.
     * Creates the card layout system for switching between login and main screens,
     * initializes both login and main panels, configures window properties,
     * and displays the login screen initially. Sets up the application icon
     * and window close behavior.
     */
    public GUI() {
        logger.log(Level.INFO, "Initializing GUI application window");
        setTitle(ResourceManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setApplicationIcon();
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        LoginPanel.createLoginPanel(this);
        MainApplicationPanel.createMainPanel(this);

        cardPanel.add(LoginPanel.loginPanel, "login");
        cardPanel.add(MainApplicationPanel.mainPanel, "main");

        cardLayout.show(cardPanel, "login");

        setContentPane(cardPanel);
        logger.log(Level.INFO, "GUI initialization completed - displaying login screen");
    }

    /**
     * Sets the application icon with multiple sizes for better OS compatibility.
     */
    public void setApplicationIcon() {
        logger.log(Level.INFO, "Attempting to load application icons...");

        try {
            java.util.List<Image> iconImages = new java.util.ArrayList<>();

            // Try to load multiple icon sizes
            String[] iconPaths = {
                    "/main/resources/icon/icon-16.png",
                    "/main/resources/icon/icon-32.png",
                    "/main/resources/icon/icon-64.png",
                    "/main/resources/icon/icon-128.png",
                    "/main/resources/icon/icon-256.png",
                    "/main/resources/icon/icon.png" // fallback
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

        JMenuItem englishItem = new JMenuItem(ResourceManager.getString("menu.language.english"));
        englishItem.addActionListener(_ -> LanguageManager.changeLanguage(this, "en"));

        JMenuItem portugueseItem = new JMenuItem(ResourceManager.getString("menu.language.portuguese"));
        portugueseItem.addActionListener(_ -> LanguageManager.changeLanguage(this, "pt"));

        languageMenu.add(englishItem);
        languageMenu.add(portugueseItem);

        JMenuItem logoutItem = new JMenuItem(ResourceManager.getString("menu.logout"));
        logoutItem.addActionListener(_ -> logout());

        optionsMenu.add(languageMenu);
        optionsMenu.addSeparator();
        optionsMenu.add(logoutItem);

        optionsMenu.show(component, 0, component.getHeight());
    }

    /**
     * Logs out the current user and returns to the login screen.
     * Clears all user-specific data from memory for security.
     * Resets the login form fields.
     */
    public void logout() {
        logger.log(Level.INFO, "User " + LoginPanel.currentUser + " initiated logout");
        LoginPanel.currentUser = null;
        LoginPanel.currentUserName = null;
        LoginPanel.key = null;
        MainApplicationPanel.welcomeLabel.setText(ResourceManager.getString("welcome.message"));
        LoginPanel.idField.setText("");
        LoginPanel.passwordField.setText("");
        cardLayout.show(cardPanel, "login");
        logger.log(Level.INFO, "User logout completed - returned to login screen");
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
            if (MyBooksPanel.myBooksTableModel != null && LoginPanel.currentUser != null) {
                MyBooksPanel.loadMyBooksToTable(this, MyBooksPanel.myBooksTableModel);
            }

            logger.log(Level.INFO, "All book tables refreshed successfully");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to refresh some tables", e);
        }
    }


}