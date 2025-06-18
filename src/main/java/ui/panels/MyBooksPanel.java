package ui.panels;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import core.DataBaseManager;
import core.*;
import core.SecurityManager;
import ui.GUI;
import ui.utils.DialogUtils;
import ui.utils.StatusUtils;
import ui.utils.TableUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MyBooksPanel is responsible for displaying the books borrowed by the current user.
 * It provides a table interface to view book details and a button to return books.
 * The panel handles loading data from the database and updating the UI accordingly.
 */
public class MyBooksPanel extends JPanel {
    private static final Logger logger = Logger.getLogger(MyBooksPanel.class.getName());

    /** Panel displaying books borrowed by the current user */
    public static JPanel myBooksPanel;

    /** Table model for the user's borrowed books */
    public static DefaultTableModel myBooksTableModel;

    /** Label displaying total fines if any exist */
    public static JLabel totalFinesLabel;

    /**
     * Creates the panel displaying books borrowed by the current user.
     * Contains a table showing book titles, issue dates, due dates, and status.
     * Includes a return button at the bottom and displays total fines if any exist.
     *
     * @return JPanel containing the user's borrowed books interface
     * @param gui The GUI instance to which the panel will be added
     */
    public static JPanel createMyBooksPanel(GUI gui) {
        JPanel panel = new JPanel(new BorderLayout());
        DataBaseManager dbm = new DataBaseManager();

        String[] columns = {
                core.ResourceManager.getString("column.title"),
                core.ResourceManager.getString("column.dateissued"),
                ResourceManager.getString("column.datedue"),
                ResourceManager.getString("column.status")
        };

        TableUtils.TableComponents tableComponents = TableUtils.createCenteredTable(columns);
        myBooksTableModel = tableComponents.model();

        JButton returnButton = new JButton(core.ResourceManager.getString("button.return.text"));
        
        // Create fines label
        totalFinesLabel = new JLabel();
        totalFinesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalFinesLabel.setFont(totalFinesLabel.getFont().deriveFont(Font.BOLD, 14f));
        totalFinesLabel.setForeground(Color.RED);
        totalFinesLabel.setVisible(false); // Initially hidden
        
        // Create button panel with fines display
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(totalFinesLabel, BorderLayout.NORTH);
        
        JPanel returnButtonPanel = new JPanel();
        returnButtonPanel.add(returnButton);
        buttonPanel.add(returnButtonPanel, BorderLayout.CENTER);

        panel.add(tableComponents.scrollPane(), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Store references for access by other methods
        myBooksPanel = panel;

        returnButton.addActionListener(_ -> {
            try {
                if (TableUtils.isRowSelected(tableComponents.table(), panel,
                        core.ResourceManager.getString("button.return.noselection"))) {
                    int selectedRow = tableComponents.table().getSelectedRow();

                    if (selectedRow < 0 || selectedRow >= myBooksTableModel.getRowCount()) {
                        DialogUtils.showErrorDialog(panel,
                                ResourceManager.getString("error.invalid.selection"),
                                core.ResourceManager.getString("error")
                        );
                        return;
                    }

                    String bookTitle = myBooksTableModel.getValueAt(selectedRow, 0).toString();
                    String dateDue = myBooksTableModel.getValueAt(selectedRow, 2).toString();
                    String status = myBooksTableModel.getValueAt(selectedRow, 3).toString();

                    // Check if this is a placeholder "No books" row
                    if (bookTitle.equals(ResourceManager.getString("books.none"))) {
                        DialogUtils.showErrorDialog(panel,
                                ResourceManager.getString("error.no.books"),
                                core.ResourceManager.getString("error")
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
                                    double lateFee = FeeManager.calculateFee(daysOverdue, dbm.getUserType(SessionManager.getInstance().getCurrentUser(), SessionManager.getInstance().getKey()));

                                    int response = JOptionPane.showConfirmDialog(panel,
                                            core.ResourceManager.getString("confirm.return.overdue", String.format("%.2f", lateFee)),
                                            ResourceManager.getString("confirm"),
                                            JOptionPane.YES_NO_OPTION);

                                    if (response == JOptionPane.YES_OPTION) {
                                        //! IF THERE WAS A FULL PAYMENT SYSTEM, THIS IS WHERE IT WOULD BE HANDLED
                                        //! BUT CONSIDERING ITS NOT REQUIRED, WE JUST RETURN THE BOOK
                                        String bookID = dbm.findBookID(bookTitle);
                                        if (bookID != null) {
                                            success = dbm.returnBook(SessionManager.getInstance().getCurrentUser(), bookID, SessionManager.getInstance().getKey());
                                        }
                                    }
                                }
                            } else {
                                String bookID = dbm.findBookID(bookTitle);
                                if (bookID != null) {
                                    success = dbm.returnBook(SessionManager.getInstance().getCurrentUser(), bookID, core.SessionManager.getInstance().getKey());
                                }
                            }

                            if (success) {
                                JOptionPane.showMessageDialog(panel,
                                        core.ResourceManager.getString("book.return.success"),
                                        ResourceManager.getString("success"),
                                        JOptionPane.INFORMATION_MESSAGE);

                                loadMyBooksToTable(gui, myBooksTableModel);

                                updateFinesDisplay();
                            } else {
                                DialogUtils.showErrorDialog(panel,
                                        ResourceManager.getString("error.return.failed"),
                                        core.ResourceManager.getString("error")
                                );
                            }
                        } catch (Exception e) {
                            DialogUtils.showErrorDialog(panel,
                                    ResourceManager.getString("error.return.exception") + ": " + e.getMessage(),
                                    core.ResourceManager.getString("error")
                            );
                        }
                    }
                }
            } catch (Exception e) {
                DialogUtils.showErrorDialog(panel,
                        ResourceManager.getString("error.unexpected") + ": " + e.getMessage(),
                        core.ResourceManager.getString("error")
                );
            }
        });

        return panel;
    }

    /**
     * Loads the current user's borrowed books into the "My Books" table.
     * Retrieves borrowed books using core.DataBaseManager.findBorrowedBooks() and displays
     * book titles, issue dates, due dates, status, and calculated fees.
     * Status includes visual indicators: on time, due today, or overdue.
     * Handles encryption/decryption of user data and database access errors.
     *
     * @param gui The GUI instance to which the table belongs
     * @param model The DefaultTableModel to populate with the user's borrowed books
     */
    public static void loadMyBooksToTable(GUI gui, DefaultTableModel model) {
        model.setRowCount(0);

        try {
            // Session validation check - both user and encryption key must exist
            if (SessionManager.getInstance().getCurrentUser() == null || SessionManager.getInstance().getKey() == null) {
                model.addRow(new Object[]{
                        ResourceManager.getString("error.session"),
                        "", "", ""
                });
                DialogUtils.showErrorDialog(gui,
                        core.ResourceManager.getString("error.session.details"),
                        ResourceManager.getString("error")
                );
                return;
            }

            core.DataBaseManager dbm = new DataBaseManager();
            JsonArray books;

            try {
                books = dbm.findBorrowedBooks(core.SessionManager.getInstance().getCurrentUser(), SessionManager.getInstance().getKey());
            } catch (Exception e) {
                model.addRow(new Object[]{
                        ResourceManager.getString("error.load.books"),
                        "", "", ""
                });
                DialogUtils.showErrorDialog(gui,
                        core.ResourceManager.getString("error.load.books.details") + ": " + e.getMessage(),
                        core.ResourceManager.getString("error")
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
                        dateIssued = SecurityManager.decrypt(book.get("DateIssued").getAsString(), SessionManager.getInstance().getKey());
                    } catch (Exception e) {
                        dateIssued = core.ResourceManager.getString("date.unknown");
                        loadErrors++;
                    }

                    String userType;
                    try {
                        userType = dbm.getUserType(SessionManager.getInstance().getCurrentUser(), SessionManager.getInstance().getKey());
                        if (userType == null) {
                            throw new Exception("User type not found");
                        }
                    } catch (Exception e) {
                        model.addRow(new Object[]{
                                title,
                                dateIssued,
                                core.ResourceManager.getString("date.unknown"),
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
                                dbm.updateDueStatus(SessionManager.getInstance().getCurrentUser(), bookId, statusActual, core.SessionManager.getInstance().getKey());
                            } catch (Exception e) {
                                logger.log(Level.WARNING, "Failed to update due status for book: " + bookId, e);
                            }
                        }
                    } catch (Exception e) {
                        statusActual = -2;  // Error status code
                    }

                    String statusMsg = StatusUtils.getStatusMessage(statusActual);

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
                DialogUtils.showErrorDialog(gui,
                        String.format(ResourceManager.getString("error.load.some.books"), loadErrors),
                        ResourceManager.getString("warning")
                );
            }

            updateFinesDisplay();
            
        } catch (Exception e) {
            model.setRowCount(0);
            model.addRow(new Object[]{
                    ResourceManager.getString("error.unexpected"),
                    "", "", ""
            });
            DialogUtils.showErrorDialog(gui,
                    core.ResourceManager.getString("error.unexpected.details") + ": " + e.getMessage(),
                    ResourceManager.getString("error")
            );
        }
    }

    /**
     * Updates the total fines display based on current borrowed books.
     * Shows the fines label only if there are outstanding fines.
     */
    private static void updateFinesDisplay() {
        if (totalFinesLabel == null) {
            return;
        }
        
        double totalFines = FeeManager.calculateTotalFines();
        
        if (totalFines > 0) {
            totalFinesLabel.setText(ResourceManager.getString("fines.total.outstanding", String.format("%.2f", totalFines)));
            totalFinesLabel.setVisible(true);
        } else {
            totalFinesLabel.setVisible(false);
        }
    }

}