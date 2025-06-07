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
public class BrowseBooksPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(BrowseBooksPanel.class.getName());

    /** Table model for the book browsing interface */
    public static DefaultTableModel browseBooksTableModel;

    /** Search field for filtering books in browse mode */
    public static JTextField browseBooksSearchField;

    /**
     * Creates the book browsing panel with search capabilities.
     * Contains a search field, button, and a table displaying available books.
     * Uses BorderLayout with the search panel at the top and table in the center.
     *
     * @return JPanel containing the book browsing interface
     * @param gui The GUI instance to which the panel will be added
     */
    public static JPanel createBrowseBooksPanel(GUI gui) {
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

        loadBrowseBooksToTable(gui, browseBooksTableModel, "");

        browseBooksSearchField.addActionListener(_ -> searchButton.doClick());

        searchButton.addActionListener(_ -> {
            String searchTerm = browseBooksSearchField.getText();
            loadBrowseBooksToTable(gui, browseBooksTableModel, searchTerm);
        });

        borrowButton.addActionListener(_ -> {
            try {
                if (TableUtils.isRowSelected(tableComponents.table(), panel,
                        ResourceManager.getString("button.borrow.noselection"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= browseBooksTableModel.getRowCount()) {
                        DialogUtils.showErrorDialog(panel,
                                ResourceManager.getString("error.invalid.selection"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    String shelf = browseBooksTableModel.getValueAt(selectedRow, 0).toString();
                    String title = browseBooksTableModel.getValueAt(selectedRow, 1).toString();
                    String available = browseBooksTableModel.getValueAt(selectedRow, 4).toString();

                    if (available.equals(ResourceManager.getString("no"))) {
                        DialogUtils.showErrorDialog(panel,
                                ResourceManager.getString("error.book.unavailable"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    if (dbm.hasUserBorrowedBook(SessionManager.getInstance().getCurrentUser(), title, shelf, SessionManager.getInstance().getKey())) {
                        DialogUtils.showErrorDialog(panel,
                                ResourceManager.getString("error.book.already.borrowed"),
                                ResourceManager.getString("error")
                        );
                        return;
                    }

                    boolean success = dbm.borrowBook(SessionManager.getInstance().getCurrentUser(), shelf, title, SessionManager.getInstance().getKey());

                    if (success) {
                        JOptionPane.showMessageDialog(panel,
                                ResourceManager.getString("book.borrow.success", title),
                                ResourceManager.getString("success"),
                                JOptionPane.INFORMATION_MESSAGE);

                        loadBrowseBooksToTable(gui, browseBooksTableModel, browseBooksSearchField.getText());

                        if (MyBooksPanel.myBooksTableModel != null) {
                            MyBooksPanel.loadMyBooksToTable(gui, MyBooksPanel.myBooksTableModel);
                        }

                    } else {
                        DialogUtils.showErrorDialog(panel,
                                ResourceManager.getString("error.borrow.failed"),
                                ResourceManager.getString("error")
                        );
                    }
                }
            } catch (Exception e) {
                DialogUtils.showErrorDialog(panel,
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
     * @param gui        The GUI instance to which the table belongs
     * @param model      The DefaultTableModel to populate with book data
     * @param searchTerm The search term to filter books by (empty string returns all books)
     */
    public static void loadBrowseBooksToTable(GUI gui, DefaultTableModel model, String searchTerm) {
        // Clear existing table data before populating
        model.setRowCount(0);

        try {
            DataBaseManager dbm = new DataBaseManager();
            JsonArray books = dbm.findBooks(searchTerm);

            if (books == null) {
                // Handle database query failure with informative message
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
                    if (StatusUtils.hasRequiredBookFields(book)) {
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
            logger.log(Level.SEVERE, "Failed to load user's borrowed books for: " + SessionManager.getInstance().getCurrentUser(), e);

            DialogUtils.showErrorDialog(gui,
                    ResourceManager.getString("error.database") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }
    }
}
