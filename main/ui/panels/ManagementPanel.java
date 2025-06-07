package main.ui.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.core.DataBaseManager;
import main.core.ResourceManager;
import main.core.SessionManager;
import main.ui.GUI;
import main.ui.utils.DialogUtils;
import main.ui.utils.StatusUtils;
import main.ui.utils.TableUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ManagementPanel provides functionality for managing books in the library system.
 * It allows administrators to add, edit, and delete books, as well as search for them.
 * The panel includes a form for adding/editing books and a table for displaying book data.
 */
public class ManagementPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(ManagementPanel.class.getName());

    /** Table model for the book management interface */
    public static DefaultTableModel manageBooksTableModel;

    /** Search field for filtering books in management mode */
    public static JTextField manageBooksSearchField;

    /** Input fields for adding or editing book properties */
    private static JTextField shelfNumberField;

    /** Input field for the book title. */
    private static JTextField titleField;

    /** Input field for the book author. */
    private static JTextField authorField;

    /** Input field for the book publisher. */
    private static JTextField publisherField;

    /** Input field for the number of available copies of the book. */
    private static JTextField availableField;

    /** Input field for the number of copies of the book currently on loan. */
    private static JTextField onLoanField;

    /**
     * Creates a panel containing a form for adding a new book or editing an existing book.
     * The form includes fields for all book properties and handles validation and database operations.
     * When bookId is null, the form operates in "add new book" mode with empty fields.
     * When bookId is provided, the form operates in "edit book" mode with pre-populated fields.
     *
     * @param gui         The GUI instance to which the form will be added
     * @param bookId      The ID of the book to edit, or null when creating a new book
     * @param shelfNumber The shelf location number of the book (1-based), or null for a new book
     * @param title       The book title, or null for a new book
     * @param author      The book author, or null for a new book
     * @param publisher   The book publisher, or null for a new book
     * @param available   The number of available copies as a string, or null for a new book
     * @param onLoan      The number of copies currently on loan as a string, or null for a new book
     * @return JPanel containing the book form with input fields, validation, and action buttons
     */
    public static JPanel createAddBookFormPanel(GUI gui, String bookId, String shelfNumber, String title, String author, String publisher, String available, String onLoan) {
        JPanel panel = new JPanel(new BorderLayout());

        boolean isEditMode = bookId != null && !bookId.isEmpty();

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Populate fields with existing data or empty strings for new book
        shelfNumberField = new JTextField(shelfNumber != null ? shelfNumber : "", 15);
        titleField = new JTextField(title != null ? title : "", 15);
        authorField = new JTextField(author != null ? author : "", 15);
        publisherField = new JTextField(publisher != null ? publisher : "", 15);
        availableField = new JTextField(available != null ? available : "", 15);
        onLoanField = new JTextField(onLoan != null ? onLoan : "", 15);

        // Field labels
        JLabel shelfLabel = new JLabel(ResourceManager.getString("form.shelf") + ":");
        JLabel titleLabel = new JLabel(ResourceManager.getString("form.title") + ":");
        JLabel authorLabel = new JLabel(ResourceManager.getString("form.author") + ":");
        JLabel publisherLabel = new JLabel(ResourceManager.getString("form.publisher") + ":");
        JLabel availableLabel = new JLabel(ResourceManager.getString("form.available") + ":");
        JLabel onLoanLabel = new JLabel(ResourceManager.getString("form.onloan") + ":");

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton(isEditMode ? ResourceManager.getString("form.button.update") : ResourceManager.getString("form.button.add"));
        JButton cancelButton = new JButton(ResourceManager.getString("form.button.cancel"));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add title panel
        JPanel titlePanel = new JPanel();
        JLabel formTitle = new JLabel(isEditMode ? ResourceManager.getString("form.header.edit") : ResourceManager.getString("form.header.add"));
        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 16));
        titlePanel.add(formTitle);

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

        setupActionListeners(gui, saveButton, cancelButton, panel, isEditMode, bookId);


        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;

    }

    /**
     * Sets up action listeners for the form fields and buttons.
     * Handles focus transitions between fields and button actions for saving or canceling.
     *
     * @param gui         The GUI instance to which the form belongs
     * @param saveButton  The button to save the book data
     * @param cancelButton The button to cancel the operation
     * @param panel       The panel containing the form
     * @param isEditMode  True if editing an existing book, false if adding a new book
     * @param bookId      The ID of the book being edited, or null when adding a new book
     */
    private static void setupActionListeners(GUI gui, JButton saveButton, JButton cancelButton, JPanel panel, boolean isEditMode, String bookId){
        shelfNumberField.addActionListener(_ -> titleField.requestFocusInWindow());
        titleField.addActionListener(_ -> authorField.requestFocusInWindow());
        authorField.addActionListener(_ -> publisherField.requestFocusInWindow());
        publisherField.addActionListener(_ -> availableField.requestFocusInWindow());
        availableField.addActionListener(_ -> onLoanField.requestFocusInWindow());
        onLoanField.addActionListener(_ -> saveButton.doClick());

        cancelButton.addActionListener(_ -> PanelSwitcher.switchToManageBooksPanel(gui));

        saveButton.addActionListener(_ -> handleSave(gui, panel, isEditMode, bookId));
    }

    /**
     * Handles the save action for adding or updating a book.
     * Validates input fields, performs database operations, and provides user feedback.
     *
     * @param gui         The GUI instance to which the form belongs
     * @param panel       The panel containing the form
     * @param isEditMode  True if editing an existing book, false if adding a new book
     * @param bookId      The ID of the book being edited, or null when adding a new book
     */
    private static void handleSave(GUI gui, JPanel panel, boolean isEditMode, String bookId){
        try {
            // Validate input fields
            String newShelfNumber = shelfNumberField.getText().trim();
            String newTitle = titleField.getText().trim();
            String newAuthor = authorField.getText().trim();
            String newPublisher = publisherField.getText().trim();
            String newAvailable = availableField.getText().trim();
            String newOnLoan = onLoanField.getText().trim();

            if (StatusUtils.validateNewBookFields(newShelfNumber, newTitle, newAuthor, newPublisher, newAvailable, newOnLoan)){
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("validation.fields.required"),
                        ResourceManager.getString("validation.error")
                );
                return;
            }

            if (StatusUtils.validateNumericNewBookFields(newShelfNumber, newAvailable, newOnLoan)) {
                DialogUtils.showErrorDialog(panel,
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

                clearFields();

                PanelSwitcher.switchToManageBooksPanel(gui);
            } else {
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("error.book.add.failed") + "\n" + ResourceManager.getString("error.logs.check"),
                        "Database Error"
                );
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to add or update book: " + e.getMessage(), e);
            DialogUtils.showErrorDialog(panel,
                    ResourceManager.getString("error.book.add") + "\n" + ResourceManager.getString("error.logs.check"),
                    "Error"
            );
        }
    }

    /** Clears all input fields in the book form. */
    private static void clearFields(){
        shelfNumberField.setText("");
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        availableField.setText("");
        onLoanField.setText("");
    }

    /**
     * Creates a panel for adding a new book with empty fields.
     * This is used when no book ID is provided, indicating a new book creation.
     *
     * @return JPanel containing the add book form with empty fields
     * @param gui The GUI instance to which the form will be added
     */
    public static JPanel createAddBookFormPanel(GUI gui) {
        return createAddBookFormPanel(gui, null, null, null, null, null, null, null);
    }

    /**
     * Creates the Manage Books panel for administrators to add, edit, and search books.
     * Contains a search field, buttons for adding/editing books, and a table displaying book data.
     * Uses BorderLayout with the search panel at the top and table in the center.
     *
     * @return JPanel containing the manage books interface
     * @param gui The GUI instance to which the manage books panel will be added
     */
    public static JPanel createManageBooksPanel(GUI gui) {
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

        loadAllBooksToTable(gui, manageBooksTableModel, "");

        manageBooksSearchField.addActionListener(_ -> searchButton.doClick());
        searchButton.addActionListener(_ -> {
            String searchTerm = manageBooksSearchField.getText();
            loadAllBooksToTable(gui, manageBooksTableModel, searchTerm);
        });

        addButton.addActionListener(_ -> {
            JPanel addBookFormPanel = createAddBookFormPanel(gui);
            gui.cardPanel.add(addBookFormPanel, "addBook");
            gui.cardLayout.show(gui.cardPanel, "addBook");
        });

        editButton.addActionListener(_ -> {
            try {
                if (TableUtils.isRowSelected(tableComponents.table(), panel,
                        ResourceManager.getString("validation.select.edit"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= manageBooksTableModel.getRowCount()) {
                        DialogUtils.showErrorDialog(panel,
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
                    JPanel editBookFormPanel = createAddBookFormPanel(gui, bookId, shelfNumber, title,
                            author, publisher, available, onLoan);
                    gui.cardPanel.add(editBookFormPanel, "editBook");
                    gui.cardLayout.show(gui.cardPanel, "editBook");
                }
            } catch (Exception e) {
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("error.book.edit.open") + "\n" + ResourceManager.getString("error.log.check"),
                        ResourceManager.getString("error")
                );
            }
        });

        deleteButton.addActionListener(_ -> {
            try {
                if (TableUtils.isRowSelected(tableComponents.table(), panel,
                        ResourceManager.getString("validation.select.delete"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= manageBooksTableModel.getRowCount()) {
                        DialogUtils.showErrorDialog(panel,
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
                            loadAllBooksToTable(gui, manageBooksTableModel, manageBooksSearchField.getText());
                        } else {
                            DialogUtils.showErrorDialog(panel,
                                    ResourceManager.getString("error.book.delete.failed"),
                                    ResourceManager.getString("error")
                            );
                        }
                    }
                    gui.refreshAllBookTables();
                }
            } catch (Exception e) {
                DialogUtils.showErrorDialog(panel,
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
     * @param gui        The GUI instance to which the table belongs
     * @param model      The table model to populate
     * @param searchTerm The search term to filter books by
     */
    public static void loadAllBooksToTable(GUI gui, DefaultTableModel model, String searchTerm) {
        // Clear existing table data before populating
        model.setRowCount(0);

        try {
            DataBaseManager dbm = new DataBaseManager();
            JsonArray books = dbm.findBooks(searchTerm);

            if (books == null) {
                DialogUtils.showErrorDialog(gui,
                        ResourceManager.getString("error.search.failed"),
                        ResourceManager.getString("error")
                );
                return;
            }

            if (books.isEmpty()) {
                // Display different messages based on whether search was empty or no results found
                JOptionPane.showMessageDialog(
                        gui,
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
                    if (StatusUtils.hasRequiredBookFields(book)) {
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
            logger.log(Level.SEVERE, "Failed to load user's borrowed books for: " + SessionManager.getInstance().getCurrentUser(), e);
            DialogUtils.showErrorDialog(gui,
                    ResourceManager.getString("error.database") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }
    }
}
