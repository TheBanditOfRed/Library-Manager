package ui.panels.forms;

import core.DataBaseManager;
import core.ResourceManager;
import ui.GUI;
import ui.panels.ManagementPanel;
import ui.panels.PanelSwitcher;
import ui.utils.DialogUtils;
import ui.utils.StatusUtils;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class AddBookForm {
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AddBookForm.class.getName());

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
        ManagementPanel.shelfNumberField = new JTextField(shelfNumber != null ? shelfNumber : "", 15);
        ManagementPanel.titleField = new JTextField(title != null ? title : "", 15);
        ManagementPanel.authorField = new JTextField(author != null ? author : "", 15);
        ManagementPanel.publisherField = new JTextField(publisher != null ? publisher : "", 15);
        ManagementPanel.availableField = new JTextField(available != null ? available : "", 15);
        ManagementPanel.onLoanField = new JTextField(onLoan != null ? onLoan : "", 15);

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
        formPanel.add(ManagementPanel.shelfNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(titleLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(authorLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(publisherLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.publisherField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(availableLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.availableField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(onLoanLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(ManagementPanel.onLoanField, gbc);

        setupAddBookActionListeners(gui, saveButton, cancelButton, panel, isEditMode, bookId);


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
    private static void setupAddBookActionListeners(GUI gui, JButton saveButton, JButton cancelButton, JPanel panel, boolean isEditMode, String bookId){
        ManagementPanel.shelfNumberField.addActionListener(_ -> ManagementPanel.titleField.requestFocusInWindow());
        ManagementPanel.titleField.addActionListener(_ -> ManagementPanel.authorField.requestFocusInWindow());
        ManagementPanel.authorField.addActionListener(_ -> ManagementPanel.publisherField.requestFocusInWindow());
        ManagementPanel.publisherField.addActionListener(_ -> ManagementPanel.availableField.requestFocusInWindow());
        ManagementPanel.availableField.addActionListener(_ -> ManagementPanel.onLoanField.requestFocusInWindow());
        ManagementPanel.onLoanField.addActionListener(_ -> saveButton.doClick());

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
            String newShelfNumber = ManagementPanel.shelfNumberField.getText().trim();
            String newTitle = ManagementPanel.titleField.getText().trim();
            String newAuthor = ManagementPanel.authorField.getText().trim();
            String newPublisher = ManagementPanel.publisherField.getText().trim();
            String newAvailable = ManagementPanel.availableField.getText().trim();
            String newOnLoan = ManagementPanel.onLoanField.getText().trim();

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
                successMessage = core.ResourceManager.getString("book.update.success");

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
        ManagementPanel.shelfNumberField.setText("");
        ManagementPanel.titleField.setText("");
        ManagementPanel.authorField.setText("");
        ManagementPanel.publisherField.setText("");
        ManagementPanel.availableField.setText("");
        ManagementPanel.onLoanField.setText("");
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
}
