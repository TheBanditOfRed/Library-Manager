package main.ui.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import main.core.DataBaseManager;
import main.core.ResourceManager;
import main.core.SessionManager;
import main.ui.GUI;
import main.ui.panels.forms.AddBookForm;
import main.ui.panels.forms.AddUserForm;
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
    public static JTextField shelfNumberField;

    /** Input field for the book title. */
    public static JTextField titleField;

    /** Input field for the book author. */
    public static JTextField authorField;

    /** Input field for the book publisher. */
    public static JTextField publisherField;

    /** Input field for the number of available copies of the book. */
    public static JTextField availableField;

    /** Input field for the number of copies of the book currently on loan. */
    public static JTextField onLoanField;

    /** Input field for the user ID. */
    public static JTextField userIdField;

    /** Input field for the user username. */
    public static JTextField userNameField;

    /** Input field for the user password. */
    public static JPasswordField userPasswordField;

    /** Input field for the user type. */
    public static JComboBox<String> userTypeComboBox;

    /**
     * Creates the Manage Books panel for administrators to add, edit, and search books.
     * Contains a search field, buttons for adding/editing books and users, and a table displaying book data.
     * Uses BorderLayout with the search panel at the top and table in the center.
     *
     * @return JPanel containing the manage books interface
     * @param gui The GUI instance to which the manage books panel will be added
     */
    public static JPanel createManageBooksPanel(GUI gui) {
        JPanel panel = new JPanel(new BorderLayout());

        manageBooksSearchField = new JTextField(15);
        JButton searchButton = new JButton(ResourceManager.getString("button.search"));

        // Book management buttons
        JPanel bookButtonPanel = new JPanel();
        bookButtonPanel.setBorder(BorderFactory.createTitledBorder("Book Management"));
        JButton addButton = new JButton(ResourceManager.getString("button.add"));
        JButton editButton = new JButton(ResourceManager.getString("button.edit"));
        JButton deleteButton = new JButton(ResourceManager.getString("button.delete"));
        bookButtonPanel.add(addButton);
        bookButtonPanel.add(editButton);
        bookButtonPanel.add(deleteButton);

        // User management buttons
        JPanel userButtonPanel = new JPanel();
        userButtonPanel.setBorder(BorderFactory.createTitledBorder("User Management"));
        JButton addUserButton = new JButton(ResourceManager.getString("button.adduser"));
        JButton editUserButton = new JButton(ResourceManager.getString("button.edituser"));
        JButton removeUserButton = new JButton(ResourceManager.getString("button.removeuser"));
        userButtonPanel.add(addUserButton);
        userButtonPanel.add(editUserButton);
        userButtonPanel.add(removeUserButton);

        // Combine button panels
        JPanel allButtonsPanel = new JPanel(new GridLayout(1, 2));
        allButtonsPanel.add(bookButtonPanel);
        allButtonsPanel.add(userButtonPanel);

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
        panel.add(allButtonsPanel, BorderLayout.SOUTH);

        loadAllBooksToTable(gui, manageBooksTableModel, "");

        // TODO: move action listeners to a separate method for better organization

        setupManageBooksActionListeners(gui, addButton, editButton, deleteButton, addUserButton, editUserButton, removeUserButton, searchButton, manageBooksSearchField, panel, manageBooksTableModel, tableComponents);

    return panel;
}

    /**
     * Sets up action listeners for the manage books panel buttons and search field.
     * Handles search functionality, book management operations (add, edit, delete),
     * and user management operations (add user, remove user).
     *
     * @param gui                    The GUI instance
     * @param addButton             Button for adding new books
     * @param editButton            Button for editing existing books
     * @param deleteButton          Button for deleting books
     * @param addUserButton         Button for adding new users
     * @param editUserButton        Button for editing existing users
     * @param removeUserButton      Button for removing users
     * @param searchButton          Button for searching books
     * @param manageBooksSearchField Text field for search input
     * @param panel                 The parent panel for dialogs
     * @param manageBooksTableModel  The table model for book data
     * @param tableComponents       Table components for row selection validation
     */
    private static void setupManageBooksActionListeners(GUI gui, JButton addButton, JButton editButton, JButton deleteButton, JButton addUserButton, JButton editUserButton, JButton removeUserButton, JButton searchButton, JTextField manageBooksSearchField, JPanel panel, DefaultTableModel manageBooksTableModel, TableUtils.TableComponents tableComponents) {
        manageBooksSearchField.addActionListener(_ -> searchButton.doClick());

        searchButton.addActionListener(_ -> {
            String searchTerm = manageBooksSearchField.getText();
            loadAllBooksToTable(gui, manageBooksTableModel, searchTerm);
        });

        addButton.addActionListener(_ -> {
            JPanel addBookFormPanel = AddBookForm.createAddBookFormPanel(gui);
            gui.cardPanel.add(addBookFormPanel, "addBook");
            gui.cardLayout.show(gui.cardPanel, "addBook");
        });

        editButton.addActionListener(_ -> editButtonHandler(gui, manageBooksTableModel, tableComponents, panel));

        deleteButton.addActionListener(_ -> deleteButtonHandler(gui, manageBooksTableModel, tableComponents, panel));

        // User management action listeners
        addUserButton.addActionListener(_ -> {
            JPanel addUserFormPanel = AddUserForm.createAddUserFormPanel(gui);
            gui.cardPanel.add(addUserFormPanel, "addUser");
            gui.cardLayout.show(gui.cardPanel, "addUser");
        });

        editUserButton.addActionListener(_ -> AddUserForm.showEditUserDialog(panel, gui));

        removeUserButton.addActionListener(_ -> AddUserForm.showRemoveUserDialog(panel));
    }

    /**
     * Handles the edit button action for modifying an existing book.
     * Validates that a row is selected, retrieves book data from the table,
     * and opens the edit form with pre-populated fields.
     *
     * @param gui                    The GUI instance
     * @param manageBooksTableModel  The table model containing book data
     * @param tableComponents       Table components for row selection validation
     * @param panel                 The parent panel for error dialogs
     */
    private static void editButtonHandler(GUI gui, DefaultTableModel manageBooksTableModel, TableUtils.TableComponents tableComponents, JPanel panel) {
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
                JPanel editBookFormPanel = AddBookForm.createAddBookFormPanel(gui, bookId, shelfNumber, title,
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
    }

    /**
     * Handles the delete button action for removing a book from the database.
     * Validates row selection, confirms deletion with the user, and performs
     * the database operation with appropriate user feedback.
     *
     * @param gui                    The GUI instance
     * @param manageBooksTableModel  The table model containing book data
     * @param tableComponents       Table components for row selection validation
     * @param panel                 The parent panel for dialogs
     */
    private static void deleteButtonHandler(GUI gui, DefaultTableModel manageBooksTableModel, TableUtils.TableComponents tableComponents, JPanel panel) {
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