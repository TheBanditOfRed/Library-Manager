package main.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.core.DataBaseManager;
import main.core.ResourceManager;
import main.core.SecurityManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main graphical user interface for the library management system.
 * Provides a comprehensive UI with login capabilities, book browsing,
 * book management, and internationalization support.
 * Uses a card layout to switch between login and main application screens.
 */
public class GUI extends JFrame {
    private static final Logger logger = Logger.getLogger(GUI.class.getName());
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
        logger.info("Initializing GUI application window");
        setTitle(ResourceManager.getString("app.title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        createLoginPanel();
        createMainPanel();

        cardPanel.add(loginPanel, "login");
        cardPanel.add(mainPanel, "main");

        cardLayout.show(cardPanel, "login");

        setContentPane(cardPanel);
        logger.info("GUI initialization completed - displaying login screen");
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

        idField.addActionListener(_ -> passwordField.requestFocusInWindow());

        passwordField.addActionListener(_ -> loginButton.doClick());

        loginButton.addActionListener(_ -> {
            String id = idField.getText();

            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
            // Security practice: overwrite password in memory after use
            Arrays.fill(passwordChars, '0');

            DataBaseManager dbm = new DataBaseManager();
            JsonObject user = dbm.findUser(id, password);

            if (id.isEmpty() || password.isEmpty()) {
                logger.info("Login attempt with empty credentials");

                GuiHelper.showErrorDialog(loginPanel,
                        ResourceManager.getString("login.error.empty"),
                        ResourceManager.getString("error")
                );
                return;
            }

            if (user != null) {
                try {
                    String encryptedPassword = user.get("Password").getAsString();
                    // Verify password by decrypting stored password and comparing
                    String decryptedPassword = SecurityManager.decrypt(encryptedPassword, password);

                    if (decryptedPassword.equals(password)) {
                        logger.info("Successful login for user: " + id);
                        // Password acts as encryption key for other user data
                        key = password;

                        String encryptedID = user.get("UserID").getAsString();
                        currentUser = SecurityManager.decrypt(encryptedID, password);

                        String encryptedName = user.get("Name").getAsString();
                        currentUserName = SecurityManager.decrypt(encryptedName, password);

                        switchToMainPanel();
                    } else {
                        logger.warning("Failed login attempt - incorrect password for user: " + id);
                        GuiHelper.showErrorDialog(loginPanel,
                                ResourceManager.getString("login.error.invalid.password"),
                                ResourceManager.getString("error")
                        );
                    }
                } catch (RuntimeException ex) {
                    logger.warning("Failed login attempt - decryption failed for user: " + id);
                    // Decryption failure treated as authentication failure
                    GuiHelper.showErrorDialog(loginPanel,
                            ResourceManager.getString("login.error.invalid.password"),
                            ResourceManager.getString("error")
                    );
                }
            } else {
                logger.warning("Failed login attempt - user not found: " + id);
                GuiHelper.showErrorDialog(loginPanel,
                        ResourceManager.getString("login.error.invalid"),
                        ResourceManager.getString("error")
                );
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

        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel(ResourceManager.getString("welcome.message"));
        JButton optionsButton = new JButton(ResourceManager.getString("button.options"));

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(optionsButton, BorderLayout.EAST);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(ResourceManager.getString("tab.browse"), createBrowseBooksPanel());
        tabbedPane.addTab(ResourceManager.getString("tab.mybooks"), createMyBooksPanel());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        optionsButton.addActionListener(_ -> showOptionsMenu(optionsButton));
    }

    /**
     * Displays a popup options menu with language selection and logout options.
     * The menu appears below the component that triggered it (typically the options button).
     *
     * @param component The UI component that triggered the menu display
     */
    private void showOptionsMenu(JComponent component) {
        JPopupMenu optionsMenu = new JPopupMenu();

        JMenu languageMenu = new JMenu(ResourceManager.getString("menu.language"));

        JMenuItem englishItem = new JMenuItem(ResourceManager.getString("menu.language.english"));
        englishItem.addActionListener(_ -> changeLanguage("en"));

        JMenuItem portugueseItem = new JMenuItem(ResourceManager.getString("menu.language.portuguese"));
        portugueseItem.addActionListener(_ -> changeLanguage("pt"));

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
     * Changes the application's language and updates the UI text.
     * Includes error handling for preferences and resource operations.
     *
     * @param languageCode The language code to switch to (e.g., "en" for English)
     */
    private void changeLanguage(String languageCode) {
        String currentLanguage = prefs.get("language", "en");
        logger.info("User " + (currentUser != null ? currentUser : "unknown") + " changing language from " + currentLanguage + " to " + languageCode);
        
        try {
            if (languageCode == null || (!languageCode.equals("en") && !languageCode.equals("pt"))) {
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.language.invalid"),
                        ResourceManager.getString("error")
                );
                return;
            }

            try {
                prefs.put("language", languageCode);
                logger.info("Language preference saved successfully: " + languageCode);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to save language preference", e);
            }

            try {
                ResourceManager.setLocale(languageCode);
            } catch (Exception e) {
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.language.resource") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
                return;
            }

            JOptionPane.showMessageDialog(this,
                    ResourceManager.getString("options.language.changed"),
                    ResourceManager.getString("options.title"),
                    JOptionPane.INFORMATION_MESSAGE);

            try {
                updateUIText();
            } catch (Exception e) {
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.ui.update") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }
        } catch (Exception e) {
            GuiHelper.showErrorDialog(this,
                    ResourceManager.getString("error.language.general") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }
    }

    /**
     * Updates all UI text elements when the language changes.
     * Includes error handling for UI component operations.
     */
    private void updateUIText() {
        try {
            setTitle(ResourceManager.getString("app.title"));

            String currentCard = GuiHelper.getCurrentCardName(cardLayout);

            try {
                if ("login".equals(currentCard)) {
                    updateLoginPanel();
                } else if ("main".equals(currentCard)) {
                    updateMainPanel();
                }
                logger.info("UI text updated successfully for new language");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to update UI panels - ", e);
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.ui.update") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }

            revalidate();
            repaint();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Critical error updating UI text", e);
            GuiHelper.showErrorDialog(this,
                    ResourceManager.getString("error.ui.critical") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }
    }

    /**
     * Updates the login panel with new language text.
     */
    private void updateLoginPanel() {
        try {
            Container contentPane = getContentPane();
            contentPane.remove(loginPanel);
            createLoginPanel();
            cardPanel.add(loginPanel, "login");
            cardLayout.show(cardPanel, "login");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update login panel: " + e.getMessage(), e);
        }
    }

    /**
     * Updates the main panel with new language text.
     */
    private void updateMainPanel() {
        try {
            Container contentPane = getContentPane();
            contentPane.remove(mainPanel);
            createMainPanel();
            cardPanel.add(mainPanel, "main");

            if (currentUserName != null) {
                welcomeLabel.setText(ResourceManager.getString("welcome.user", currentUserName));
            } else {
                welcomeLabel.setText(ResourceManager.getString("welcome.message"));
            }

            cardLayout.show(cardPanel, "main");

            if (myBooksPanel != null && myBooksModel != null) {
                try {
                    loadMyBooksToTable(myBooksModel);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to update main panel: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update main panel: " + e.getMessage(), e);
        }
    }

    /**
     * Logs out the current user and returns to the login screen.
     * Clears all user-specific data from memory for security.
     * Resets the login form fields.
     */
    private void logout() {
        logger.info("User " + currentUser + " initiated logout");
        currentUser = null;
        currentUserName = null;
        key = null;
        welcomeLabel.setText(ResourceManager.getString("welcome.message"));
        idField.setText("");
        passwordField.setText("");
        cardLayout.show(cardPanel, "login");
        logger.info("User logout completed - returned to login screen");
    }

    /**
     * Creates the book browsing panel with search capabilities.
     * Contains a search field, button, and a table displaying available books.
     * Uses BorderLayout with the search panel at the top and table in the center.
     *
     * @return JPanel containing the book browsing interface
     */
    private JPanel createBrowseBooksPanel() {
        DataBaseManager dbm = new DataBaseManager();

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

        JPanel buttonPanel = new JPanel();
        JButton borrowButton = new JButton(ResourceManager.getString("button.borrow"));
        buttonPanel.add(borrowButton);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        loadBrowseBooksToTable(browseBooksTableModel, "");

        searchField.addActionListener(_ -> searchButton.doClick());

        searchButton.addActionListener(_ -> {
            String searchTerm = searchField.getText();
            loadBrowseBooksToTable(browseBooksTableModel, searchTerm);
        });

        // TODO: ADD BORROW BUTTON TO EACH ROW
        borrowButton.addActionListener(_ -> {
            try {
                if (GuiHelper.isRowSelected(tableComponents.table(), panel,
                        ResourceManager.getString("button.borrow.noselection"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= browseBooksTableModel.getRowCount()) {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.invalid.selection"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    String shelf = browseBooksTableModel.getValueAt(selectedRow, 0).toString();
                    String title = browseBooksTableModel.getValueAt(selectedRow, 1).toString();
                    String available = browseBooksTableModel.getValueAt(selectedRow, 4).toString();
                    
                    if (available.equals(ResourceManager.getString("no"))) {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.book.unavailable"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    if (dbm.hasUserBorrowedBook(currentUser, title, shelf, key)) {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.book.already.borrowed"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    boolean success = dbm.borrowBook(currentUser, shelf, title, key);
                    
                    if (success) {
                        JOptionPane.showMessageDialog(panel,
                                ResourceManager.getString("book.borrow.success", title),
                                ResourceManager.getString("success"),
                                JOptionPane.INFORMATION_MESSAGE);

                        loadBrowseBooksToTable(browseBooksTableModel, searchField.getText());

                        if (myBooksModel != null) {
                            loadMyBooksToTable(myBooksModel);
                        }

                    } else {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.borrow.failed"),
                                ResourceManager.getString("error")
                        );
                    }
                }
            } catch (Exception e) {
                GuiHelper.showErrorDialog(panel,
                        ResourceManager.getString("error.borrow.failed") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }
        });

        return panel;
    }

    /**
     * Loads book data into the browse books table based on a search term.
     * Includes error handling for database operations and data validation.
     *
     * @param model      The table model to populate
     * @param searchTerm The search term to filter books by
     */
    private void loadBrowseBooksToTable(DefaultTableModel model, String searchTerm) {
        // Clear existing table data before populating
        model.setRowCount(0);

        try {
            DataBaseManager dbm = new DataBaseManager();
            JsonArray books = dbm.findBooks(searchTerm);

            if (books == null) {
                // Handle database query failure with informative message
                model.addRow(new Object[]{
                        "-",
                        ResourceManager.getString("error.search.failed"),
                        "-", "-", "-"
                });
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.search.failed"),
                        ResourceManager.getString("error")
                );
                return;
            }

            if (books.isEmpty()) {
                // Display different messages based on whether search was empty or no results found
                model.addRow(new Object[]{
                        "-",
                        searchTerm.isEmpty() ?
                                ResourceManager.getString("books.none.database") :
                                ResourceManager.getString("books.none.search"),
                        "-", "-", "-"
                });
                return;
            }

            for (int i = 0; i < books.size(); i++) {
                try {
                    JsonObject book = books.get(i).getAsJsonObject();

                    // Skip books with missing required fields to prevent NullPointerException
                    if (!GuiHelper.hasRequiredBookFields(book)) {
                        continue; // Skip invalid book entries
                    }

                    String bookID = book.get("BookID").getAsString();
                    int shelfNumber = dbm.getShelfNumber(bookID);

                    if (shelfNumber == -1) {
                        shelfNumber = 0; // Default shelf value when not found
                    }

                    model.addRow(new Object[]{
                            shelfNumber,
                            book.get("Title").getAsString(),
                            book.get("Author").getAsString(),
                            book.get("Publisher").getAsString(),
                            // Convert numeric availability to localized Yes/No string
                            book.get("Available").getAsInt() > 0 ?
                                    ResourceManager.getString("yes") :
                                    ResourceManager.getString("no")
                    });
                } catch (Exception e) {
                    // Individual book processing errors don't stop the entire loading process
                    logger.log(Level.WARNING, "Error processing book data at index " + i, e);
                }
            }
        } catch (Exception e) {
            // Handle catastrophic database failures with a single error row
            logger.log(Level.SEVERE, "Failed to load user's borrowed books for: " + currentUser, e);

            model.addRow(new Object[]{
                    "-",
                    ResourceManager.getString("error.database"),
                    "-", "-", "-"
            });
            
            GuiHelper.showErrorDialog(this,
                    ResourceManager.getString("error.database") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
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
        DataBaseManager dbm = new DataBaseManager();

        String[] columns = {
                ResourceManager.getString("column.title"),
                ResourceManager.getString("column.dateissued"),
                ResourceManager.getString("column.datedue"),
                ResourceManager.getString("column.status")
        };

        TableUtils.TableComponents tableComponents = TableUtils.createCenteredTable(columns);
        DefaultTableModel myBooksTableModel = tableComponents.model();

        JButton returnButton = new JButton(ResourceManager.getString("button.return.text"));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(returnButton);

        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Store references for access by other methods (e.g., loadMyBooksToTable)
        myBooksPanel = panel;
        myBooksModel = myBooksTableModel;

        returnButton.addActionListener(_ -> {
            try {
                if (GuiHelper.isRowSelected(tableComponents.table(), panel,
                        ResourceManager.getString("button.return.noselection"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= myBooksTableModel.getRowCount()) {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.invalid.selection"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    String bookTitle = myBooksTableModel.getValueAt(selectedRow, 0).toString();
                    String dateDue = myBooksTableModel.getValueAt(selectedRow, 2).toString();
                    String status = myBooksTableModel.getValueAt(selectedRow, 3).toString();

                    // Check if this is a placeholder "No books" row
                    if (bookTitle.equals(ResourceManager.getString("books.none"))) {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.no.books"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(panel,
                            ResourceManager.getString("confirm.return") + " " + bookTitle + "?",
                            ResourceManager.getString("confirm"),
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            boolean success = false;

                            if (status.equals(ResourceManager.getString("status.overdue"))) {
                                int daysOverdue = dbm.getDaysOverdue(dateDue);

                                if (daysOverdue > 0) {
                                    double lateFee = GuiHelper.calculateFee(daysOverdue, dbm.getUserType(currentUser, key));

                                    int response = JOptionPane.showConfirmDialog(panel,
                                            ResourceManager.getString("confirm.return.overdue", lateFee),
                                            ResourceManager.getString("confirm"),
                                            JOptionPane.YES_NO_OPTION);

                                    if (response == JOptionPane.YES_OPTION) {
                                        //! IF THERE WAS A FULL PAYMENT SYSTEM, THIS IS WHERE IT WOULD BE HANDLED
                                        //! BUT CONSIDERING ITS NOT REQUIRED, WE JUST RETURN THE BOOK
                                        String bookID = dbm.findBookID(bookTitle);
                                        if (bookID != null) {
                                            success = dbm.returnBook(currentUser, bookID, key);
                                        }
                                    }
                                }
                            } else {
                                String bookID = dbm.findBookID(bookTitle);
                                if (bookID != null) {
                                    success = dbm.returnBook(currentUser, bookID, key);
                                }
                            }

                            if (success) {
                                JOptionPane.showMessageDialog(panel,
                                        ResourceManager.getString("book.return.success"),
                                        ResourceManager.getString("success"),
                                        JOptionPane.INFORMATION_MESSAGE);

                                loadMyBooksToTable(myBooksModel);
                            } else {
                                GuiHelper.showErrorDialog(panel,
                                        ResourceManager.getString("error.return.failed"),
                                        ResourceManager.getString("error")
                                );
                            }
                        } catch (Exception e) {
                            GuiHelper.showErrorDialog(panel,
                                    ResourceManager.getString("error.return.exception") + ": " + e.getMessage(),
                                    ResourceManager.getString("error")
                            );
                        }
                    }
                }
            } catch (Exception e) {
                GuiHelper.showErrorDialog(panel,
                        ResourceManager.getString("error.unexpected") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }
        });

        return panel;
    }

    /**
     * Loads the current user's borrowed books into the "My Books" table.
     * Includes error handling for database access and data validation.
     *
     * @param model The table model to populate with borrowed books
     */
    private void loadMyBooksToTable(DefaultTableModel model) {
        model.setRowCount(0);

        try {
            // Session validation check - both user and encryption key must exist
            if (currentUser == null || key == null) {
                model.addRow(new Object[]{
                        ResourceManager.getString("error.session"),
                        "", "", ""
                });
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.session.details"),
                        ResourceManager.getString("error")
                );
                return;
            }

            DataBaseManager dbm = new DataBaseManager();
            JsonArray books;

            try {
                books = dbm.findBorrowedBooks(currentUser, key);
            } catch (Exception e) {
                model.addRow(new Object[]{
                        ResourceManager.getString("error.load.books"),
                        "", "", ""
                });
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.load.books.details") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
                return;
            }

            if (books == null) {
                model.addRow(new Object[]{
                        ResourceManager.getString("error.database"),
                        "", "", ""
                });
                return;
            }

            if (books.isEmpty()) {
                model.addRow(new Object[]{
                        ResourceManager.getString("books.none"),
                        "", "", ""
                });
                return;
            }

            int loadErrors = 0;

            for (int i = 0; i < books.size(); i++) {
                try {
                    JsonObject book = books.get(i).getAsJsonObject();

                    // Validate required fields before processing
                    if (!book.has("BookID") || !book.has("DateIssued") || !book.has("Status")) {
                        loadErrors++;
                        continue;
                    }

                    String bookId = book.get("BookID").getAsString();
                    String title = dbm.getBookTitle(bookId);

                    // Use placeholder for books that may have been deleted from database
                    if (title == null) {
                        title = ResourceManager.getString("book.unknown") + " (" + bookId + ")";
                    }

                    String dateIssued;
                    try {
                        // DateIssued is stored encrypted and needs decryption
                        dateIssued = SecurityManager.decrypt(book.get("DateIssued").getAsString(), key);
                    } catch (Exception e) {
                        dateIssued = ResourceManager.getString("date.unknown");
                        loadErrors++;
                    }

                    String userType;
                    try {
                        userType = dbm.getUserType(currentUser, key);
                        if (userType == null) {
                            throw new Exception("User type not found");
                        }
                    } catch (Exception e) {
                        model.addRow(new Object[]{
                                title,
                                dateIssued,
                                ResourceManager.getString("date.unknown"),
                                ResourceManager.getString("status.unknown")
                        });
                        continue;
                    }

                    String dateDue = dbm.getDueDate(dateIssued, userType);
                    if (dateDue == null) {
                        dateDue = ResourceManager.getString("date.unknown");
                    }

                    int statusActual;
                    int statusSaved;

                    try {
                        // Calculate and synchronize book status if it has changed
                        statusActual = dbm.getDueStatus(dateDue);
                        statusSaved = book.get("Status").getAsInt();

                        if (statusActual != statusSaved) {
                            try {
                                dbm.updateDueStatus(currentUser, bookId, statusActual, key);
                            } catch (Exception e) {
                                logger.log(Level.WARNING, "Failed to update due status for book: " + bookId, e);
                            }
                        }
                    } catch (Exception e) {
                        statusActual = -2;  // Error status code
                    }

                    String statusMsg = GuiHelper.getStatusMessage(statusActual);

                    model.addRow(new Object[]{
                            title,
                            dateIssued,
                            dateDue,
                            statusMsg
                    });
                } catch (Exception e) {
                    loadErrors++;
                    logger.log(Level.WARNING, "Failed to load book data at index " + i, e);
                }
            }

            // Tracking errors lets us notify user about partial data issues
            if (loadErrors > 0) {
                GuiHelper.showErrorDialog(this,
                        String.format(ResourceManager.getString("error.load.some.books"), loadErrors),
                        ResourceManager.getString("warning")
                );
            }
        } catch (Exception e) {
            model.setRowCount(0);
            model.addRow(new Object[]{
                    ResourceManager.getString("error.unexpected"),
                    "", "", ""
            });
            GuiHelper.showErrorDialog(this,
                    ResourceManager.getString("error.unexpected.details") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
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
     * Includes error handling for component access and database operations.
     */
    private void switchToMainPanel() {
        logger.info("User " + currentUser + " successfully authenticated - switching to main panel");

        try {
            try {
                welcomeLabel.setText(ResourceManager.getString("welcome.user", currentUserName));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to update welcome message", e);
            }

            try {
                // Load user's borrowed books if the panel is already initialized
                if (myBooksPanel != null && myBooksModel != null) {
                    loadMyBooksToTable(myBooksModel);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to load user's borrowed books", e);
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.load.books") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }

            try {
                // Dynamically adjust tabs based on user type (add admin tab if needed)
                Component component = mainPanel.getComponent(1);
                if (component instanceof JTabbedPane tabbedPane) {
                    // Remove admin tab if it exists (index 2)
                    if (tabbedPane.getTabCount() > 2) {
                        tabbedPane.removeTabAt(2);
                    }

                    // Add admin tab only for admin users
                    if ("admin".equals(currentUser)) {
                        tabbedPane.addTab(ResourceManager.getString("tab.manage"), createManageBooksPanel());
                    }
                } else {
                    throw new ClassCastException("Expected JTabbedPane but found " +
                            (component != null ? component.getClass().getName() : "null"));
                }
            } catch (IndexOutOfBoundsException e) {
                logger.log(Level.WARNING, "Failed to access tabbed pane in main panel", e);
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.ui.component") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            } catch (ClassCastException e) {
                logger.log(Level.SEVERE, "Unexpected component type in main panel", e);
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.ui.component") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to update admin tab", e);
            }

            try {
                // CardLayout switches between different panels (login, main, etc.)
                cardLayout.show(cardPanel, "main");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to switch to main panel", e);
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.ui.navigation") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Critical error in switchToMainPanel", e);
            GuiHelper.showErrorDialog(this,
                    ResourceManager.getString("error.login.failed") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );

            try {
                // Fallback to login screen on critical error
                cardLayout.show(cardPanel, "login");
            } catch (Exception ex) {
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.critical") + ": " + e.getMessage(),
                        ResourceManager.getString("error")
                );
            }
        }
    }
}