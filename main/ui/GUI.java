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
    private final JPanel cardPanel;

    /** Layout manager for switching between different UI screens */
    private final CardLayout cardLayout;

    /** User preferences storage for settings like language selection */
    private final Preferences prefs = Preferences.userNodeForPackage(GUI.class);

    /** Panel containing the login interface */
    private JPanel loginPanel;

    /** Panel containing the main application interface */
    private JPanel mainPanel;

    /** Password input field for user authentication */
    private JPasswordField passwordField;

    /** User ID input field for authentication */
    private JTextField idField;

    /** ID of the currently logged-in user */
    private String currentUser;

    /** Label displaying welcome message with user's name */
    private JLabel welcomeLabel;

    /** Display name of the current user */
    private String currentUserName;

    /** Encryption key derived from user's password */
    private String key;

    /** Panel displaying books borrowed by the current user */
    private JPanel myBooksPanel;

    /** Table model for the book browsing interface */
    private DefaultTableModel browseBooksTableModel;

    /** Table model for the book management interface */
    private DefaultTableModel manageBooksTableModel;

    /** Table model for the user's borrowed books */
    private DefaultTableModel myBooksTableModel;

    /** Search field for filtering books in browse mode */
    private JTextField browseBooksSearchField;

    /** Search field for filtering books in management mode */
    private JTextField manageBooksSearchField;


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

        createLoginPanel();
        createMainPanel();

        cardPanel.add(loginPanel, "login");
        cardPanel.add(mainPanel, "main");

        cardLayout.show(cardPanel, "login");

        setContentPane(cardPanel);
        logger.log(Level.INFO, "GUI initialization completed - displaying login screen");
    }

    /**
     * Sets the application icon with multiple sizes for better OS compatibility.
     */
    private void setApplicationIcon() {
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
                logger.log(Level.INFO, "Login attempt with empty credentials");

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
                        logger.log(Level.INFO, "Successful login for user: " + id);
                        // Password acts as encryption key for other user data
                        key = password;

                        String encryptedID = user.get("UserID").getAsString();
                        currentUser = SecurityManager.decrypt(encryptedID, password);

                        String encryptedName = user.get("Name").getAsString();
                        currentUserName = SecurityManager.decrypt(encryptedName, password);

                        switchToMainPanel();
                    } else {
                        logger.log(Level.WARNING, "Failed login attempt - incorrect password for user: " + id);
                        GuiHelper.showErrorDialog(loginPanel,
                                ResourceManager.getString("login.error.invalid.password"),
                                ResourceManager.getString("error")
                        );
                    }
                } catch (RuntimeException ex) {
                    logger.log(Level.WARNING, "Failed login attempt - decryption failed for user: " + id);
                    // Decryption failure treated as authentication failure
                    GuiHelper.showErrorDialog(loginPanel,
                            ResourceManager.getString("login.error.invalid.password"),
                            ResourceManager.getString("error")
                    );
                }
            } else {
                logger.log(Level.WARNING, "Failed login attempt - user not found: " + id);
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
        logger.log(Level.INFO, "User " + (currentUser != null ? currentUser : "unknown") + " changing language from " + currentLanguage + " to " + languageCode);

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
                logger.log(Level.INFO, "Language preference saved successfully: " + languageCode);
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
                logger.log(Level.INFO, "UI text updated successfully for new language");
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

            if (myBooksPanel != null && myBooksTableModel != null) {
                try {
                    loadMyBooksToTable(myBooksTableModel);
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
        logger.log(Level.INFO, "User " + currentUser + " initiated logout");
        currentUser = null;
        currentUserName = null;
        key = null;
        welcomeLabel.setText(ResourceManager.getString("welcome.message"));
        idField.setText("");
        passwordField.setText("");
        cardLayout.show(cardPanel, "login");
        logger.log(Level.INFO, "User logout completed - returned to login screen");
    }

    /**
     * Creates a panel containing a form for adding a new book or editing an existing book.
     * The form includes fields for all book properties and handles validation and database operations.
     * When bookId is null, the form operates in "add new book" mode with empty fields.
     * When bookId is provided, the form operates in "edit book" mode with pre-populated fields.
     *
     * @param bookId      The ID of the book to edit, or null when creating a new book
     * @param shelfNumber The shelf location number of the book (1-based), or null for a new book
     * @param title       The book title, or null for a new book
     * @param author      The book author, or null for a new book
     * @param publisher   The book publisher, or null for a new book
     * @param available   The number of available copies as a string, or null for a new book
     * @param onLoan      The number of copies currently on loan as a string, or null for a new book
     * @return JPanel containing the book form with input fields, validation, and action buttons
     */
    private JPanel createAddBookFormPanel(String bookId, String shelfNumber, String title, String author, String publisher, String available, String onLoan) {
        JPanel panel = new JPanel(new BorderLayout());

        boolean isEditMode = bookId != null && !bookId.isEmpty();

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField shelfNumberField = new JTextField(shelfNumber != null ? shelfNumber : "", 15);
        JTextField titleField = new JTextField(title != null ? title : "", 15);
        JTextField authorField = new JTextField(author != null ? author : "", 15);
        JTextField publisherField = new JTextField(publisher != null ? publisher : "", 15);
        JTextField availableField = new JTextField(available != null ? available : "", 15);
        JTextField onLoanField = new JTextField(onLoan != null ? onLoan : "", 15);

        // Labels
        JLabel shelfLabel = new JLabel(ResourceManager.getString("form.shelf") + ":");
        JLabel titleLabel = new JLabel(ResourceManager.getString("form.title") + ":");
        JLabel authorLabel = new JLabel(ResourceManager.getString("form.author") + ":");
        JLabel publisherLabel = new JLabel(ResourceManager.getString("form.publisher") + ":");
        JLabel availableLabel = new JLabel(ResourceManager.getString("form.available") + ":");
        JLabel onLoanLabel = new JLabel(ResourceManager.getString("form.onloan") + ":");

        // Add components to form
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(shelfLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(shelfNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(authorLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(publisherLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(publisherField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(availableLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(availableField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(onLoanLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(onLoanField, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton(isEditMode ? ResourceManager.getString("form.button.update") : ResourceManager.getString("form.button.add"));
        JButton cancelButton = new JButton(ResourceManager.getString("form.button.cancel"));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add action listeners
        saveButton.addActionListener(_ -> {
            try {
                // Validate input fields
                String newShelfNumber = shelfNumberField.getText().trim();
                String newTitle = titleField.getText().trim();
                String newAuthor = authorField.getText().trim();
                String newPublisher = publisherField.getText().trim();
                String newAvailable = availableField.getText().trim();
                String newOnLoan = onLoanField.getText().trim();

                if (newShelfNumber.isEmpty() || newTitle.isEmpty() || newAuthor.isEmpty() ||
                        newPublisher.isEmpty() || newAvailable.isEmpty() || newOnLoan.isEmpty()) {
                    GuiHelper.showErrorDialog(panel,
                            ResourceManager.getString("validation.fields.required"),
                            ResourceManager.getString("validation.error")
                    );
                    return;
                }

                // Validate numeric fields
                try {
                    Integer.parseInt(newShelfNumber);
                    Integer.parseInt(newAvailable);
                    Integer.parseInt(newOnLoan);
                } catch (NumberFormatException e) {
                    GuiHelper.showErrorDialog(panel,
                            ResourceManager.getString("validation.numeric"),
                            ResourceManager.getString("validation.error")
                    );
                    return;
                }

                DataBaseManager dbm = new DataBaseManager();
                boolean success;
                String successMessage;

                if (isEditMode) {
                    success = dbm.updateBook(
                            bookId,
                            newShelfNumber,
                            newTitle,
                            newAuthor,
                            newPublisher,
                            Integer.parseInt(newAvailable),
                            Integer.parseInt(newOnLoan)
                    );
                    successMessage = ResourceManager.getString("book.update.success");

                } else {
                    success = dbm.addBook(
                            newShelfNumber,
                            newTitle,
                            newAuthor,
                            newPublisher,
                            Integer.parseInt(newAvailable),
                            Integer.parseInt(newOnLoan)
                    );
                    successMessage = ResourceManager.getString("book.add.success");
                }


                if (success) {
                    JOptionPane.showMessageDialog(panel,
                            successMessage,
                            ResourceManager.getString("success"),
                            JOptionPane.INFORMATION_MESSAGE);

                    shelfNumberField.setText("");
                    titleField.setText("");
                    authorField.setText("");
                    publisherField.setText("");
                    availableField.setText("");
                    onLoanField.setText("");

                    switchToManageBooksPanel();
                } else {
                    GuiHelper.showErrorDialog(panel,
                            ResourceManager.getString("error.book.add.failed") + "\n" + ResourceManager.getString("error.logs.check"),
                            "Database Error"
                    );
                }

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to add or update book: " + e.getMessage(), e);
                GuiHelper.showErrorDialog(panel,
                        ResourceManager.getString("error.book.add") + "\n" + ResourceManager.getString("error.logs.check"),
                        "Error"
                );
            }
        });

        cancelButton.addActionListener(_ -> switchToManageBooksPanel());

        // Add title panel
        JPanel titlePanel = new JPanel();
        JLabel formTitle = new JLabel(isEditMode ? ResourceManager.getString("form.header.edit") : ResourceManager.getString("form.header.add"));
        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 16));
        titlePanel.add(formTitle);

        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;

    }

    /**
     * Creates a panel for adding a new book with empty fields.
     * This is used when no book ID is provided, indicating a new book creation.
     *
     * @return JPanel containing the add book form with empty fields
     */
    private JPanel createAddBookFormPanel() {
        return createAddBookFormPanel(null, null, null, null, null, null, null);
    }

    /**
     * Switches the view to the Manage Books panel in the main interface.
     */
    private void switchToManageBooksPanel() {
        cardLayout.show(cardPanel, "main");

        try {
            Component component = mainPanel.getComponent(1);
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
            GuiHelper.showErrorDialog(mainPanel,
                    ResourceManager.getString("error.switch.panel") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }

        refreshAllBookTables();
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
        browseBooksSearchField = new JTextField(20);
        JButton searchButton = new JButton(ResourceManager.getString("button.search"));

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel(ResourceManager.getString("search.label") + ":"));
        searchPanel.add(browseBooksSearchField);
        searchPanel.add(searchButton);

        JPanel buttonPanel = new JPanel();
        JButton borrowButton = new JButton(ResourceManager.getString("button.borrow"));
        buttonPanel.add(borrowButton);

        String[] columns = {
                ResourceManager.getString("column.shelf"),
                ResourceManager.getString("column.title"),
                ResourceManager.getString("column.author"),
                ResourceManager.getString("column.publisher"),
                ResourceManager.getString("column.available")
        };

        TableUtils.TableComponents tableComponents = TableUtils.createCenteredTable(columns);
        browseBooksTableModel = tableComponents.model();


        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        loadBrowseBooksToTable(browseBooksTableModel, "");

        browseBooksSearchField.addActionListener(_ -> searchButton.doClick());

        searchButton.addActionListener(_ -> {
            String searchTerm = browseBooksSearchField.getText();
            loadBrowseBooksToTable(browseBooksTableModel, searchTerm);
        });

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

                        loadBrowseBooksToTable(browseBooksTableModel, browseBooksSearchField.getText());

                        if (myBooksTableModel != null) {
                            loadMyBooksToTable(myBooksTableModel);
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
     * Retrieves books from the database using DataBaseManager.findBooks() and populates
     * the table with book information including ID, title, author, publisher, and availability.
     * Empty search terms return all books. Handles database errors gracefully by logging
     * and showing error dialogs to the user.
     *
     * @param model      The DefaultTableModel to populate with book data
     * @param searchTerm The search term to filter books by (empty string returns all books)
     */
    private void loadBrowseBooksToTable(DefaultTableModel model, String searchTerm) {
        // Clear existing table data before populating
        model.setRowCount(0);

        try {
            DataBaseManager dbm = new DataBaseManager();
            JsonArray books = dbm.findBooks(searchTerm);

            if (books == null) {
                // Handle database query failure with informative message
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.search.failed"),
                        ResourceManager.getString("error")
                );
                return;
            }

            if (books.isEmpty()) {
                // Display different messages based on whether search was empty or no results found
                JOptionPane.showMessageDialog(
                        this,
                        ResourceManager.getString("books.none"),
                        ResourceManager.getString("info"),
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            for (int i = 0; i < books.size(); i++) {
                try {
                    JsonObject book = books.get(i).getAsJsonObject();

                    // Skip books with missing required fields to prevent NullPointerException
                    if (GuiHelper.hasRequiredBookFields(book)) {
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
        myBooksTableModel = tableComponents.model();

        JButton returnButton = new JButton(ResourceManager.getString("button.return.text"));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(returnButton);

        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Store references for access by other methods (e.g., loadMyBooksToTable)
        myBooksPanel = panel;

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

                                loadMyBooksToTable(myBooksTableModel);
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
     * Retrieves borrowed books using DataBaseManager.findBorrowedBooks() and displays
     * book titles, issue dates, due dates, status, and calculated fees.
     * Status includes visual indicators: on time, due today, or overdue.
     * Handles encryption/decryption of user data and database access errors.
     *
     * @param model The DefaultTableModel to populate with the user's borrowed books
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
     * Creates the Manage Books panel for administrators to add, edit, and search books.
     * Contains a search field, buttons for adding/editing books, and a table displaying book data.
     * Uses BorderLayout with the search panel at the top and table in the center.
     *
     * @return JPanel containing the manage books interface
     */
    private JPanel createManageBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        manageBooksSearchField = new JTextField(15);
        JButton searchButton = new JButton(ResourceManager.getString("button.search"));

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton(ResourceManager.getString("button.add"));
        JButton editButton = new JButton(ResourceManager.getString("button.edit"));
        JButton deleteButton = new JButton(ResourceManager.getString("button.delete"));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel(ResourceManager.getString("button.search") + ":"));
        searchPanel.add(manageBooksSearchField);
        searchPanel.add(searchButton);

        String[] columns = {
                ResourceManager.getString("column.bookid"),
                ResourceManager.getString("column.shelf"),
                ResourceManager.getString("column.title"),
                ResourceManager.getString("column.author"),
                ResourceManager.getString("column.publisher"),
                ResourceManager.getString("column.available"),
                ResourceManager.getString("column.onloan")
        };

        TableUtils.TableComponents tableComponents = TableUtils.createCenteredTable(columns);
        manageBooksTableModel = tableComponents.model();

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        loadAllBooksToTable(manageBooksTableModel, "");

        manageBooksSearchField.addActionListener(_ -> searchButton.doClick());
        searchButton.addActionListener(_ -> {
            String searchTerm = manageBooksSearchField.getText();
            loadAllBooksToTable(manageBooksTableModel, searchTerm);
        });

        addButton.addActionListener(_ -> {
            JPanel addBookFormPanel = createAddBookFormPanel();
            cardPanel.add(addBookFormPanel, "addBook");
            cardLayout.show(cardPanel, "addBook");
        });

        editButton.addActionListener(_ -> {
            try {
                if (GuiHelper.isRowSelected(tableComponents.table(), panel,
                        ResourceManager.getString("validation.select.edit"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= manageBooksTableModel.getRowCount()) {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.invalid.selection"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    // Get the selected book data from the table
                    String bookId = manageBooksTableModel.getValueAt(selectedRow, 0).toString();
                    String shelfNumber = manageBooksTableModel.getValueAt(selectedRow, 1).toString();
                    String title = manageBooksTableModel.getValueAt(selectedRow, 2).toString();
                    String author = manageBooksTableModel.getValueAt(selectedRow, 3).toString();
                    String publisher = manageBooksTableModel.getValueAt(selectedRow, 4).toString();
                    String available = manageBooksTableModel.getValueAt(selectedRow, 5).toString();
                    String onLoan = manageBooksTableModel.getValueAt(selectedRow, 6).toString();

                    // Create and show the edit form with the selected book data
                    JPanel editBookFormPanel = createAddBookFormPanel(bookId, shelfNumber, title,
                            author, publisher, available, onLoan);
                    cardPanel.add(editBookFormPanel, "editBook");
                    cardLayout.show(cardPanel, "editBook");
                }
            } catch (Exception e) {
                GuiHelper.showErrorDialog(panel,
                        ResourceManager.getString("error.book.edit.open") + "\n" + ResourceManager.getString("error.log.check"),
                        ResourceManager.getString("error")
                );
            }
        });

        deleteButton.addActionListener(_ -> {
            try {
                if (GuiHelper.isRowSelected(tableComponents.table(), panel,
                        ResourceManager.getString("validation.select.delete"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= manageBooksTableModel.getRowCount()) {
                        GuiHelper.showErrorDialog(panel,
                                ResourceManager.getString("error.invalid.selection"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    String bookId = manageBooksTableModel.getValueAt(selectedRow, 0).toString();
                    String title = manageBooksTableModel.getValueAt(selectedRow, 2).toString();

                    int confirm = JOptionPane.showConfirmDialog(panel,
                            ResourceManager.getString("confirm.delete", title),
                            ResourceManager.getString("confirm"),
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        DataBaseManager dbm = new DataBaseManager();
                        boolean success = dbm.deleteBook(bookId);

                        if (success) {
                            JOptionPane.showMessageDialog(panel,
                                    ResourceManager.getString("book.delete.success", title),
                                    ResourceManager.getString("success"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadAllBooksToTable(manageBooksTableModel, manageBooksSearchField.getText());
                        } else {
                            GuiHelper.showErrorDialog(panel,
                                    ResourceManager.getString("error.book.delete.failed"),
                                    ResourceManager.getString("error")
                            );
                        }
                    }
                    refreshAllBookTables();
                }
            } catch (Exception e) {
                GuiHelper.showErrorDialog(panel,
                        ResourceManager.getString("error.book.delete.failed") + "\n" + ResourceManager.getString("error.log.check"),
                        ResourceManager.getString("error")
                );
            }
        });

        return panel;
    }

    /**
     * Loads all books into the manage books table based on a search term.
     * Includes error handling for database access and data validation.
     *
     * @param model      The table model to populate
     * @param searchTerm The search term to filter books by
     */
    private void loadAllBooksToTable(DefaultTableModel model, String searchTerm) {
        // Clear existing table data before populating
        model.setRowCount(0);

        try {
            DataBaseManager dbm = new DataBaseManager();
            JsonArray books = dbm.findBooks(searchTerm);

            if (books == null) {
                GuiHelper.showErrorDialog(this,
                        ResourceManager.getString("error.search.failed"),
                        ResourceManager.getString("error")
                );
                return;
            }

            if (books.isEmpty()) {
                // Display different messages based on whether search was empty or no results found
                JOptionPane.showMessageDialog(
                        this,
                        searchTerm.isEmpty() ?
                                ResourceManager.getString("books.none.database") :
                                ResourceManager.getString("books.none.search"),
                        ResourceManager.getString("info"),
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            for (int i = 0; i < books.size(); i++) {
                try {
                    JsonObject book = books.get(i).getAsJsonObject();

                    // Skip books with missing required fields to prevent NullPointerException
                    if (GuiHelper.hasRequiredBookFields(book)) {
                        continue; // Skip invalid book entries
                    }

                    String bookID = book.get("BookID").getAsString();
                    int shelfNumber = dbm.getShelfNumber(bookID);

                    if (shelfNumber == -1) {
                        shelfNumber = 0; // Default shelf value when not found
                    }

                    model.addRow(new Object[]{
                            bookID,
                            shelfNumber,
                            book.get("Title").getAsString(),
                            book.get("Author").getAsString(),
                            book.get("Publisher").getAsString(),
                            book.get("Available").getAsString(),
                            book.get("OnLoan").getAsString()
                    });
                } catch (Exception e) {
                    // Individual book processing errors don't stop the entire loading process
                    logger.log(Level.WARNING, "Error processing book data at index " + i, e);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load user's borrowed books for: " + currentUser, e);
            GuiHelper.showErrorDialog(this,
                    ResourceManager.getString("error.database") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }
    }

    /**
     * Refreshes all tables that display book data after database changes.
     */
    private void refreshAllBookTables() {
        try {
            logger.log(Level.INFO, "Refreshing all book tables after database update");

            // Refresh browse books table
            if (browseBooksTableModel != null && browseBooksSearchField != null) {
                loadBrowseBooksToTable(browseBooksTableModel, browseBooksSearchField.getText());
            }

            // Refresh manage books table (admin only)
            if (manageBooksTableModel != null && manageBooksSearchField != null) {
                loadAllBooksToTable(manageBooksTableModel, manageBooksSearchField.getText());
            }

            // Refresh my books table (if user is logged in)
            if (myBooksTableModel != null && currentUser != null) {
                loadMyBooksToTable(myBooksTableModel);
            }

            logger.log(Level.INFO, "All book tables refreshed successfully");

        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to refresh some tables", e);
        }
    }


    /**
     * Switches from the login screen to the main application screen.
     * Includes error handling for component access and database operations.
     */
    private void switchToMainPanel() {
        logger.log(Level.INFO, "User " + currentUser + " successfully authenticated - switching to main panel");

        try {
            try {
                welcomeLabel.setText(ResourceManager.getString("welcome.user", currentUserName));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to update welcome message", e);
            }

            try {
                // Load user's borrowed books if the panel is already initialized
                if (myBooksPanel != null && myBooksTableModel != null) {
                    loadMyBooksToTable(myBooksTableModel);
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