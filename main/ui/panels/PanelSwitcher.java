package main.ui.panels;

import main.core.ResourceManager;
import main.core.SessionManager;
import main.ui.GUI;
import main.ui.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PanelSwitcher is responsible for switching between different panels in the application.
 * It handles navigation from the login screen to the main application screen and manages
 * the visibility of various components based on user authentication status.
 */
public class PanelSwitcher extends JPanel {
    private static final Logger logger = Logger.getLogger(PanelSwitcher.class.getName());

    /**
     * Switches from the login screen to the main application screen.
     * Includes error handling for component access and database operations.
     * @param gui The GUI instance to which the main panel belongs
     */
    public static void switchToMainPanel(GUI gui) {
        logger.log(Level.INFO, "User " + SessionManager.getInstance().getCurrentUser() + " successfully authenticated - switching to main panel");

        try {
            try {
                MainApplicationPanel.welcomeLabel.setText(ResourceManager.getString("welcome.user", SessionManager.getInstance().getCurrentUser()));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to update welcome message", e);
            }

            try {
                // Load user's borrowed books if the panel is already initialized
                if (MyBooksPanel.myBooksPanel != null && MyBooksPanel.myBooksTableModel != null) {
                    MyBooksPanel.loadMyBooksToTable(gui, MyBooksPanel.myBooksTableModel);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load user's borrowed books", e);
                DialogUtils.showErrorDialog(gui,
                        ResourceManager.getString("error.load.books") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }

            try {
                // Dynamically adjust tabs based on user type (add admin tab if needed)
                Component component = MainApplicationPanel.mainPanel.getComponent(1);
                if (component instanceof JTabbedPane tabbedPane) {
                    // Remove admin tab if it exists (index 2)
                    if (tabbedPane.getTabCount() > 2) {
                        tabbedPane.removeTabAt(2);
                    }

                    // Add admin tab only for admin users
                    if ("admin".equals(SessionManager.getInstance().getCurrentUser())) {
                        tabbedPane.addTab(ResourceManager.getString("tab.manage"), ManagementPanel.createManageBooksPanel(gui));
                    }
                } else {
                    throw new ClassCastException("Expected JTabbedPane but found " +
                            (component != null ? component.getClass().getName() : "null"));
                }
            } catch (IndexOutOfBoundsException e) {
                logger.log(Level.WARNING, "Failed to access tabbed pane in main panel", e);
                DialogUtils.showErrorDialog(gui,
                        ResourceManager.getString("error.ui.component") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            } catch (ClassCastException e) {
                logger.log(Level.SEVERE, "Unexpected component type in main panel", e);
                DialogUtils.showErrorDialog(gui,
                        ResourceManager.getString("error.ui.component") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to update admin tab", e);
            }

            try {
                // CardLayout switches between different panels (login, main, etc.)
                gui.cardLayout.show(gui.cardPanel, "main");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to switch to main panel", e);
                DialogUtils.showErrorDialog(gui,
                        ResourceManager.getString("error.ui.navigation") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Critical error in switchToMainPanel", e);
            DialogUtils.showErrorDialog(gui,
                    ResourceManager.getString("error.login.failed") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );

            try {
                // Fallback to login screen on critical error
                gui.cardLayout.show(gui.cardPanel, "login");
            } catch (Exception ex) {
                DialogUtils.showErrorDialog(gui,
                        ResourceManager.getString("error.critical") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }
        }
    }

    /**
     * Switches the view to the Manage Books panel in the main interface.
     * @param gui The GUI instance to which the main panel belongs
     */
    public static void switchToManageBooksPanel(GUI gui) {
        gui.cardLayout.show(gui.cardPanel, "main");

        try {
            Component component = MainApplicationPanel.mainPanel.getComponent(1);
            if (component instanceof JTabbedPane tabbedPane) {
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getTitleAt(i).equals(ResourceManager.getString("tab.manage"))) {
                        tabbedPane.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to switch to Manage Books panel: " + e.getMessage(), e);
            DialogUtils.showErrorDialog(MainApplicationPanel.mainPanel,
                    ResourceManager.getString("error.switch.panel") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }

        gui.refreshAllBookTables();
    }
}
