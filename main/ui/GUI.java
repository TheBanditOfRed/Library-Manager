package main.ui;

import main.core.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.prefs.Preferences;

import main.core.SecurityManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

//TODO: ADD PROPER ERROR HANDLING FOR ALL CALLS

/**
 * Main graphical user interface for the library management system.
 * Provides a comprehensive UI with login capabilities, book browsing,
 * book management, and internationalization support.
 * Uses a card layout to switch between login and main application screens.
 */
public class GUI extends JFrame {
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private JPanel loginPanel;
    private JPanel mainPanel;
    private JPasswordField passwordField;
    private JTextField idField;
    private String currentUser;
    private JLabel welcomeLabel;
    private String currentUserName;
    private String key;
    private JPanel myBooksPanel;
    private DefaultTableModel myBooksModel;
    private final Preferences prefs = Preferences.userNodeForPackage(GUI.class);

    /**
     * Initializes the main application window and sets up the UI structure.
     * Creates the login and main panels, configures the card layout,
     * and displays the login screen initially.
     */
    public GUI() {
        setTitle(ResourceManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Create card layout and main container
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Create login panel
        createLoginPanel();

        // Create main application panel
        createMainPanel();

        // Add panels to card layout
        cardPanel.add(loginPanel, "login");
        cardPanel.add(mainPanel, "main");

        // Show login panel first
        cardLayout.show(cardPanel, "login");

        // Set the content pane
        setContentPane(cardPanel);
    }

    /**
     * Creates the login panel with user ID and password fields.
     * Uses GridBagLayout for precise component positioning.
     * Sets up the login button action to authenticate users and handle errors.
     */
    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel idLabel = new JLabel(ResourceManager.getString("login.userid"));
        JLabel passwordLabel = new JLabel(ResourceManager.getString("login.password"));
        idField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton(ResourceManager.getString("login.button"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(idLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(loginButton, gbc);

        loginButton.addActionListener(_ -> {
            String id = idField.getText();

            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
            Arrays.fill(passwordChars, '0');

            DataBaseManager dbm = new DataBaseManager();
            JsonObject user = dbm.findUser(id, password);

            if (id.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(loginPanel,
                        ResourceManager.getString("login.error.empty"),
                        ResourceManager.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (user != null) {
                try {
                    String encryptedPassword = user.get("Password").getAsString();
                    String decryptedPassword = SecurityManager.decrypt(encryptedPassword, password);

                    if (decryptedPassword.equals(password)) {
                        key = password;
                        
                        String encryptedID = user.get("UserID").getAsString();
                        currentUser = SecurityManager.decrypt(encryptedID, password);

                        String encryptedName = user.get("Name").getAsString();
                        currentUserName = SecurityManager.decrypt(encryptedName, password);

                        switchToMainPanel();
                    } else {
                        JOptionPane.showMessageDialog(loginPanel,
                                ResourceManager.getString("login.error.invalid.password"),
                                ResourceManager.getString("error"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(loginPanel,
                            ResourceManager.getString("login.error.invalid.password"),
                            ResourceManager.getString("error"),
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(loginPanel,
                        ResourceManager.getString("login.error.invalid"),
                        ResourceManager.getString("error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Creates the main application panel with a tabbed interface.
     * Sets up the top panel with welcome message and options button.
     * Initializes the tabbed pane with book browsing and user book panels.
     * Uses BorderLayout for efficient space utilization.
     */
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());

        // Top panel with welcome message and options button
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel(ResourceManager.getString("welcome.message"));
        JButton optionsButton = new JButton(ResourceManager.getString("button.options"));

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(optionsButton, BorderLayout.EAST);

        // Main content with tabbed pane for different functions
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(ResourceManager.getString("tab.browse"), createBrowseBooksPanel());
        tabbedPane.addTab(ResourceManager.getString("tab.mybooks"), createMyBooksPanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add options menu functionality
        optionsButton.addActionListener(e -> showOptionsMenu(optionsButton));
    }

    /**
     * Displays a popup options menu with language selection and logout options.
     * The menu appears below the component that triggered it (typically the options button).
     *
     * @param component The UI component that triggered the menu display
     */
    private void showOptionsMenu(JComponent component) {
        JPopupMenu optionsMenu = new JPopupMenu();
        
        // Language submenu
        JMenu languageMenu = new JMenu(ResourceManager.getString("menu.language"));
        
        JMenuItem englishItem = new JMenuItem(ResourceManager.getString("menu.language.english"));
        englishItem.addActionListener(e -> changeLanguage("en"));
        
        JMenuItem portugueseItem = new JMenuItem(ResourceManager.getString("menu.language.portuguese"));
        portugueseItem.addActionListener(e -> changeLanguage("pt"));
        
        languageMenu.add(englishItem);
        languageMenu.add(portugueseItem);
        
        // Logout option
        JMenuItem logoutItem = new JMenuItem(ResourceManager.getString("menu.logout"));
        logoutItem.addActionListener(e -> logout());
        
        // Add items to menu
        optionsMenu.add(languageMenu);
        optionsMenu.addSeparator();
        optionsMenu.add(logoutItem);
        
        // Show the popup menu
        optionsMenu.show(component, 0, component.getHeight());
    }

    /**
     * Changes the application's language and updates the UI text.
     * Saves the language preference for future sessions.
     * Displays a confirmation message to the user.
     *
     * @param languageCode The language code to switch to (e.g., "en" for English)
     */
    private void changeLanguage(String languageCode) {
        // Save language preference
        prefs.put("language", languageCode);
        
        // Change language
        ResourceManager.setLocale(languageCode);
        
        // Show confirmation message
        JOptionPane.showMessageDialog(this,
                ResourceManager.getString("options.language.changed"),
                ResourceManager.getString("options.title"),
                JOptionPane.INFORMATION_MESSAGE);
        
        // Update UI text
        updateUIText();
    }

    /**
     * Updates all UI text elements when the language changes.
     * Recreates panels as needed to apply the new language strings.
     * Maintains the current state (login or main) and user context.
     */
    private void updateUIText() {
        // Update frame title
        setTitle(ResourceManager.getString("app.title"));
        
        // If on login screen
        if (cardLayout.toString().contains("login")) {
            // Recreate login panel with updated text
            Container contentPane = getContentPane();
            contentPane.remove(loginPanel);
            createLoginPanel();
            ((CardLayout)cardPanel.getLayout()).addLayoutComponent(loginPanel, "login");
            cardPanel.add(loginPanel, "login");
            cardLayout.show(cardPanel, "login");
        } 
        // If on main screen
        else {
            // Recreate main panel with updated text
            Container contentPane = getContentPane();
            contentPane.remove(mainPanel);
            createMainPanel();
            ((CardLayout)cardPanel.getLayout()).addLayoutComponent(mainPanel, "main");
            cardPanel.add(mainPanel, "main");
            
            // Update welcome message with current user
            if (currentUserName != null) {
                welcomeLabel.setText(ResourceManager.getString("welcome.user", currentUserName));
            } else {
                welcomeLabel.setText(ResourceManager.getString("welcome.message"));
            }
            
            cardLayout.show(cardPanel, "main");
            
            // Reload book data if needed
            if (myBooksPanel != null && myBooksModel != null) {
                loadMyBooksToTable(myBooksModel);
            }
        }
        
        // Force repaint
        revalidate();
        repaint();
    }

    /**
     * Logs out the current user and returns to the login screen.
     * Clears all user-specific data from memory for security.
     * Resets the login form fields.
     */
    private void logout() {
        currentUser = null;
        currentUserName = null;
        key = null;
        welcomeLabel.setText(ResourceManager.getString("welcome.message"));
        idField.setText("");
        passwordField.setText("");
        cardLayout.show(cardPanel, "login");
    }

    /**
     * Creates the book browsing panel with search capabilities.
     * Contains a search field, button, and a table displaying available books.
     * Uses BorderLayout with the search panel at the top and table in the center.
     *
     * @return JPanel containing the book browsing interface
     */
    private JPanel createBrowseBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton(ResourceManager.getString("button.search"));
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel(ResourceManager.getString("search.label") + ":"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        String[] columns = {
                ResourceManager.getString("column.shelf"),
                ResourceManager.getString("column.title"),
                ResourceManager.getString("column.author"),
                ResourceManager.getString("column.publisher"),
                ResourceManager.getString("column.available")
        };

        TableUtils.TableComponents tableComponents = TableUtils.createCenteredTable(columns);
        DefaultTableModel browseBooksTableModel = tableComponents.model();

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);

        loadBrowseBooksToTable(browseBooksTableModel, "");

        searchButton.addActionListener(_ -> {
            String searchTerm = searchField.getText();
            loadBrowseBooksToTable(browseBooksTableModel, searchTerm);
        });

        return panel;
    }

    /**
     * Loads book data into the browse books table based on a search term.
     * Clears the table and populates it with books matching the search criteria.
     * If searchTerm is empty, all books are displayed.
     *
     * @param model The table model to populate
     * @param searchTerm The search term to filter books by
     */
    private void loadBrowseBooksToTable(DefaultTableModel model, String searchTerm) {
        model.setRowCount(0);

        DataBaseManager dbm = new DataBaseManager();
        JsonArray books = dbm.findBooks(searchTerm);

        for (int i = 0; i < books.size(); i++) {
            JsonObject book = books.get(i).getAsJsonObject();
            int shelfNumber = dbm.getShelfNumber(book.get("BookID").getAsString());

            model.addRow(new Object[]{
                    shelfNumber,
                    book.get("Title").getAsString(),
                    book.get("Author").getAsString(),
                    book.get("Publisher").getAsString(),
                    book.get("Available").getAsInt() > 0 ? 
                        ResourceManager.getString("yes") : 
                        ResourceManager.getString("no")
            });
        }
    }

    /**
     * Creates the panel displaying books borrowed by the current user.
     * Contains a table showing book titles, issue dates, due dates, and status.
     * Includes a return button at the bottom (functionality not implemented).
     *
     * @return JPanel containing the user's borrowed books interface
     */
    private JPanel createMyBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {
                ResourceManager.getString("column.title"),
                ResourceManager.getString("column.dateissued"),
                ResourceManager.getString("column.datedue"),
                ResourceManager.getString("column.status")
        };

        TableUtils.TableComponents tableComponents = TableUtils.createCenteredTable(columns);
        DefaultTableModel myBooksTableModel = tableComponents.model();

        JButton returnButton = new JButton(ResourceManager.getString("button.return"));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(returnButton);

        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        myBooksPanel = panel;
        myBooksModel = myBooksTableModel;

        return panel;
    }

    /**
     * Loads the current user's borrowed books into the "My Books" table.
     * For each book, shows title, issue date, due date, and status.
     * Checks and updates the due status of each book.
     * Displays a message if the user has no borrowed books.
     *
     * @param model The table model to populate with borrowed books
     */
    private void loadMyBooksToTable(DefaultTableModel model) {
        model.setRowCount(0);

        DataBaseManager dbm = new DataBaseManager();
        JsonArray books = dbm.findBorrowedBooks(currentUser, key);

        if (books != null && !books.isEmpty()) {
            for (int i = 0; i < books.size(); i++) {
                try {
                    JsonObject book = books.get(i).getAsJsonObject();
                    String bookId = book.get("BookID").getAsString();
                    String title = dbm.getBookTitle(bookId);

                    String dateIssued = SecurityManager.decrypt(book.get("DateIssued").getAsString(), key);

                    String userType = dbm.getUserType(currentUser, key);
                    String dateDue = dbm.getDueDate(dateIssued, userType);

                    int statusActual = dbm.getDueStatus(dateIssued, dateDue);
                    int statusSaved = book.get("Status").getAsInt();

                    if (statusActual != statusSaved) {
                        dbm.updateDueStatus(currentUser, bookId, statusActual, key);
                    }

                    String statusMsg = "";
                    if (statusActual == 0) {
                        statusMsg = ResourceManager.getString("status.duetoday");
                    } else if (statusActual == 1) {
                        statusMsg = ResourceManager.getString("status.ontime");
                    } else if (statusActual == -1) {
                        statusMsg = ResourceManager.getString("status.overdue");
                    }

                    model.addRow(new Object[]{
                            title,
                            dateIssued,
                            dateDue,
                            statusMsg
                    });
                } catch (Exception e) {
                    System.err.println("Error loading book: " + e.getMessage());
                }
            }
        } else {
            // Add a row to show no books are borrowed
            model.addRow(new Object[]{ResourceManager.getString("books.none"), "", "", ""});
        }
    }

    /**
     * Creates the admin panel for managing books (adding/removing).
     * Only accessible to users with admin privileges.
     * Contains fields and buttons for book management operations.
     *
     * @return JPanel containing the book management interface
     */
    private JPanel createManageBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField bookIdField = new JTextField(15);
        JButton addButton = new JButton(ResourceManager.getString("button.add"));
        JButton removeButton = new JButton(ResourceManager.getString("button.remove"));

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel(ResourceManager.getString("book.id") + ":"));
        inputPanel.add(bookIdField);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Switches from the login screen to the main application screen.
     * Updates the welcome message with the user's name.
     * Loads the user's borrowed books into the "My Books" table.
     * Adds the admin panel for users with admin privileges.
     */
    private void switchToMainPanel() {
        welcomeLabel.setText(ResourceManager.getString("welcome.user", currentUserName));

        if (myBooksPanel != null) {
            loadMyBooksToTable(myBooksModel);
        }

        Component component = mainPanel.getComponent(1);
        if (component instanceof JTabbedPane tabbedPane) {

            // Remove admin tab if it exists (when switching users)
            if (tabbedPane.getTabCount() > 2) {
                tabbedPane.removeTabAt(2);
            }

            // Add admin tab if current user is admin
            if (currentUser.equals("admin")) {
                tabbedPane.addTab(ResourceManager.getString("tab.manage"), createManageBooksPanel());
            }
        }

        cardLayout.show(cardPanel, "main");
    }

    /**
     * Main method to launch the application.
     * Uses SwingUtilities.invokeLater to ensure thread safety.
     * Loads saved language preference and initializes the UI.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Load saved language preference
            Preferences prefs = Preferences.userNodeForPackage(GUI.class);
            String savedLanguage = prefs.get("language", "en");
            ResourceManager.setLocale(savedLanguage);
            
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }
}