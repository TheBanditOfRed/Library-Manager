package main.ui.panels;

import main.core.ResourceManager;
import main.core.SessionManager;
import main.ui.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MainApplicationPanel is responsible for creating and updating the main application panel.
 * It sets up the welcome message, options button, and tabbed interface for browsing books and viewing user books.
 * The panel uses BorderLayout for efficient space utilization.
 */
public class MainApplicationPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(MainApplicationPanel.class.getName());

    /** Panel containing the main application interface */
    public static JPanel mainPanel;

    /** Label displaying welcome message with user's name */
    public static JLabel welcomeLabel;

    /**
     * Updates the main panel with new language text.
     * @param gui The GUI instance to update
     */
    public static void updateMainPanel(GUI gui) {
        try {
            Container contentPane = gui.getContentPane();
            contentPane.remove(mainPanel);
            createMainPanel(gui);
            gui.cardPanel.add(mainPanel, "main");

            if (SessionManager.getInstance().getCurrentUserName() != null) {
                welcomeLabel.setText(ResourceManager.getString("welcome.user", SessionManager.getInstance().getCurrentUserName()));
            } else {
                welcomeLabel.setText(ResourceManager.getString("welcome.message"));
            }

            gui.cardLayout.show(gui.cardPanel, "main");

            if (MyBooksPanel.myBooksPanel != null && MyBooksPanel.myBooksTableModel != null) {
                try {
                    MyBooksPanel.loadMyBooksToTable(gui, MyBooksPanel.myBooksTableModel);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to update main panel: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update main panel: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the main application panel with a tabbed interface.
     * Sets up the top panel with welcome message and options button.
     * Initializes the tabbed pane with book browsing and user book panels.
     * Uses BorderLayout for efficient space utilization.
     * @param gui The GUI instance to create the main panel for
     */
    public static void createMainPanel(GUI gui) {
        mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel(ResourceManager.getString("welcome.message"));
        JButton optionsButton = new JButton(ResourceManager.getString("button.options"));

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(optionsButton, BorderLayout.EAST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(ResourceManager.getString("tab.browse"), BrowseBooksPanel.createBrowseBooksPanel(gui));
        tabbedPane.addTab(ResourceManager.getString("tab.mybooks"), MyBooksPanel.createMyBooksPanel(gui));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        optionsButton.addActionListener(_ -> gui.showOptionsMenu(optionsButton));
    }
}
