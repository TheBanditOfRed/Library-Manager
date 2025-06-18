package ui.panels;

import core.ResourceManager;
import ui.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MainApplicationPanel is responsible for displaying the main application interface.
 * It includes a welcome message, options button, and a tabbed pane for browsing books
 * and viewing the user's books.
 * The panel updates dynamically based on the current user's session.
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

            if (core.SessionManager.getInstance().getCurrentUserName() != null) {
                welcomeLabel.setText(ResourceManager.getString("welcome.user", core.SessionManager.getInstance().getCurrentUserName()));
            } else {
                welcomeLabel.setText(core.ResourceManager.getString("welcome.message"));
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
        JButton optionsButton = new JButton(core.ResourceManager.getString("button.options"));

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(optionsButton, BorderLayout.EAST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(core.ResourceManager.getString("tab.browse"), BrowseBooksPanel.createBrowseBooksPanel(gui));
        tabbedPane.addTab(core.ResourceManager.getString("tab.mybooks"), MyBooksPanel.createMyBooksPanel(gui));

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        optionsButton.addActionListener(_ -> gui.showOptionsMenu(optionsButton));
    }
}
